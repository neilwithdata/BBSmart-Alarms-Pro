package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.models.*;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.DigitalClockField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.StyledButtonField;
import com.bbsmart.pda.blackberry.bbtime.util.AlarmNotificationThread;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.HolsterListener;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class AlarmNotificationScreen extends MainScreen implements
		HolsterListener {
	AlarmNotificationFieldManager manager;
	private DigitalClockField digitalClock;
	private StyledButtonField dismissButton;
	private StyledButtonField snoozeButton;
	private StyledButtonField hideButton;
	private VerticalFieldManager labelManager;
	private LabelField snoozeLabel;
	private LabelField snoozeTimeLabel;
	
	private StyledButtonField snoozeTimePicker;

	private HorizontalFieldManager dynamicSnoozeManager;

	private Alarm alarm;

	// The notification thread is in charge of continuously making sound and vibrating
	private AlarmNotificationThread notificationThread;
	
	// Device specific display prefs
	private int majorBtnFontHeight;
	private int majorBtnWidth;
	private int majorBtnSize;
	
	private int txtFontHeight;

	public AlarmNotificationScreen(Alarm alarm) {
		this.alarm = alarm;
		alarm.notificationScreen = this;
		alarm.alarmState = Alarm.STATE_RINGING;

		setupScreen();

		BBSmartTimePro.instance.addHolsterListener(this);
		notificationThread = new AlarmNotificationThread(alarm);

		startAlerts();
	}

	public void inHolster() {
		// placed in holster - do nothing
	}

	public void outOfHolster() {
		// removed from holster - stop alarm sound
		if (alarm.silentUnholster) {
			stopAlerts();
		}
	}
	
	private void setDisplayPrefs() {
		if (UiUtilities.DEVICE_480W) {
			txtFontHeight = 48;
			majorBtnWidth = 220;
			majorBtnSize = UiUtilities.IS_TOUCH ? StyledButtonField.SIZE_XLARGE
					: StyledButtonField.SIZE_LARGE;
			majorBtnFontHeight = 25;
		} else {
			txtFontHeight = 34;
			majorBtnWidth = 160;
			majorBtnSize = StyledButtonField.SIZE_MID;
			majorBtnFontHeight = 17;
		}
		
		setFont(getFont().derive(Font.BOLD, txtFontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));
	}

	private void setupScreen() {
		setDisplayPrefs();

		manager = new AlarmNotificationFieldManager();

		digitalClock = new DigitalClockField();
		manager.add(digitalClock);

		labelManager = new VerticalFieldManager(
				VerticalFieldManager.FIELD_HCENTER);
		labelManager.add(new LabelField(alarm.name, Field.FIELD_HCENTER));

		Font currentFont = getFont();
		Font dynamicSnoozeFont = currentFont.derive(currentFont.getStyle(),
				currentFont.getHeight() / 2);

		dynamicSnoozeManager = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);

		LabelField snoozeForLF = new LabelField("Snooze for ",
				LabelField.FIELD_VCENTER);
		snoozeForLF.setFont(dynamicSnoozeFont);

		LabelField minsLF = new LabelField(" min(s)", LabelField.FIELD_VCENTER);
		minsLF.setFont(dynamicSnoozeFont);

		snoozeTimePicker = new StyledButtonField(String
				.valueOf(alarm.snoozeLength), StyledButtonField.SIZE_MID, 50,
				dynamicSnoozeFont);
		snoozeTimePicker.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				changeSnoozeTime();
			}
		});

		dynamicSnoozeManager.add(snoozeForLF);
		dynamicSnoozeManager.add(snoozeTimePicker);
		dynamicSnoozeManager.add(minsLF);
		labelManager.add(dynamicSnoozeManager);

		manager.add(labelManager);

		snoozeButton = new StyledButtonField("Snooze", majorBtnSize,
				majorBtnWidth, getBigBtnFont());
		snoozeButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				snooze();
			}
		});
		manager.add(snoozeButton);

		dismissButton = new StyledButtonField("Dismiss", majorBtnSize,
				majorBtnWidth, getBigBtnFont());
		dismissButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				alarm.dismiss();
				close();
			}
		});
		manager.add(dismissButton);

		hideButton = new StyledButtonField("Hide", majorBtnSize, majorBtnWidth,
				getBigBtnFont());
		hideButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				// Hide the alarm - this actually closes the notification window
				hide();
			}
		});

		add(manager);
	}

	private void changeSnoozeTime() {
		String currSnooze = snoozeTimePicker.getText();

		int i;
		for (i = 0; i < Alarm.SNOOZE_OPTS.length; i++) {
			if (Alarm.SNOOZE_OPTS[i].equals(currSnooze)) {
				i = ++i % Alarm.SNOOZE_OPTS.length;
				break;
			}
		}

		snoozeTimePicker.setText(Alarm.SNOOZE_OPTS[i]);
	}

	private Font getBigBtnFont() {
		Font f;
		try {
			f = FontFamily.forName("BBSansSerif").getFont(Font.BOLD,
					majorBtnFontHeight);
			f = f.derive(f.getStyle(), f.getHeight(), Ui.UNITS_px,
					Font.ANTIALIAS_STANDARD, 0);
		} catch (ClassNotFoundException cnfe) {
			f = getFont().derive(Font.BOLD, majorBtnFontHeight, Ui.UNITS_px,
					Font.ANTIALIAS_STANDARD, 0);
		}

		return f;
	}

	private void startAlerts() {
		Backlight.enable(true);
		notificationThread.start();
	}

	private void stopAlerts() {
		notificationThread.stop();
	}

	private void snooze() {
		// snooze the alarm! stop the sound, stop vibrate
		stopAlerts();

		// change the buttons etc and add a snooze counter on the form
		snoozeLabel = new LabelField("Snoozed", Field.FIELD_HCENTER);
		snoozeLabel.setFont(getFont().derive(0, getFont().getHeight() - 10));
		labelManager.add(snoozeLabel);

		snoozeTimeLabel = new LabelField(alarm.getSnoozeTimeString(),
				Field.FIELD_HCENTER);
		labelManager.add(snoozeTimeLabel);
		
		// Delete the dynamic snooze time field
		labelManager.delete(dynamicSnoozeManager);

		// Replace the snooze Button with a hide button
		manager.delete(snoozeButton);
		manager.insert(hideButton, dismissButton.getIndex());
		hideButton.setFocus();

		int snoozeOverride = Integer.parseInt(snoozeTimePicker.getText());
		alarm.snooze(snoozeOverride);
	}

	public void updateSnoozeTimeLeft() {
		synchronized (UiApplication.getEventLock()) {
			try {
				snoozeTimeLabel.setText(alarm.getSnoozeTimeString());
			} catch (Exception e) {
				// Do nothing..just checking...
			}
		}
	}

	public void unsnooze() {
		synchronized (UiApplication.getEventLock()) {
			labelManager.delete(snoozeLabel);
			labelManager.delete(snoozeTimeLabel);
			
			labelManager.add(dynamicSnoozeManager);

			manager.delete(hideButton);
			manager.insert(snoozeButton, dismissButton.getIndex());
			snoozeButton.setFocus();
		}

		// Make sound happen! and vibrate! and backlight!
		startAlerts();
	}

	public void hide() {
		alarm.notificationScreen = null;
		AlarmList.notifyAlarmsChanged();
		close();
	}
	
	protected boolean keyDown(int keycode, int time) {
		if (alarm.alarmState == Alarm.STATE_SNOOZED) {
			hide();
		} else {
			snooze();
		}
		
		return true;
	}

	public void close() {
		stopAlerts();
		BBSmartTimePro.instance.removeHolsterListener(this);
		BBSmartTimePro.instance.popScreen(this);
	}
}

