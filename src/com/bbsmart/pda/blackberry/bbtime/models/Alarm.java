package com.bbsmart.pda.blackberry.bbtime.models;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.ui.screens.AlarmNotificationScreen;

public class Alarm {
	// **** ALARM STATUS **** //
	public String name;
	public boolean enabled;
	
	public volatile int alarmState;
	public volatile AlarmNotificationScreen notificationScreen;
	
	private long snoozeStartedAtTick = 0;
	private Timer snoozeTimer;
	
	public static final int STATE_DORMANT = 0;
	public static final int STATE_RINGING = 1;
	public static final int STATE_SNOOZED = 2;

	// **** ALARM TIME **** //
	public int hour;
	public int minute;
	public boolean recurring;
	public int daysOfWeek;

	public static final int SUNDAY = 1;
	public static final int MONDAY = 2;
	public static final int TUESDAY = 4;
	public static final int WEDNESDAY = 8;
	public static final int THURSDAY = 16;
	public static final int FRIDAY = 32;
	public static final int SATURDAY = 64;

	public static final int WEEKDAYS = MONDAY | TUESDAY | WEDNESDAY | THURSDAY
			| FRIDAY;

	// **** ALARM SOUND **** //
	public static final int SOUND_PRE = 0;
	public static final int SOUND_FILE = 1;
	public int soundType;
	public int soundVolume;
	public int soundRepeats; // 0 is forever

	// **** ALARM SOUND - PRELOADED **** //
	public int soundPreIndx;

	public static final String[] PRE_NAMES = { "Electric Piano", "Guitar Riff",
			"Drum Beat", "Disco" };

	// **** ALARM SOUND - FILE **** //
	public String soundFileName;

	// **** ALARM SETTINGS **** //
	public boolean vibrate;
	public boolean silentUnholster;
	public int snoozeLength;
	
	// The user can dynamically set the snooze time when this alarm goes off.
	// We do not persist this information anywhere, it is just a temporary
	// override of the normal snooze length
	public int snoozeLengthOverride;

	public static final String[] SNOOZE_OPTS = { "1", "2", "5", "10", "15",
			"30", "45", "60" };
	
	public int alarmColorIndx;

	public Alarm() {
		alarmState = STATE_DORMANT;
		notificationScreen = null;
	}
	
