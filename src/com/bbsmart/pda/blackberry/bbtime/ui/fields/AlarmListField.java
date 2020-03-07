package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.models.*;
import com.bbsmart.pda.blackberry.bbtime.ui.screens.AlarmDetailScreen;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;

public class AlarmListField extends ListField {
	Vector theAlarmList;
	Vector listElements;

	public AlarmListField(Vector alarmList) {
		setDisplayPrefs();

		this.setCallback(new AlarmListCallback());

		theAlarmList = alarmList;
		updateAlarms();
	}

	private void setDisplayPrefs() {
		setFont(getFont().derive(Font.BOLD, UiUtilities.DEVICE_480W ? 22 : 17,
				Ui.UNITS_px, Font.ANTIALIAS_STANDARD, 0));

		setRowHeight(UiUtilities.DEVICE_480W ? 55 : 36);
	}

	public void updateAlarms() {
		SimpleSortingVector sortedList = new SimpleSortingVector();

		// Sort the elements from the unsortedList and put them into the
		// listElements vector
		// one time alarms should come first, in the order which they will be
		// activated - we need to find out what time it is!
		// following that, the recurring alarms should go in order of HH:MM

		sortedList.setSortComparator(new Comparator() {
			public int compare(Object o1, Object o2) {
				Alarm a1 = (Alarm) o1;
				Alarm a2 = (Alarm) o2;

				// One off alarms are always lower than recurring alarms
				if (a1.recurring != a2.recurring) {
					if (a1.recurring) {
						return 1;
					} else {
						return -1;
					}
				} else {
					if (a1.recurring) {
						// sort these alarms simply by their time of day
						if (a1.hour < a2.hour)
							return -1;
						if (a1.hour > a2.hour)
							return 1;

						if (a1.minute < a2.minute)
							return -1;
						if (a1.minute > a2.minute)
							return 1;
						return 0;
					} else {
						// Sort based on how long until they are active
						// the alarms at the top of the list should be those
						// that will be activated soonest

						Calendar cal = Calendar.getInstance();
						int currHour = cal.get(Calendar.HOUR_OF_DAY);
						int currMinute = cal.get(Calendar.MINUTE);

						int minutesFromNow1 = 0;
						int minutesFromNow2 = 0;

						minutesFromNow1 = (a1.hour * 60 + a1.minute)
								- (currHour * 60 + currMinute);
						minutesFromNow2 = (a2.hour * 60 + a2.minute)
								- (currHour * 60 + currMinute);

						if (minutesFromNow1 < 0)
							minutesFromNow1 += 24 * 60;
						if (minutesFromNow2 < 0)
							minutesFromNow2 += 24 * 60;

						if (minutesFromNow1 < minutesFromNow2)
							return -1;
						if (minutesFromNow1 > minutesFromNow2)
							return 1;
						return 0;
					}
				}
			}
		});

		for (Enumeration e = theAlarmList.elements(); e.hasMoreElements();) {
			sortedList.addElement(e.nextElement());
		}

		sortedList.reSort();
		listElements = sortedList;
		super.setSize(listElements.size());
	}

	public Alarm getSelectedAlarm() {
		int selectedIndx = getSelectedIndex();
		return (selectedIndx == -1) ? null : (Alarm) listElements
				.elementAt(selectedIndx);
	}

	private MenuItem editMenuItem = new MenuItem("Edit", 0, 0) {
		public void run() {
			BBSmartTimePro.instance.pushScreen(new AlarmDetailScreen(
					getSelectedAlarm(), AlarmDetailScreen.MODE_EDIT));
		}
	};

	private MenuItem delMenuItem = new MenuItem("Delete", 0, 0) {
		public void run() {
			Alarm selectedAlarm = getSelectedAlarm();

			if (AlarmPreferences.getInstance().promptOnAlarmDelete
					&& Dialog.ask(Dialog.D_YES_NO, "Delete alarm \""
							+ selectedAlarm.name + "\"?", Dialog.YES) != Dialog.YES) {
				return;
			}

			AlarmList.getInstance().alarmList.removeElement(getSelectedAlarm());
			AlarmList.notifyAlarmsChanged();
		}
	};

	private MenuItem dismissMenuItem = new MenuItem("Dismiss Alarm", 0, 0) {
		public void run() {
			getSelectedAlarm().dismiss();
			AlarmList.notifyAlarmsChanged();
		}
	};

	protected void makeContextMenu(ContextMenu contextMenu) {
		if (getSelectedAlarm().alarmState == Alarm.STATE_SNOOZED) {
			contextMenu.addItem(dismissMenuItem);
		} else {
			contextMenu.addItem(editMenuItem);
			contextMenu.addItem(delMenuItem);
		}
	}

