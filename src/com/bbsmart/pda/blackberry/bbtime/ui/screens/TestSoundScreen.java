package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.HrefField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.StyledButtonField;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class TestSoundScreen extends PopupScreen {
	private int btnFontHeight;
	private int btnSize;
	
	public TestSoundScreen() {
		super(new VerticalFieldManager());
		
		setDisplayPrefs();

		HorizontalFieldManager hfm = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);

		StyledButtonField okBtn = new StyledButtonField("STOP SOUND", btnSize,
				0, getScreenFont());
		okBtn.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				close();
			}
		});

		// blank labelfield spacer
		add(new LabelField(""));

		hfm.add(okBtn);
		add(hfm);

		// blank labelfield spacer
		add(new LabelField(""));

		Font linkFont = getScreenFont().derive(Font.ITALIC,
				getScreenFont().getHeight() - 3, Ui.UNITS_px);
		HrefField noSoundLink = new HrefField("No sound? Click here!", false,
				linkFont);
		noSoundLink.setBackgroundColour(Color.LEMONCHIFFON);
		noSoundLink.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				BBSmartTimePro.instance
						.pushScreen(new PermissionsExplanationScreen());
			}
		});

		HorizontalFieldManager hfm2 = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_RIGHT);
		hfm2.add(noSoundLink);
		add(hfm2);
	}
	
	private void setDisplayPrefs() {
		if (UiUtilities.DEVICE_480W) {
			btnFontHeight = 22;
			btnSize = StyledButtonField.SIZE_LARGE;
		} else {
			btnFontHeight = 16;
			btnSize = StyledButtonField.SIZE_MID;
		}
	}

	private Font getScreenFont() {
		return getFont().derive(Font.BOLD, btnFontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0);
	}
}
