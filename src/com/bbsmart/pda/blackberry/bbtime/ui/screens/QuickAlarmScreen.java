package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import java.util.Calendar;

import com.bbsmart.pda.blackberry.bbtime.models.Alarm;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmList;
import com.bbsmart.pda.blackberry.bbtime.models.DefaultQuickAlarm;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.StyledButtonField;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class QuickAlarmScreen extends PopupScreen {
	private QuickAlarmTouchField quickAlarmTouchField;
	private QuickAlarmField quickAlarmField;

	public QuickAlarmScreen() {
		super(new VerticalFieldManager());
		
		if (UiUtilities.IS_TOUCH) {
			quickAlarmTouchField = new QuickAlarmTouchField(10);
			add(quickAlarmTouchField);
		} else {
			quickAlarmField = new QuickAlarmField(10);
			add(quickAlarmField);	
		}
		
		if (UiUtilities.IS_TOUCH) {
			HorizontalFieldManager hfm = new HorizontalFieldManager(
					HorizontalFieldManager.FIELD_HCENTER);
			hfm.setMargin(new XYEdges(20, 0, 0, 0));
			StyledButtonField okBtn = new StyledButtonField("OK",
					StyledButtonField.SIZE_LARGE, 0, getFont());
			okBtn.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					trackwheelClick(0, 0);
				}
			});
			hfm.add(okBtn);
			add(hfm);
		}
	}
	
	private int getMinutes() {
		if (UiUtilities.IS_TOUCH) {
			return quickAlarmTouchField.getMinutes();
		} else {
			return quickAlarmField.getMinutes();
		}
	}

	protected boolean trackwheelClick(int status, int time) {
		if (getMinutes() == -1)
			return true;
		
		// Set an alarm for the specified number of minutes
		Alarm alarm = new Alarm(DefaultQuickAlarm.getInstance());

		// Get the current time
		Calendar currTime = Calendar.getInstance();
		int minute = currTime.get(Calendar.MINUTE);
		int hour = currTime.get(Calendar.HOUR_OF_DAY);

		// add our minutes
		minute += getMinutes();
		while (minute > 59) {
			minute -= 60;
			hour++;
		}
		alarm.hour = hour;
		alarm.minute = minute;
		AlarmList.getInstance().alarmList.addElement(alarm);
		AlarmList.notifyAlarmsChanged();
		close();
		return true;
	}

	protected boolean keyCharUnhandled(char key, int status, int time) {
		if (key == Characters.ESCAPE) {
			close();
			return true;
		} else if (key == Keypad.KEY_ENTER) {
			trackwheelClick(status, time);
			return true;
		}
		return false;
	}

	private static class QuickAlarmTouchField extends BasicEditField {
		public QuickAlarmTouchField(int mins) {
			super("Set alarm in (mins): ", "", 2, BasicEditField.FILTER_NUMERIC);
		}

		public int getMinutes() {
			int minsValue = -1;

			try {
				minsValue = Integer.parseInt(this.getText());
			} catch (NumberFormatException nfe) {
				// handle silently
			}

			return minsValue;
		}
	}

	private static class QuickAlarmField extends Field {
		private int fieldWidth;
		private int fieldHeight;
		private int selectX;
		private int selectWidth;
		private int selectY;
		private int selectHeight;
		private int minutes;

		// Time in milliseconds (since device start) when the last key was pressed
		private int lastKeyPress;

		public QuickAlarmField(int mins) {
			minutes = mins;
			lastKeyPress = 0;
		}

		public int getMinutes() {
			return minutes;
		}

		protected void layout(int availWidth, int availHeight) {
			fieldHeight = Font.getDefault().getHeight() * 2;
			fieldWidth = availWidth;
			setExtent(fieldWidth, fieldHeight);
		}

		private void drawTextCentered(Graphics g, String text, int x, int y) {
			int textWidth = g.getFont().getAdvance(text);
			g.drawText(text, x - textWidth / 2, y);
		}

		protected void paint(Graphics graphics) {
			drawTextCentered(graphics, "Set alarm in", fieldWidth / 2, 0);

			String text = "" + minutes + " minute";
			if (minutes > 1) {
				text += "s";
			}
			drawTextCentered(graphics, text, fieldWidth / 2, graphics.getFont()
					.getHeight());

			selectX = fieldWidth / 2 - graphics.getFont().getAdvance(text) / 2;
			selectWidth = graphics.getFont().getAdvance("" + minutes);
			selectY = graphics.getFont().getHeight();
			selectHeight = graphics.getFont().getHeight();

		}

		public boolean isFocusable() {
			return true;
		}

		protected int moveFocus(int amount, int status, int time) {
			minutes += amount;
			if (minutes < 1) {
				minutes = 1;
			}
			if (minutes > 99) {
				minutes = 99;
			}
			return 0;
		}

		protected void drawFocus(Graphics graphics, boolean on) {
			drawHighlightRegion(graphics, Field.HIGHLIGHT_FOCUS, on, selectX,
					selectY, selectWidth, selectHeight);
			if (minutes == 1 || minutes == 9 || minutes == 10) {
				invalidate();
			}
		}

		protected boolean keyChar(char character, int status, int time) {
			int number = getNumber(character);

			if (number == -1) {
				lastKeyPress = 0; // force a reset
				return super.keyChar(character, status, time);
			}

			if (time - lastKeyPress > 1500) {
				// Started typing a new number
				minutes = number;
			} else {
				// adding a second digit to the current number
				minutes = (minutes * 10) + number;
			}

			if (minutes < 1) {
				minutes = 1;
			}

			if (minutes > 99) {
				minutes = 99;
			}

			lastKeyPress = time;
			invalidate();
			return true;
		}

		private int getNumber(char character) {
			if (UiUtilities.DEVICE_SURETYPE) {
				switch (character) {
				case 'e':
					return 1;
				case 't':
					return 2;
				case 'u':
					return 3;
				case 'd':
					return 4;
				case 'g':
					return 5;
				case 'j':
					return 6;
				case 'c':
					return 7;
				case 'b':
					return 8;
				case 'm':
					return 9;
				case Keypad.KEY_SPACE:
					return 0;
				default:
					return -1;
				}
			} else {
				switch (character) {
				case 'w':
					return 1;
				case 'e':
					return 2;
				case 'r':
					return 3;
				case 's':
					return 4;
				case 'd':
					return 5;
				case 'f':
					return 6;
				case 'z':
					return 7;
				case 'x':
					return 8;
				case 'c':
					return 9;
				case '0':
					return 0;
				default:
					return -1;
				}
			}
		}
	}
}
