package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.models.Alarm;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmList;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.AlarmColorPickerField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.AlarmSoundFileRepeatsField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.AlarmSoundFilenameField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.HrefField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.NowHeadingField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.SpacerField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.StyledButtonField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.WeekdayField;
import com.bbsmart.pda.blackberry.bbtime.util.AlarmSoundHelper;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;
import com.bbsmart.pda.blackberry.bbtime.models.*;

public class AlarmDetailScreen extends MainScreen {

	private BasicEditField nameField;
	private CheckboxField enabledField;
	private DateField timeOfDayField;
	private CheckboxField recurringField;
	private WeekdayField daysOfWeekField;

	private HorizontalFieldManager linkHFM;
	private HrefField preloadSoundLink;
	private HrefField fromFileSoundLink;

	private AlarmSoundFilenameField soundFileNameField;
	private ObjectChoiceField soundPreField;
	
	private AlarmSoundFileRepeatsField soundRepeatsField;
	private NumericChoiceField soundVolumeField;
	private StyledButtonField soundTestField;

	private CheckboxField vibrateField;
	private CheckboxField silentUnholsterField;
	private ObjectChoiceField snoozeLengthField;
	
	private AlarmColorPickerField colorPickField;
	
	private Alarm alarm;

	private int mode;
	public static final int MODE_NEW = 0;
	public static final int MODE_EDIT = 1;
	public static final int MODE_EDIT_DEFAULTS = 2;
	public static final int MODE_EDIT_QUICK_DEFAULTS = 3;

	// Passing an alarm and a mode will let you:
	// - edit a new alarm - MODE_NEW
	// - edit an existing alarm - MODE_EDIT
	// - edit the alarm defaults - MODE_EDIT_DEFAULTS
	// - edit the quick defaults - MODE_EDIT_QUICK_DEFAULTS
	public AlarmDetailScreen(Alarm alarm, int mode) {
		if (mode > MODE_EDIT_QUICK_DEFAULTS) {
			mode = MODE_EDIT;
		}
		this.mode = mode;
		this.alarm = alarm;
		setupScreen();
	}

	private void setScreenFont() {
		setFont(getFont().derive(Font.BOLD, UiUtilities.DEVICE_480W ? 25 : 17,
				Ui.UNITS_px, Font.ANTIALIAS_STANDARD, 0));
	}

	private boolean isNormalAlarm() {
		return (mode == MODE_NEW || mode == MODE_EDIT);
	}

	private void setupScreen() {
		setScreenFont();
		displayHeading();

		if (mode == MODE_EDIT) {
			displayStatus();
		}

		displayTitle();
		displayTime();
		displayAlarmColorSettings();
		displaySounds();
		displaySettings();
		
		if (UiUtilities.IS_TOUCH) {
			try {
				timeOfDayField.setFocus();
			} catch (Exception e) {
				recurringField.setFocus();
			}
		}
	}

	private void displayStatus() {
		addBlackHeading("Status:");
		enabledField = new CheckboxField("Enabled", alarm.enabled);
		add(enabledField);
	}
	
	private void displayAlarmColorSettings() {
		addBlackHeading("Alarm Color:");
		
		colorPickField = new AlarmColorPickerField(alarm.alarmColorIndx); 
		add(colorPickField);
	}

	private void displayTitle() {
		addBlackHeading("Title:");

		nameField = new BasicEditField("", alarm.name,
				UiUtilities.IS_TOUCH ? 14 : BasicEditField.DEFAULT_MAXCHARS, 0);
		
		// name field should use bigger font for better visibility
		Font f = getFont();
		nameField.setFont(f.derive(f.getStyle(), f.getHeight() + 4));
		
		add(nameField);
	}

