package com.bbsmart.pda.blackberry.bbtime.util;

import java.util.Calendar;

import net.rim.device.api.i18n.DateFormat;

public class TimeUtil {
	public static String getTimeString(Calendar calTime) {
		String time;
		time = DateFormat.getInstance(DateFormat.TIME_DEFAULT).formatLocal(
				calTime.getTime().getTime());
		return time;
	}	
}