class AlarmNotificationFieldManager extends VerticalFieldManager {
	private Bitmap bgBitmap;

	public AlarmNotificationFieldManager() {
		super(HorizontalFieldManager.USE_ALL_HEIGHT
				| HorizontalFieldManager.USE_ALL_WIDTH);
		bgBitmap = UiUtilities.NOALARMS_BG;
	}

	protected void paint(Graphics graphics) {
		graphics.drawBitmap(0, 0, Graphics.getScreenWidth(), Graphics
				.getScreenHeight(), bgBitmap, 0, bgBitmap.getHeight()
				- Graphics.getScreenHeight());

		super.paint(graphics);
	}

	protected void sublayout(int maxWidth, int maxHeight) {
		if (getFieldCount() < 4) {
			return;
		}
		maxWidth = Graphics.getScreenWidth();
		maxHeight = Graphics.getScreenHeight();

		int yGap1, yGap2, yGap3, yGap4;
		
		if (UiUtilities.IS_TOUCH) {
			yGap1 = 20;
			yGap2 = 10;
			yGap3 = 20;
			yGap4 = 60;
		} else if (UiUtilities.DEVICE_480W) {
			yGap1 = 5;
			yGap2 = 10;
			yGap3 = 5;
			yGap4 = 5;
		} else {
			yGap1 = 5;
			yGap2 = 5;
			yGap3 = 5;
			yGap4 = 5;
		}
		
		// First field to layout is the clock field
		int currY = yGap1;
		Field clock = getField(0);
		setPositionChild(clock, 0, currY);
		layoutChild(clock, maxWidth, maxHeight);
		
		currY += clock.getPreferredHeight() + yGap2;
		
		// Position the labels (and snooze configuration options)
		Field labels = getField(1);
		int labelsWidth = labels.getPreferredWidth();
		setPositionChild(labels, (maxWidth - labelsWidth) / 2, currY);
		layoutChild(labels, maxWidth, maxHeight);
		
		currY += labels.getPreferredHeight() + yGap3;

		// Position the first button, horizontally centered
		Field button1 = getField(2);
		int button1width = button1.getPreferredWidth();
		setPositionChild(button1, (maxWidth - button1width) / 2, currY);
		layoutChild(button1, maxWidth, maxHeight);
		
		currY += button1.getPreferredHeight() + yGap3;

		// Position the second button, horizontally centered
		Field button2 = getField(3);
		int button2width = button2.getPreferredWidth();
		setPositionChild(button2, (maxWidth - button2width) / 2, currY);
		layoutChild(button2, maxWidth, maxHeight);

		setExtent(Graphics.getScreenWidth(), Graphics.getScreenHeight());
	}
}
