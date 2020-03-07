package com.bbsmart.pda.blackberry.bbtime.models;

import com.bbsmart.pda.blackberry.bbtime.io.PersistenceManager;

public final class AlarmPreferences {
	public static final int STORE_INDX = 3;

	public boolean promptOnAlarmDelete;
	public boolean showSavingDialog;
	public boolean displayBannerIcon;

	// Singleton Accessor
	private static AlarmPreferences instance;
	
	// TO BE USED ONLY WHEN UPGRADING FROM OLD DATA STORE...AVOID OTHERWISE
	public static AlarmPreferences getThrowawayInstance() {
		return new AlarmPreferences(true);
	}

	public static AlarmPreferences getInstance() {
		if (instance == null) {
			instance = new AlarmPreferences();
		}
		return instance;
	}
	
	private AlarmPreferences(boolean throwaway) {
		init();
	}

	private AlarmPreferences() {
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
		promptOnAlarmDelete = true;
		showSavingDialog = false;
		displayBannerIcon = true;
	}

	public void save() {
		PersistenceManager.setStoreDataAtIndex(STORE_INDX, getPersistentObj());
	}

	public Object getPersistentObj() {
		Object[] persistentObj = new Object[3];
		persistentObj[0] = new Boolean(promptOnAlarmDelete);
		persistentObj[1] = new Boolean(showSavingDialog);
		persistentObj[2] = new Boolean(displayBannerIcon);
		return persistentObj;
	}

	public void restorePersistentObj(Object persistentObj) {
		Object[] persistentArray = (Object[]) persistentObj;

		promptOnAlarmDelete = ((Boolean) persistentArray[0]).booleanValue();
		showSavingDialog = ((Boolean) persistentArray[1]).booleanValue();

		// Try to restore the property "displayBannerIcon". If it does not exist,
		// set it to the default value = true
		try {
			displayBannerIcon = ((Boolean) persistentArray[2]).booleanValue();
		} catch (Exception e1) {
			displayBannerIcon = true;
		}
	}
}