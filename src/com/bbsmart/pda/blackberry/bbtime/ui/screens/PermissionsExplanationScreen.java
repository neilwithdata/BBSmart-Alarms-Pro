package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.ui.fields.ColorLabelField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.NowHeadingField;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.container.MainScreen;

public class PermissionsExplanationScreen extends MainScreen {
	public PermissionsExplanationScreen() {
		setScreenFont();
		initDisplay();
	}

	private void setScreenFont() {
		int fontHeight = UiUtilities.DEVICE_480W ? 25 : 17;
		setFont(getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));
	}

	private void initDisplay() {
		add(new NowHeadingField("No Alarm Sound?", false,
				UiUtilities.ALERT_SMALL));
		displayText();
	}

	private void displayText() {
		heading("> HOW TO FIX IT...", true);
		text("One reason you may not hear the alarm sound is if your BlackBerry permissions are configured to block third-party applications from accessing this functionality.\n");
		text("To correct this you need to enable all application permissions for this application.\n");
		text("To do this, from the BlackBerry Home Screen go to Options - Advanced Options - Applications and scroll down and highlight \"BBSmart Alarms Pro\". Select \"Edit Permissions\" from the menu and for all 3 permissions (Connections, Interactions, User Data) set the permission to \"Allow\".");
	}

	private void heading(String headingText, boolean major) {
		add(new ColorLabelField(headingText, major ? Color.BLUE : Color.GREEN));
	}

	private void text(String text) {
		add(new BasicEditField("", text.toString(),
				BasicEditField.DEFAULT_MAXCHARS, BasicEditField.READONLY));
	}
}