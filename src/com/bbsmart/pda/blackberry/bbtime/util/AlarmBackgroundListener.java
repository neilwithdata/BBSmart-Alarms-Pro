package com.bbsmart.pda.blackberry.bbtime.util;

import java.util.Calendar;
import java.util.Vector;

import net.rim.device.api.system.RealtimeClockListener;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.io.TrialManager;
import com.bbsmart.pda.blackberry.bbtime.models.Alarm;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmList;
import com.bbsmart.pda.blackberry.bbtime.ui.screens.AlarmNotificationScreen;

public class AlarmBackgroundListener implements RealtimeClockListener {
	private Vector alarmList;
	private int lastMinute;
	private int lastHour;

	public AlarmBackgroundListener() {
		alarmList = AlarmList.getInstance().alarmList;
		lastMinute = -1;
		lastHour = -1;
	}

	public void clockUpdated() {
		TrialManager tMan = TrialManager.getInstance();
		if (tMan.state == TrialManager.STATE_TRIAL) {
			if (tMan.isTrialExpired()) {
				// Change the state to expired
				tMan.state = TrialManager.STATE_TRIAL_EX;
				tMan.save();

				// Trial has now expired...exit out...
				System.exit(0);
			}
		}

		// Activate any alarms that are due to go off now
		Calendar cal = Calendar.getInstance();
		int currMinute = cal.get(Calendar.MINUTE);
		int currHour = cal.get(Calendar.HOUR_OF_DAY);
		int currWeekdayBit = 1 << (cal.get(Calendar.DAY_OF_WEEK) - 1);
		
		if ((lastMinute == currMinute) && (lastHour == currHour)) {
			// I put this check in here since I suspect that some of the bugs
			// people are reporting of an alarm going off after being dismissed
			// may be due to a faulty real time clock listener that is
			// triggering twice within the same minute
			return;
		}
		lastMinute = currMinute;
		lastHour = currHour;

		Alarm currAlarm;
		for (int idx = 0; idx < alarmList.size(); idx++) {
			currAlarm = (Alarm) alarmList.elementAt(idx);

			if (currAlarm.isRingNow(currMinute, currHour, currWeekdayBit)) {
				BBSmartTimePro.instance.pushGlobalScreen(
						new AlarmNotificationScreen(currAlarm), 0, 0);
			}
		}
	}
}
