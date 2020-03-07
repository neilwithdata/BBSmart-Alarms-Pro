//#preprocess
package com.bbsmart.pda.blackberry.bbtime;

import com.bbsmart.pda.blackberry.bbtime.io.TrialManager;
import com.bbsmart.pda.blackberry.bbtime.ui.screens.AlarmListScreen;
import com.bbsmart.pda.blackberry.bbtime.ui.screens.TrialEndedScreen;
import com.bbsmart.pda.blackberry.bbtime.util.AlarmBackgroundListener;
import com.bbsmart.pda.blackberry.bbtime.util.BBLogger;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.ui.UiApplication;

//#ifdef 4.7
import net.rim.device.api.ui.Ui;
import net.rim.device.api.system.Display;
//#endif

//#ifndef 4.2
import net.rim.device.api.system.EncodedImage;
import net.rim.blackberry.api.messagelist.*;
//#endif

public class BBSmartTimePro extends UiApplication {
	public static UiApplication instance;
	private TrialManager tMan;
	
	//#ifndef 4.2
	private ApplicationIndicator appIndicator;

	public ApplicationIndicator getAppIndicator() {
		return appIndicator;
	}
	//#endif

	public static void main(String[] args) throws Exception {
		instance = new BBSmartTimePro();
		
		//#ifdef 4.7
		Ui.getUiEngineInstance().setAcceptableDirections(Display.DIRECTION_NORTH);
		//#endif
		
		// Fairly nasty hack to make the home screen default icon correct
		instance.invokeLater(new Runnable() {
			public void run() {
				try {
					// Ensure correct icon is set on startup
					UiUtilities.updateIcon();
				} catch (NullPointerException npe) {
					// In OS 4.5.0.37 RIM introduced a bug which causes
					// updateIcon to throw a null pointer exception
				}
			}
		}, 2000, false);

		instance.enterEventDispatcher();
	}

	private BBSmartTimePro() {
		// Initialize the event logging utility
		BBLogger.initialize();

		tMan = TrialManager.getInstance();
		
		// 4.6 and 4.7 support the banner notification - register the indicator with the system
		//#ifndef 4.2
		ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
				.getInstance();
		ApplicationIcon appIcon = new ApplicationIcon(EncodedImage
				.getEncodedImageResource("bannericon.png"));

		appIndicator = reg.register(appIcon, false, false);
		//#endif

		switch (tMan.state) {
		case TrialManager.STATE_TRIAL:
			// Remember the first time the application is run
			if (tMan.isFirstRun()) {
				tMan.setFirstTimeRun();
				tMan.save();
			}

			if (tMan.isTrialExpired()) {
				tMan.state = TrialManager.STATE_TRIAL_EX;
				tMan.save();

				pushScreen(new TrialEndedScreen());
				break;
			}

			// intentional fall-through
		case TrialManager.STATE_REG:
			// Display the listing of alarms and start the listeners
			pushScreen(new AlarmListScreen());
			addRealtimeClockListener(new AlarmBackgroundListener());
			break;
		case TrialManager.STATE_TRIAL_EX:
			pushScreen(new TrialEndedScreen());
			break;
		}
	}
}