	private void displayTime() {
		addBlackHeading("Time:");

		Calendar alarmTime = Calendar.getInstance();
		alarmTime.set(Calendar.HOUR_OF_DAY, alarm.hour);
		alarmTime.set(Calendar.MINUTE, alarm.minute);

		timeOfDayField = new DateField("", alarmTime.getTime().getTime(),
				DateFormat.getInstance(DateFormat.TIME_DEFAULT), DateField.TIME
						| DrawStyle.HCENTER);
		
		// time field should use bigger font for better visibility
		Font f = getFont();
		timeOfDayField.setFont(f.derive(f.getStyle(), f.getHeight() + 7));

		if (isNormalAlarm()) {
			add(timeOfDayField);
		}

		recurringField = new CheckboxField("Recurring", alarm.recurring);
		recurringField.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (recurringField.getChecked()) {
					displayDaysofWeek(true);
				} else {
					displayDaysofWeek(false);
				}
			}
		});

		add(recurringField);

		daysOfWeekField = new WeekdayField(alarm.daysOfWeek);
		if (recurringField.getChecked()) {
			displayDaysofWeek(true);
		}
	}

	private void displaySounds() {
		initPreFields();
		initFileFields();
		initSoundLinks();

		addBlackHeading("Alarm Sound:");

		linkHFM = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);

		linkHFM.add(preloadSoundLink);
		linkHFM.add(new SpacerField(SpacerField.MODE_HORIZ, 10, Color.WHITE));
		linkHFM.add(fromFileSoundLink);

		add(linkHFM);

		switch (alarm.soundType) {
		case Alarm.SOUND_FILE:
			fromFileSoundLink.setSelected(true);
			displaySoundFileField(true);
			break;
		case Alarm.SOUND_PRE:
			preloadSoundLink.setSelected(true);
			displaySoundPreField(true);
			break;
		}

		add(soundRepeatsField);
		add(soundVolumeField);

		HorizontalFieldManager hfm1 = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm1.add(soundTestField);
		add(hfm1);
	}

	private void initPreFields() {
		soundPreField = new ObjectChoiceField("Sound", Alarm.PRE_NAMES,
				alarm.soundPreIndx);
	}

	private void initFileFields() {
		soundFileNameField = new AlarmSoundFilenameField(alarm.soundFileName);
		soundRepeatsField = new AlarmSoundFileRepeatsField(alarm.soundRepeats);
		soundVolumeField = NumericChoice("Volume", 0, 100, 10,
				alarm.soundVolume);

		int btnSize = UiUtilities.DEVICE_480W ? StyledButtonField.SIZE_LARGE
				: StyledButtonField.SIZE_MID;

		soundTestField = new StyledButtonField("Test Alarm Sound", btnSize, 0,
				getFont());
		soundTestField.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				testAlarmSound();
			}
		});
	}

	private void initSoundLinks() {
		preloadSoundLink = new HrefField("Preloaded", true, getFont());
		preloadSoundLink.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (preloadSoundLink.isSelected()) {
					fromFileSoundLink.setSelected(false);

					displaySoundFileField(false);
					displaySoundPreField(true);
				}
			}
		});

		fromFileSoundLink = new HrefField("From File", true, getFont());
		fromFileSoundLink.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (fromFileSoundLink.isSelected()) {
					preloadSoundLink.setSelected(false);

					displaySoundPreField(false);
					displaySoundFileField(true);
				}
			}
		});
	}

	private void displaySettings() {
		addBlackHeading("Alarm Settings:");

		vibrateField = new CheckboxField("Vibrate", alarm.vibrate);
		add(vibrateField);

		silentUnholsterField = new CheckboxField("Silence When Unholstered",
				alarm.silentUnholster);
		add(silentUnholsterField);

		snoozeLengthField = new ObjectChoiceField("Snooze Length (m)",
				Alarm.SNOOZE_OPTS, String.valueOf(alarm.snoozeLength));
		add(snoozeLengthField);
	}
	
	private void displayHeading() {
		String title;
		switch (mode) {
		case MODE_NEW:
			title = "New Alarm";
			break;
		default:
		case MODE_EDIT:
			title = "Edit Alarm";
			break;
		case MODE_EDIT_DEFAULTS:
			title = "Defaults";
			break;
		case MODE_EDIT_QUICK_DEFAULTS:
			title = "Quick Defaults";
			break;
		}

		add(new NowHeadingField(title, true, UiUtilities.ALARMBELL));
	}

	private NumericChoiceField NumericChoice(String label, int begin, int end,
			int increment, int initialValue) {
		if (initialValue < begin)
			initialValue = begin;
		if (initialValue > end)
			initialValue = end;
		int initialIndex = (initialValue - begin) / increment;
		return new NumericChoiceField(label, begin, end, increment,
				initialIndex);
	}

	private void displayDaysofWeek(boolean display) {
		toggleField(daysOfWeekField, recurringField.getIndex() + 1, display);
	}

	private void displaySoundFileField(boolean display) {
		toggleField(soundFileNameField, linkHFM.getIndex() + 1, display);
	}

	private void displaySoundPreField(boolean display) {
		toggleField(soundPreField, linkHFM.getIndex() + 1, display);
	}

	private void toggleField(Field field, int index, boolean display) {
		try {
			if (display) {
				insert(field, index);
			} else {
				delete(field);
			}
		} catch (Exception e) {
			// silently ignore
		}
	}

	private void testAlarmSound() {
		alarm.soundType = preloadSoundLink.isSelected() ? Alarm.SOUND_PRE
				: Alarm.SOUND_FILE;
		alarm.soundFileName = soundFileNameField.getFilename();
		alarm.soundPreIndx = soundPreField.getSelectedIndex();
		alarm.soundRepeats = soundRepeatsField.getValue();
		alarm.soundVolume = soundVolumeField.getSelectedValue();

		AlarmSoundHelper alarmTone = new AlarmSoundHelper(alarm);
		alarmTone.startAlarmSound();
		BBSmartTimePro.instance.pushModalScreen(new TestSoundScreen());
		alarmTone.stopAlarmSound();
	}

	public boolean onClose() {
		if (isDirty()) {
			try {
				if (AlarmPreferences.getInstance().showSavingDialog) {
					displaySavingDialog();
				}

				save();
			} catch (Exception e) {
				// Handle silently
			}
		}

		close();
		return true;
	}
	
	private void displaySavingDialog() {
		final PopupScreen popup = new PopupScreen(new VerticalFieldManager());
		popup.add(new LabelField("Saving..."));

		// In one second, then close the popup screen
		BBSmartTimePro.instance.invokeLater(new Runnable() {
			public void run() {
				BBSmartTimePro.instance.popScreen(popup);
			}
		}, 500, false);

		// Launch the "Saving..." popup screen
		BBSmartTimePro.instance.pushModalScreen(popup);
	}
	
	public void save() throws IOException {
		// Update all the fields of the alarm
		alarm.name = nameField.getText();

		// New alarms are always enabled
		alarm.enabled = (mode == MODE_EDIT) ? enabledField.getChecked() : true;

		Calendar alarmTime = Calendar.getInstance();
		alarmTime.setTime(new Date(timeOfDayField.getDate()));
		alarm.hour = alarmTime.get(Calendar.HOUR_OF_DAY);
		alarm.minute = alarmTime.get(Calendar.MINUTE);

		alarm.recurring = recurringField.getChecked();
		alarm.daysOfWeek = daysOfWeekField.getDays();

		if (preloadSoundLink.isSelected()) {
			alarm.soundType = Alarm.SOUND_PRE;
			alarm.soundPreIndx = soundPreField.getSelectedIndex();
		} else {
			alarm.soundType = Alarm.SOUND_FILE;
			alarm.soundFileName = soundFileNameField.getFilename();
		}
		
		alarm.soundRepeats = soundRepeatsField.getValue();
		alarm.soundVolume = soundVolumeField.getSelectedValue();

		alarm.vibrate = vibrateField.getChecked();
		alarm.silentUnholster = silentUnholsterField.getChecked();
		alarm.snoozeLength = Integer.parseInt((String) snoozeLengthField
				.getChoice(snoozeLengthField.getSelectedIndex()));
		
		alarm.alarmColorIndx = colorPickField.getSelectedColorIndx();

		// if this is a new alarm we have to add it to the list
		if (mode == MODE_NEW) {
			Vector allAlarms = AlarmList.getInstance().alarmList;
			if (!allAlarms.contains(alarm)) {
				allAlarms.addElement(alarm);
				AlarmList.getInstance().save();
			}
		}

		// only notify of changes to alarms stored in the alarm list!
		if (isNormalAlarm()) {
			// Let everyone know an alarm has changed
			AlarmList.notifyAlarmsChanged();
		}
	}

	private void addBlackHeading(String text) {
		Bitmap b = UiUtilities
				.get(UiUtilities.DEVICE_480W ? "black-heading-hr.png"
						: "black-heading.png");

		Graphics g = new Graphics(b);

		g.setFont(getFont());
		g.setColor(Color.WHITE);
		g.drawText(text, 2, 2);

		add(new BitmapField(b));
	}
}