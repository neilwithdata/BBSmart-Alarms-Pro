package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmPreferences;
import com.bbsmart.pda.blackberry.bbtime.models.DefaultAlarm;
import com.bbsmart.pda.blackberry.bbtime.models.DefaultQuickAlarm;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.NowHeadingField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.SpacerField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.StyledButtonField;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public class AlarmOptionsScreen extends MainScreen {
	private CheckboxField promptOnAlarmDeleteField;
	private CheckboxField showSavingAlarmsCB;
	private CheckboxField displayBannerIconCB;
	private StyledButtonField normalAlarmDefaultsButton;
	private StyledButtonField quickAlarmDefaultsButton;

	private AlarmPreferences alarmPrefs;
	
	private int btnSize;
	private int btnLen;
	private int fontHeight;

	public AlarmOptionsScreen() {
		setDisplayPrefs();
		setScreenFont();
		add(new NowHeadingField("Alarm Options", false, UiUtilities.OPTIONS));

		alarmPrefs = AlarmPreferences.getInstance();

		promptOnAlarmDeleteField = new CheckboxField("Prompt on alarm delete",
				alarmPrefs.promptOnAlarmDelete);

		showSavingAlarmsCB = new CheckboxField(
				"Show saving dialog when alarms change",
				alarmPrefs.showSavingDialog);
		
		displayBannerIconCB = new CheckboxField("Display banner icon",
				alarmPrefs.displayBannerIcon);

		normalAlarmDefaultsButton = new StyledButtonField(
				"Regular Alarm Defaults", btnSize, btnLen, getFont());
		normalAlarmDefaultsButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				editNormalAlarmDefaults();
			}
		});

		quickAlarmDefaultsButton = new StyledButtonField(
				"Quick Alarm Defaults", btnSize, btnLen, getFont());
		quickAlarmDefaultsButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				editQuickAlarmDefaults();
			}
		});

		add(promptOnAlarmDeleteField);
		add(showSavingAlarmsCB);
		add(displayBannerIconCB);
		add(new SeparatorField());

		add(new SpacerField(SpacerField.MODE_VERT, 10, Color.WHITE));

		HorizontalFieldManager hfm1 = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm1.add(normalAlarmDefaultsButton);
		add(hfm1);

		add(new SpacerField(SpacerField.MODE_VERT, 10, Color.WHITE));

		HorizontalFieldManager hfm2 = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm2.add(quickAlarmDefaultsButton);
		add(hfm2);
	}
	
	private void setDisplayPrefs() {
		if (UiUtilities.DEVICE_480W) {
			fontHeight = 25;
			btnSize = StyledButtonField.SIZE_LARGE;
			btnLen = 300;
		} else {
			fontHeight = 17;
			btnSize = StyledButtonField.SIZE_MID;
			btnLen = 200;
		}
	}

	private void setScreenFont() {
		setFont(getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));
	}

	private void editNormalAlarmDefaults() {
		DefaultAlarm defaultAlarm = DefaultAlarm.getInstance();
		BBSmartTimePro.instance.pushModalScreen(new AlarmDetailScreen(
				defaultAlarm, AlarmDetailScreen.MODE_EDIT_DEFAULTS));
		defaultAlarm.save();
	}

	private void editQuickAlarmDefaults() {
		DefaultQuickAlarm defaultAlarm = DefaultQuickAlarm.getInstance();
		BBSmartTimePro.instance.pushModalScreen(new AlarmDetailScreen(
				defaultAlarm, AlarmDetailScreen.MODE_EDIT_QUICK_DEFAULTS));
		defaultAlarm.save();
	}

	public boolean onClose() {
		alarmPrefs.promptOnAlarmDelete = promptOnAlarmDeleteField.getChecked();
		alarmPrefs.showSavingDialog = showSavingAlarmsCB.getChecked();
		alarmPrefs.displayBannerIcon = displayBannerIconCB.getChecked();
		
		// Refresh banner icon if pref changed
		if (displayBannerIconCB.isDirty()) {
			UiUtilities.updateIcon();
		}
		
		alarmPrefs.save();

		close();
		return true;
	}
}