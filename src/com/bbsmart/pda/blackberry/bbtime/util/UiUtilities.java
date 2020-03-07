//#preprocess
package com.bbsmart.pda.blackberry.bbtime.util;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmList;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmPreferences;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;

//#ifndef 4.2
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
//#endif

public final class UiUtilities {
	public static final String DEVICE_NAME = DeviceInfo.getDeviceName();
	
	public static final boolean IS_TOUCH = DEVICE_NAME.startsWith("95");

	// Curve, Pearl, 88xx series devices
	public static final boolean DEVICE_240W = DEVICE_NAME.startsWith("7")
			|| DEVICE_NAME.startsWith("81") || DEVICE_NAME.startsWith("82")
			|| DEVICE_NAME.startsWith("83");

	// 9000 (Bold), 95xx (Storm), 8900, 9630 (Tour) have 480w screen
	public static final boolean DEVICE_480W = DEVICE_NAME.startsWith("9")
			|| DEVICE_NAME.startsWith("89");

	// Is a SureType device (81xx, 82xx, 71xx)
	public static final boolean DEVICE_SURETYPE = DEVICE_NAME.startsWith("71")
			|| DEVICE_NAME.startsWith("81") || DEVICE_NAME.startsWith("82");

	public static final boolean HAS_TRACKBALL = DEVICE_NAME.startsWith("81")
			|| DEVICE_NAME.startsWith("82") || DEVICE_NAME.startsWith("83")
			|| DEVICE_NAME.startsWith("88") || DEVICE_NAME.startsWith("89")
			|| DEVICE_NAME.startsWith("9");

	public static final Bitmap ALARM_HEADING_BG = get("title_bg_480.jpg");

	public static final Bitmap ALARMBELL = get("alarmbell1.png");
	public static final Bitmap OPTIONS = get("options_icon.png");
	public static final Bitmap ALERT_SMALL = get("alert-small.png");
	public static final Bitmap KEY_SMALL = get("key_icon_small.png");

	public static final Bitmap BLACK_HEADING = get("black-heading.png");

	public static final Bitmap[] ALARM_BG_COLORS = new Bitmap[10];
	static {
		for (int i = 0; i < 10; i++) {
			ALARM_BG_COLORS[i] = get((DEVICE_480W ? "hr_" : "") + i + ".jpg");
		}
	}
	
	public static String OS_VERSION;

	static {
		//#ifdef 4.2
		ApplicationDescriptor[] apps = ApplicationManager
				.getApplicationManager().getVisibleApplications();

		for (int i = 0; i < apps.length; i++) {
			if ((apps[i].getModuleName()).equals("net_rim_bb_ribbon_app")) {
				OS_VERSION = apps[i].getVersion();
			}
		}
		//#else
		OS_VERSION = DeviceInfo.getSoftwareVersion();
		//#endif
	}

	public static final Bitmap NOALARMS_BG;
	static {
		if (IS_TOUCH) {
			NOALARMS_BG = get("cloud_360x480.jpg");
		} else {
			NOALARMS_BG = get("cloud_480x360.jpg");
		}
	}


	public static Bitmap get(String imageName) {
		return Bitmap.getBitmapResource(imageName);
	}
	
	private static String getHomeIconName() {
		AlarmList alarmList = AlarmList.getInstance();

		// Update the home screen icon
		if (DEVICE_NAME.startsWith("87") || DEVICE_NAME.startsWith("83")
				|| DEVICE_NAME.startsWith("81") || DEVICE_NAME.startsWith("88")) {
			if (alarmList.isAnyActiveAlarms()) {
				return "icon_32_on.png";
			} else {
				return "icon_32_off.png";
			}
		} else {
			if (alarmList.isAnyActiveAlarms()) {
				return "icon_80_on.png";
			} else {
				return "icon_80_off.png";
			}
		}
	}

	public static void updateIcon() {
		Bitmap icon = get(getHomeIconName());
		HomeScreen.updateIcon(icon);
		
		//#ifndef 4.2
		// Update the banner notification
		ApplicationIndicator appI = ((BBSmartTimePro) BBSmartTimePro.instance)
				.getAppIndicator();
		
		if (AlarmPreferences.getInstance().displayBannerIcon) {
			AlarmList alarmList = AlarmList.getInstance();
			appI.setValue(alarmList.getNumActiveAlarms());
			appI.setVisible(alarmList.isAnyActiveAlarms());
		} else {
			appI.setValue(0);
			appI.setVisible(false);
		}
		//#endif
	}
}