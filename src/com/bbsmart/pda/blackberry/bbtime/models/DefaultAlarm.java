package com.bbsmart.pda.blackberry.bbtime.models;

import com.bbsmart.pda.blackberry.bbtime.io.PersistenceManager;

public class DefaultAlarm extends Alarm {
	private static final int STORE_INDX = 1;

	// Singleton Accessor
	private static DefaultAlarm instance;

	public static DefaultAlarm getInstance() {
		if (instance == null) {
			instance = new DefaultAlarm();
		}
		return instance;
	}

	private DefaultAlarm() {
		Object persistentObj = PersistenceManager
				.getStoreDataAtIndex(STORE_INDX);
		if (persistentObj == null) {
			// Currently not in the data store so create with defaults and save
			init();
			save();
		} else {
			restorePersistentObj(persistentObj);
		}
	}

	private void init() {
		// status
		name = "Alarm";
		enabled = true;

		// time
		hour = 0;
		minute = 0;
		recurring = true;
		daysOfWeek = Alarm.WEEKDAYS;

		// sound
		soundType = Alarm.SOUND_PRE;
		soundVolume = 100;
		soundRepeats = 0; // forever
		soundPreIndx = 0;
		soundFileName = "";

		// alarm settings
		vibrate = true;
		silentUnholster = true;
		snoozeLength = 10;
		
		// Default alarm color (green)
		alarmColorIndx = 0;
	}

	public void save() {
		PersistenceManager.setStoreDataAtIndex(STORE_INDX, getPersistentObj());
	}
}