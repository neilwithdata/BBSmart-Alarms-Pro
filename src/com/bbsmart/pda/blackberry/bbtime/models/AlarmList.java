package com.bbsmart.pda.blackberry.bbtime.models;

import java.util.Vector;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.io.PersistenceManager;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

public final class AlarmList {
	private static final int STORE_INDX = 2;

	public Vector alarmList;

	// Singleton Accessor
	private static AlarmList instance;

	// list of objects to tell when alarms change
	private static Vector alarmListeners;

	static {
		alarmListeners = new Vector();
	}

	public static AlarmList getInstance() {
		if (instance == null) {
			instance = new AlarmList();
		}
		return instance;
	}

	private AlarmList() {
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
		alarmList = new Vector();
	}

	public void save() {
		PersistenceManager.setStoreDataAtIndex(STORE_INDX, getPersistentObj());
	}

	public Object getPersistentObj() {
		Vector persistentVect = new Vector(alarmList.size());

		Alarm a;
		for (int i = 0; i < alarmList.size(); i++) {
			a = (Alarm) alarmList.elementAt(i);
			persistentVect.addElement(a.getPersistentObj());
		}

		return persistentVect;
	}

	public void restorePersistentObj(Object persistentObj) {
		Vector persistentVect = (Vector) persistentObj;
		alarmList = new Vector(persistentVect.size());

		Alarm a;
		for (int i = 0; i < persistentVect.size(); i++) {
			a = new Alarm();
			a.restorePersistentObj(persistentVect.elementAt(i));
			alarmList.addElement(a);
		}
	}

	static public void addAlarmListener(AlarmListener listener) {
		if (listener != null && !alarmListeners.contains(listener)) {
			alarmListeners.addElement(listener);
		}
	}

	static public void removeAlarmListener(AlarmListener listener) {
		alarmListeners.removeElement(listener);
	}
	
	public boolean isAnyActiveAlarms() {
		for (int i = 0; i < alarmList.size(); i++) {
			if (((Alarm) alarmList.elementAt(i)).enabled) {
				return true;
			}
		}

		return false;
	}
	
	public int getNumActiveAlarms() {
		int numActive = 0;

		for (int i = 0; i < alarmList.size(); i++) {
			if (((Alarm) alarmList.elementAt(i)).enabled) {
				numActive++;
			}
		}

		return numActive;
	}

	static public void notifyAlarmsChanged() {
		// Let any listeners know the alarm list has changed
		synchronized (BBSmartTimePro.instance.getAppEventLock()) {
			for (int i = 0; i < alarmListeners.size(); i++) {
				((AlarmListener) alarmListeners.elementAt(i)).alarmsChanged();
			}
		}

		UiUtilities.updateIcon();
	}

	static public interface AlarmListener {
		public abstract void alarmsChanged();
	}
}