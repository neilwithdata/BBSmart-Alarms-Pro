package com.bbsmart.pda.blackberry.bbtime.io;

import com.bbsmart.pda.blackberry.bbtime.AppInfo;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmPreferences;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public final class PersistenceManager {

	private static PersistentObject store = PersistentStore
			.getPersistentObject(AppInfo.APP_KEY);
	
	public static PersistentObject privateStore = PersistentStore
			.getPersistentObject(AppInfo.APP_PRIVATE_KEY);

	private static BBSmartVector data;

	private static final int NUM_STORE_ITEMS = 5;
	// Element 0: Store version number
	// Element 1: DefaultAlarm
	// Element 2: AlarmList
	// Element 3: AlarmPreferences
	// Element 4: DefaultQuickAlarm

	static {
		synchronized (store) {
			try {
				data = (BBSmartVector) store.getContents();
				if (data == null) { // Create for the first time
					data = new BBSmartVector();
					defaultPopulateStore(data);
				} else {
					String currentVersion = AppInfo.VERSION_STRING;
					String storeVersion = (String) data.elementAt(0);

					if (!currentVersion.equals(storeVersion)) {
						upgradeDataStore(data, storeVersion);
					}
				}
			} catch (Exception e) { // Exception occurred upgrading store; reset
				data = new BBSmartVector();
				defaultPopulateStore(data);
			} finally {
				store.setContents(data);
				store.commit();
			}
		}
	}

	private static void defaultPopulateStore(BBSmartVector data) {
		data.removeAllElements();
		data.setSize(NUM_STORE_ITEMS);

		// Store version number
		data.setElementAt(AppInfo.VERSION_STRING, 0);

		// Other persistent objects will dynamically add themselves on-demand
	}

	/**
	 * Upgrades the data store (data)
	 * 
	 * @param storeVersion
	 * @param data
	 */
	private static void upgradeDataStore(BBSmartVector data, String storeVersion) {
		float fStoreVersion = Float.parseFloat(storeVersion);

		if (fStoreVersion < 1.0) {
			// Destroy data from alpha/beta releases (potentially out of date
			// data stores)
			defaultPopulateStore(data);
		} else if (fStoreVersion == 1.0f) { // Upgrading from v1.0
			// ** UPGRADE ALARM PREFERENCES ** //
			AlarmPreferences alarmPrefs = AlarmPreferences
					.getThrowawayInstance();
			Object[] oldPrefs = (Object[]) data
					.elementAt(AlarmPreferences.STORE_INDX);

			alarmPrefs.promptOnAlarmDelete = ((Boolean) oldPrefs[0])
					.booleanValue();

			data.setElementAt(alarmPrefs.getPersistentObj(),
					AlarmPreferences.STORE_INDX);
		}

		data.setElementAt(AppInfo.VERSION_STRING, 0);
	}
	
	public static Object getStoreDataAtIndex(int index) {
		Object o = null;
		synchronized (store) {
			try {
				data = (BBSmartVector) store.getContents();
				o = data.elementAt(index);
			} catch (Exception e) {
				return null; // Controlled access exception - hide silently
			}
		}
		return o;
	}

	public static void setStoreDataAtIndex(int index, Object o) {
		synchronized (store) {
			try {
				data = (BBSmartVector) store.getContents();
				data.setElementAt(o, index);
				store.setContents(data);
				store.commit();
			} catch (Exception e) {
				// Controlled access exception - hide silently
			}
		}
	}
}