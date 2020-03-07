package com.bbsmart.pda.blackberry.bbtime.models;

import com.bbsmart.pda.blackberry.bbtime.io.PersistenceManager;

public final class DefaultQuickAlarm extends Alarm {
	private static final int STORE_INDX = 4;

	// Singleton Accessor
	private static DefaultQuickAlarm instance;

	public static DefaultQuickAlarm getInstance() {
		if (instance == null) {
			instance = new DefaultQuickAlarm();
		}
		return instance;
	}

	private DefaultQuickAlarm() {
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
		name = "Quick Alarm";
		enabled = true;

		// time
		hour = 0;
		minute = 0;
		recurring = false;
		daysOfWeek = Alarm.WEEKDAYS;

		// sound
		soundType = Alarm.SOUND_PRE;
		soundVolume = 100;
		soundRepeats = 0; // forever
		soundPreIndx = 0;
		soundFileName = "";

		// alarm settings
		snoozeLength = 10;
		silentUnholster = true;
		vibrate = true;
		
		// Default alarm color (green)
		alarmColorIndx = 0;
	}

	public void save() {
		PersistenceManager.setStoreDataAtIndex(STORE_INDX, getPersistentObj());
	}
}