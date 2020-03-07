package com.bbsmart.pda.blackberry.bbtime.util;

import com.bbsmart.pda.blackberry.bbtime.AppInfo;

import net.rim.device.api.system.EventLogger;

public final class BBLogger {
	public static void initialize() {
		EventLogger.register(AppInfo.APP_KEY, "BBSmart Alarms Pro",
				EventLogger.VIEWER_STRING);
	}

	public static void logEvent(String eventMsg) {
		EventLogger.logEvent(AppInfo.APP_KEY, eventMsg.getBytes());
	}
}