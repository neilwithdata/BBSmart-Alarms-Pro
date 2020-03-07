package com.bbsmart.pda.blackberry.bbtime.util;

import net.rim.device.api.system.Alert;

import com.bbsmart.pda.blackberry.bbtime.models.Alarm;

public class AlarmNotificationThread implements Runnable {
	private Alarm alarm;
	private boolean running = false;
	private Thread theThread;
	private int vibratePeriod = 1000;
	private AlarmSoundHelper alarmTone;

	public AlarmNotificationThread(Alarm alarm) {
		this.alarm = alarm;
		alarmTone = new AlarmSoundHelper(alarm);
	}

	public void start() {
		if (!running) {
			running = true;
			theThread = new Thread(this);
			theThread.start();
		}
		alarmTone.startAlarmSound();
	}

	public void stop() {
		if (theThread != null && running) {
			running = false;
			theThread.interrupt();
			try {
				theThread.join();
			} catch (Exception e) {
				// Do nothing - nobody should be interrupting us!
			}
			theThread = null;
		}
		alarmTone.stopAlarmSound();
	}

	public void run() {
		while (running) {
			if (alarm.vibrate) {
				Alert.startVibrate(vibratePeriod / 2);
			}
			try {
				Thread.sleep(vibratePeriod);
			} catch (Exception e) {
				// interrupted
				Alert.stopVibrate();
			}
		}
		;
	}

}
