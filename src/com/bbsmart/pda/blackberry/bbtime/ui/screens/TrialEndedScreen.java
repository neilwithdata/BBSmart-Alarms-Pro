package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.ui.fields.ColorLabelField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.HrefField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.NowHeadingField;
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
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public class TrialEndedScreen extends MainScreen {
	private String buyNowURL = "https://www.mobihand.com/mobilecart/mc1.asp?posid=16&pid=19069&did="
			+ Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase();

	private ButtonField submitFeedbackBtn;
	
	private int fontHeight;

	public TrialEndedScreen() {
		setDisplayPrefs();
		setScreenFont();
		initButtons();
		initDisplay();
	}
	
	private void setDisplayPrefs() {
		if (UiUtilities.DEVICE_480W) {
			fontHeight = 25;
		} else {
			fontHeight = 17;
		}

		setScreenFont();
	}

	private void setScreenFont() {
		setFont(getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));
	}

	private void initButtons() {
		submitFeedbackBtn = new ButtonField("Submit Feedback",
				ButtonField.NEVER_DIRTY | ButtonField.CONSUME_CLICK
						| ButtonField.FIELD_HCENTER);
		submitFeedbackBtn.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				MessageArguments mArgs = new MessageArguments(
						MessageArguments.ARG_NEW,
						"support@blackberrysmart.com",
						"BBSmart Alarms Pro Feedback", "");

				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, mArgs);
			}
		});
	}

	private void initDisplay() {
		add(new NowHeadingField("Trial Ended!", false, UiUtilities.ALERT_SMALL));
		displayText();
	}

	private void displayText() {
		heading("> THANKS FOR STOPPING BY", true);
		text("Your free trial of BBSmart Alarms Pro, the most fully featured BlackBerry multi-alarm program, has now expired!\n");

		heading("> GET THE FULL VERSION", true);
		text("If you enjoyed using this application and now dread going back to the simple native alarm program, there is another way!\n");

		heading(">> From your PC", false);
		text("From your home PC, head on over to the BBSmart website and pick up a copy.");
		link("www.blackberrysmart.com", "http://www.blackberrysmart.com");

		spacer();

		heading(">> From your BlackBerry", false);
		text("Alternatively you can buy online right now from our secure mobile-friendly store by clicking the \"Buy Now\" link below.");
		link("Buy Now", buyNowURL);

		spacer();

		heading("> WHAT DID YOU THINK?", true);
		text("Got some feedback you would like to give? Loved it? Hated it? Got a cool idea to make it better? We'd love to hear from you!");
		add(submitFeedbackBtn);
	}

	private void heading(String headingText, boolean major) {
		add(new ColorLabelField(headingText, major ? Color.BLUE : Color.GREEN));
	}

	private void text(String text) {
		add(new BasicEditField("", text.toString(),
				BasicEditField.DEFAULT_MAXCHARS, BasicEditField.READONLY));
	}

	private void link(String show, final String url) {
		HrefField siteLink = new HrefField(show, false, getFont());
		siteLink.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Browser.getDefaultSession().displayPage(url);
			}
		});

		HorizontalFieldManager hfm = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm.add(siteLink);
		add(hfm);
	}

	private void spacer() {
		add(new LabelField("", LabelField.READONLY));
	}
}