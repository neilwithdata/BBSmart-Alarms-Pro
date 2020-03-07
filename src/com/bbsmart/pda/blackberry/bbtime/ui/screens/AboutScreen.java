package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.AppInfo;
import com.bbsmart.pda.blackberry.bbtime.io.TrialManager;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.HrefField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.NowHeadingField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.SpacerField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.StyledButtonField;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public class AboutScreen extends MainScreen {
	private TrialManager tMan;
	
	private int btnSize;
	private int btnWidth;

	public AboutScreen() {
		tMan = TrialManager.getInstance();

		setDisplayPrefs();
		initDisplay();
	}
	
	private void setDisplayPrefs() {
		setScreenFont();
		
		if (UiUtilities.DEVICE_480W) {
			btnSize = StyledButtonField.SIZE_LARGE;
			btnWidth = 320;
		} else {
			btnSize = StyledButtonField.SIZE_MID;
			btnWidth = 170;
		}
	}

	private void setScreenFont() {
		int fontHeight = UiUtilities.DEVICE_480W ? 25 : 17;

		setFont(getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));
	}

	private void initDisplay() {
		add(new NowHeadingField("BBSmart Alarms Pro", false, UiUtilities
				.get("headeralarmicon.png")));

		Font btnFont = getFont().derive(0, getFont().getHeight() - 2);
		LabelField cpy = new LabelField("© BBSmart Solutions Pty Ltd",
				LabelField.FIELD_HCENTER);
		cpy.setFont(btnFont);
		add(cpy);

		HrefField siteLink = new HrefField("www.blackberrysmart.com", false,
				getFont());
		siteLink.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Browser.getDefaultSession().displayPage(
						"http://www.blackberrysmart.com");
			}
		});

		HorizontalFieldManager hfm = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm.add(siteLink);
		add(hfm);

		// Seperator
		add(new LabelField("", LabelField.NON_FOCUSABLE));

		add(new EditField("Version: ", AppInfo.VERSION_STRING,
				EditField.DEFAULT_MAXCHARS, EditField.READONLY));
		add(new EditField("Status: ", tMan.getStateString(),
				EditField.DEFAULT_MAXCHARS, EditField.READONLY));

		if (tMan.state != TrialManager.STATE_REG) {
			StyledButtonField registerButton = new StyledButtonField(
					"Buy Full Version", btnSize, btnWidth, getFont());
			registerButton.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					close();
					UiApplication.getUiApplication().pushScreen(
							new RegisterScreen());
				}
			});

			HorizontalFieldManager hfm1 = new HorizontalFieldManager(
					HorizontalFieldManager.FIELD_HCENTER);
			hfm1.add(registerButton);
			add(hfm1);
		}

		// Seperator
		add(new LabelField("", LabelField.NON_FOCUSABLE));

		add(new EditField("Device Type: ", DeviceInfo.getDeviceName(),
				EditField.DEFAULT_MAXCHARS, EditField.READONLY));

		add(new EditField("Device PIN: ", Integer.toHexString(
				DeviceInfo.getDeviceId()).toUpperCase(),
				EditField.DEFAULT_MAXCHARS, EditField.READONLY));

		// Seperator
		add(new LabelField("", LabelField.NON_FOCUSABLE));

		StyledButtonField checkForUpdatesBtn = new StyledButtonField(
				"Check for Updates", btnSize, btnWidth, getFont());
		checkForUpdatesBtn.setFont(btnFont);
		checkForUpdatesBtn.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				String updateURL = "http://www.blackberrysmart.com/apps/updater.php";
				updateURL += "?appName=alarmspro";
				updateURL += "&appVer=" + AppInfo.VERSION_STRING;
				updateURL += "&osVer=" + UiUtilities.OS_VERSION;
				updateURL += "&deviceModel=" + DeviceInfo.getDeviceName();
				
				Browser.getDefaultSession().displayPage(updateURL);
			}
		});

		StyledButtonField contactUsBtn = new StyledButtonField("Contact Us",
				btnSize, btnWidth, getFont());
		contactUsBtn.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				MessageArguments mArgs = new MessageArguments(
						MessageArguments.ARG_NEW,
						"support@blackberrysmart.com",
						"BBSmart Alarms Pro Support Query", "");

				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, mArgs);
			}
		});

		HorizontalFieldManager hfm2 = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm2.add(checkForUpdatesBtn);
		add(hfm2);

		add(new SpacerField(SpacerField.MODE_VERT, 10, Color.WHITE));

		HorizontalFieldManager hfm3 = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm3.add(contactUsBtn);
		add(hfm3);
	}

	public boolean onClose() {
		close();
		return true;
	}
}