	/**
	 * Given the current minute, hour and day determines whether this alarm
	 * should ring
	 * 
	 * @param currMin
	 * @param currHour
	 * @param currWeekdayBit
	 * @return
	 */
	public boolean isRingNow(int currMin, int currHour, int currWeekdayBit) {
		if (enabled) { // alarm is enabled
			if ((currHour == hour) && (currMin == minute)) { // h&m match
				if (recurring) {
					if ((daysOfWeek & currWeekdayBit) > 0) { // day match
						return true;
					}
				} else {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Called when the user selects to dismiss this alarm (stop it from
	 * ringing).
	 */
	public void dismiss() {
		alarmState = STATE_DORMANT;
		notificationScreen = null;

		// stop snooze timer if started
		if (snoozeTimer != null) {
			snoozeTimer.cancel();
		}

		if (!recurring) {
			AlarmList.getInstance().alarmList.removeElement(this);
			AlarmList.getInstance().save();
			AlarmList.notifyAlarmsChanged();
		}
	}
	
	public void snooze(int snoozeLengthOverride) {
		alarmState = STATE_SNOOZED;
		
		this.snoozeLengthOverride = snoozeLengthOverride;
		
		// Record the time when the snooze was started
		snoozeStartedAtTick = System.currentTimeMillis();
		
		// Start the snooze count-down timer
		snoozeTimer = new Timer();
		snoozeTimer.schedule(new SnoozeTimerTask(), 0, 100);
	}
	
	public void unsnooze() {
		if (alarmState == STATE_SNOOZED) {
			alarmState = STATE_RINGING;
			snoozeTimer.cancel();

			if (notificationScreen == null) {
				synchronized (BBSmartTimePro.getEventLock()) {
					BBSmartTimePro.instance.pushGlobalScreen(
							new AlarmNotificationScreen(this), 0, 0);
				}
			} else {
				// modify the existing screen
				notificationScreen.unsnooze();
			}
		}
	}
	
	public String getSnoozeTimeString() {
		int elapsedTime = (int) (System.currentTimeMillis() - snoozeStartedAtTick) / 1000;
		int timeRemaining = (snoozeLengthOverride * 60) - elapsedTime;
		int minRem = (int) (timeRemaining / 60);
		int secRem = timeRemaining - (minRem * 60);
		String secString = "" + secRem;
		if (secRem < 10) {
			secString = "0" + secString;
		}
		return "" + minRem + ":" + secString;
	}
	
	class SnoozeTimerTask extends TimerTask {
		public void run() {
			// This is called around every 100ms
			// update the label which is showing how long the alarm has left
			long elapsedTime = System.currentTimeMillis() - snoozeStartedAtTick;
			int elapsedMinutes = (int) (elapsedTime / 60000);

			if (elapsedMinutes < snoozeLengthOverride) {
				if (notificationScreen != null) {
					notificationScreen.updateSnoozeTimeLeft();
				}
			} else {
				// re-trigger the alarm if the snooze time expires!
				unsnooze();
			}
		}
	}

	public Alarm(Alarm toCopy) {
		this();
		
		this.name = toCopy.name;
		this.enabled = toCopy.enabled;

		this.hour = toCopy.hour;
		this.minute = toCopy.minute;
		this.recurring = toCopy.recurring;
		this.daysOfWeek = toCopy.daysOfWeek;

		this.soundType = toCopy.soundType;
		this.soundVolume = toCopy.soundVolume;
		this.soundRepeats = toCopy.soundRepeats;

		this.soundPreIndx = toCopy.soundPreIndx;

		this.soundFileName = toCopy.soundFileName;

		this.vibrate = toCopy.vibrate;
		this.silentUnholster = toCopy.silentUnholster;
		this.snoozeLength = toCopy.snoozeLength;
		
		this.alarmColorIndx = toCopy.alarmColorIndx;

		if (toCopy instanceof DefaultAlarm) {
			// Set the time of the alarm - always next whole hour
			// get the next whole hour and put it here
			Calendar currTime = Calendar.getInstance();
			this.hour = currTime.get(Calendar.HOUR_OF_DAY);
			this.hour++;
			if (this.hour == 24) {
				this.hour = 0;
			}
			this.minute = 0;
		}
	}

	public Object getPersistentObj() {
		Object[] persistentObj = new Object[15];

		persistentObj[0] = name;
		persistentObj[1] = new Boolean(enabled);

		persistentObj[2] = new Integer(hour);
		persistentObj[3] = new Integer(minute);
		persistentObj[4] = new Boolean(recurring);
		persistentObj[5] = new Integer(daysOfWeek);

		persistentObj[6] = new Integer(soundType);
		persistentObj[7] = new Integer(soundVolume);
		persistentObj[8] = new Integer(soundRepeats);

		persistentObj[9] = new Integer(soundPreIndx);

		persistentObj[10] = soundFileName;

		persistentObj[11] = new Boolean(vibrate);
		persistentObj[12] = new Boolean(silentUnholster);
		persistentObj[13] = new Integer(snoozeLength);
		
		persistentObj[14] = new Integer(alarmColorIndx);

		return persistentObj;
	}

	public void restorePersistentObj(Object persistentObj) {
		Object[] persistentArray = (Object[]) persistentObj;

		name = (String) persistentArray[0];
		enabled = ((Boolean) persistentArray[1]).booleanValue();

		hour = ((Integer) persistentArray[2]).intValue();
		minute = ((Integer) persistentArray[3]).intValue();
		recurring = ((Boolean) persistentArray[4]).booleanValue();
		daysOfWeek = ((Integer) persistentArray[5]).intValue();

		soundType = ((Integer) persistentArray[6]).intValue();
		soundVolume = ((Integer) persistentArray[7]).intValue();
		soundRepeats = ((Integer) persistentArray[8]).intValue();

		soundPreIndx = ((Integer) persistentArray[9]).intValue();

		soundFileName = (String) persistentArray[10];

		vibrate = ((Boolean) persistentArray[11]).booleanValue();
		silentUnholster = ((Boolean) persistentArray[12]).booleanValue();
		snoozeLength = ((Integer) persistentArray[13]).intValue();

		if (persistentArray.length == 14) {
			// Upgrading from v1.1 to 1.21 -- alarmColorIndx was added
			alarmColorIndx = 0;
		} else {
			alarmColorIndx = ((Integer) persistentArray[14]).intValue();
		}
	}
}