	protected boolean keyChar(char key, int status, int time) {
		if (getSelectedAlarm() == null) {
			return true; // nothing to do here
		}

		if (getSelectedAlarm().alarmState == Alarm.STATE_SNOOZED) {
			return super.keyChar(key, status, time); // no keyboard shortcuts
														// for snoozed alarms
		}

		switch (key) {
		case Keypad.KEY_DELETE:
		case Keypad.KEY_BACKSPACE:
			delMenuItem.run();
			return true;
		case Keypad.KEY_SPACE:
			editMenuItem.run();
			return true;
		}

		return super.keyChar(key, status, time);
	}

	protected boolean trackwheelClick(int status, int time) {
		if (UiUtilities.HAS_TRACKBALL) {
			if (getSelectedAlarm().alarmState == Alarm.STATE_SNOOZED) {
				Menu menu = new Menu();
				menu.add(dismissMenuItem);
				menu.show();
			} else {
				editMenuItem.run();
			}
			return true;
		}

		return super.trackwheelClick(status, time);
	}

	// Handles repainting the listfield (only way to ensure highlighted row is
	// colored correctly)
	protected int moveFocus(int amount, int status, int time) {
		invalidate(getSelectedIndex());
		return super.moveFocus(amount, status, time);
	}

	private class AlarmListCallback implements ListFieldCallback {
		private Bitmap crossBitmap;
		private Bitmap tickBitmap;

		public AlarmListCallback() {
			crossBitmap = UiUtilities
					.get(UiUtilities.DEVICE_480W ? "notifier-cross-hr.png"
							: "notifier-cross.png");

			tickBitmap = UiUtilities
					.get(UiUtilities.DEVICE_480W ? "notifier-tick-hr.png"
							: "notifier-tick.png");
		}

		private String getAlarmTimeString(Alarm alarm) {
			if (alarm.alarmState == Alarm.STATE_SNOOZED) {
				return "Snoozed";
			} else {
				Calendar alarmTime = Calendar.getInstance();
				alarmTime.set(Calendar.HOUR_OF_DAY, alarm.hour);
				alarmTime.set(Calendar.MINUTE, alarm.minute);

				return DateFormat.getInstance(DateFormat.TIME_DEFAULT)
						.formatLocal(alarmTime.getTime().getTime());
			}
		}

		public void drawListRow(ListField list, Graphics g, int index, int y,
				int w) {
			Alarm alarm = (Alarm) listElements.elementAt(index);

			Bitmap bg = UiUtilities.ALARM_BG_COLORS[alarm.alarmColorIndx];

			g.drawBitmap(0, y, bg.getWidth(), bg.getHeight(), bg, 0, 0);

			g.setColor((index == getSelectedIndex()) ? Color.WHITE
					: Color.BLACK);

			g.setGlobalAlpha(180);
			Bitmap notifier = alarm.enabled ? tickBitmap : crossBitmap;
			g.drawBitmap(0, y + 2, notifier.getWidth(), notifier.getHeight(),
					notifier, 0, 0);
			g.setGlobalAlpha(255);

			final int horizSpace = notifier.getWidth() + 7;
			final int vertSpace = UiUtilities.DEVICE_480W ? 6 : 2;

			String text = alarm.name;
			g.drawText(text, horizSpace, y + vertSpace, DrawStyle.ELLIPSIS, w);

			int secondRow = y + g.getFont().getHeight() + vertSpace;

			if (alarm.recurring) {
				// lets draw the SMTWTFS indicators, left justified
				char days[] = { 'S', 'M', 'T', 'W', 'T', 'F', 'S' };
				int i;

				int xPos = horizSpace;
				for (i = 0; i < 7; i++) {
					if ((alarm.daysOfWeek & (1 << i)) != 0) { // enabled
						g.setGlobalAlpha(255);
					} else { // disabled
						g.setGlobalAlpha(128);
					}

					xPos += g.drawText(days[i], xPos, secondRow, 0, w);
					xPos += 6;
				}
			}

			// Return alpha to default
			g.setGlobalAlpha(255);

			String time = getAlarmTimeString(alarm);
			int textWidth = g.getFont().getAdvance(time);
			int xpos = w - textWidth;
			g.drawText(time, xpos, secondRow);
		}

		public Object get(ListField list, int index) {
			return listElements.elementAt(index);
		}

		public int indexOfList(ListField list, String prefix, int start) {
			return -1;
		}

		public int getPreferredWidth(ListField list) {
			return Graphics.getScreenWidth();
		}
	}
}
