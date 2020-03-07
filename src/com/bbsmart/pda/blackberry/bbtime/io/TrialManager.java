package com.bbsmart.pda.blackberry.bbtime.io;

import com.bbsmart.pda.blackberry.bbtime.AppInfo;

public class TrialManager {
	private final long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

	public static final int STATE_TRIAL = 0;
	public static final int STATE_TRIAL_EX = 1;
	public static final int STATE_REG = 3;

	public String storeVersion;
	public int state;
	public long firstTimeRun;

	// Singleton Accessor
	private static TrialManager instance;

	public static TrialManager getInstance() {
		if (instance == null) {
			instance = new TrialManager();
		}
		return instance;
	}

	public void setFirstTimeRun() {
		firstTimeRun = System.currentTimeMillis();
	}

	public boolean isFirstRun() {
		return (firstTimeRun == 0L);
	}

	public boolean isTrialExpired() {
		long currentTime = System.currentTimeMillis();

		if (currentTime < firstTimeRun) { // Catch out funny business...
			return true;
		}

		if (currentTime - firstTimeRun >= MILLIS_IN_DAY
				* AppInfo.TRIAL_DURATION_DAYS) {
			// Trial period has expired
			return true;
		}
		return false;
	}

	public String getTrialTimeRemaining() {
		long timeRemInMS = firstTimeRun
				+ (AppInfo.TRIAL_DURATION_DAYS * MILLIS_IN_DAY)
				- System.currentTimeMillis();

		int daysRem = (int) (timeRemInMS / MILLIS_IN_DAY) + 1;

		if (timeRemInMS <= 0) {
			return "Expired!";
		} else {
			return daysRem + " days left";
		}
	}

	public String getStateString() {
		switch (state) {
		case STATE_REG:
			return "Registered";
		case STATE_TRIAL:
			return "Trial (" + getTrialTimeRemaining() + ")";
		case STATE_TRIAL_EX:
			return "Trial (Expired!)";
		default:
			return "";
		}
	}

	private TrialManager() {
		Object persistentObj = PersistenceManager.privateStore.getContents();
		if (persistentObj == null) {
			// Currently not in the data store so create with defaults and save
			init();
			save();
		} else {
			persistentObj = upgradeStore(persistentObj);
			restorePersistentObj(persistentObj);
		}
	}

	// Makes sure that if a user had at a previous time installed an earlier
	// version of BBSmart Alarms Pro and their trial expired, they can install
	// the new version and have their trial period reset
	private Object upgradeStore(Object persistentObj) {
		Object[] objArray = (Object[]) persistentObj;

		if (objArray.length == 3) {
			// Persistent store is v1.0
			int oldState = ((Integer) objArray[0]).intValue();

			if (oldState == TrialManager.STATE_TRIAL
					|| oldState == TrialManager.STATE_TRIAL_EX) {
				// Reset the trial version
				init();
			} else {
				// User has a registered or full version - upgrade to include
				// store version and restore existing data
				storeVersion = AppInfo.VERSION_STRING;
				state = ((Integer) objArray[0]).intValue();
				firstTimeRun = ((Long) objArray[1]).longValue();
			}

			save();
			return getPersistentObj();
		} else if (objArray[0] == null
				|| !((String) objArray[0]).equals(AppInfo.VERSION_STRING)) {
			// Persistent store is v1.1 or higher
			// We do need to perform an upgrade of the data store

			int oldState = ((Integer) objArray[1]).intValue();
			if (oldState == TrialManager.STATE_TRIAL
					|| oldState == TrialManager.STATE_TRIAL_EX) {
				// If it was previously a trial version or the trial had
				// expired, we want to reset the trial back to beginning so the
				// user can try out the new version
				init();
			} else {
				// User has a registered or full version - upgrade to include
				// store version and restore existing data
				storeVersion = AppInfo.VERSION_STRING;
				state = ((Integer) objArray[1]).intValue();
				firstTimeRun = ((Long) objArray[2]).longValue();
			}

			save();
			return getPersistentObj();
		} else if (((String) objArray[0]).equals(AppInfo.VERSION_STRING)) {
			// No upgrade required, simply restoring from DB
			
			// If user previously had trial version installed, upgrade to
			// registered if that is what they have just installed
			if (AppInfo.APP_START_STATE == TrialManager.STATE_REG) {
				init();
				save();
				return getPersistentObj();
			}
		}

		return persistentObj;
	}

	public void init() {
		storeVersion = AppInfo.VERSION_STRING;
		state = AppInfo.APP_START_STATE;
		firstTimeRun = 0L;
	}

	public void save() {
		synchronized (PersistenceManager.privateStore) {
			PersistenceManager.privateStore.setContents(getPersistentObj());
			PersistenceManager.privateStore.commit();
		}
	}

	public Object getPersistentObj() {
		Object[] persistentObj = new Object[4];

		persistentObj[0] = storeVersion;
		persistentObj[1] = new Integer(state);
		persistentObj[2] = new Long(firstTimeRun);

		return persistentObj;
	}

	public void restorePersistentObj(Object persistentObj) {
		Object[] objArray = (Object[]) persistentObj;

		storeVersion = (String) objArray[0];
		state = ((Integer) objArray[1]).intValue();
		firstTimeRun = ((Long) objArray[2]).longValue();
	}
}