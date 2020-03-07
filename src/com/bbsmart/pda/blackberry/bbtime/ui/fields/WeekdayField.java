//#preprocess
package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import java.lang.String;

import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;

public class WeekdayField extends Field {
	private int days;
	private int focusDay;
	private int[] labelXCoords;
	private int[] labelXWidths;
	private int fieldWidth;
	private int fieldHeight;
	
	private boolean displayDaysTxt;
	private int letterSeperation;

	public WeekdayField(int days) {
		this.days = days;
		labelXCoords = new int[7];
		labelXWidths = new int[7];

		setDisplayPrefs();
	}

	public int getDays() {
		return days;
	}
	
	//#ifdef 4.7
	protected boolean touchEvent(net.rim.device.api.ui.TouchEvent message) {
		if (message.getEvent() == net.rim.device.api.ui.TouchEvent.CANCEL) {
			return false;
		}
		
		switch (message.getEvent()) {
		case net.rim.device.api.ui.TouchEvent.CLICK:
			touchToggleFocus(message.getX(1), message.getY(1));
			return true;
		}

		return super.touchEvent(message);
	}
	
	private void touchToggleFocus(int x, int y) {
		int xTouch = Display.getWidth() / 7;
		focusDay = x / xTouch;
		toggleFocusedDay();
	}
	//#endif

	private void setDisplayPrefs() {
		if (UiUtilities.IS_TOUCH) {
			displayDaysTxt = false;
			setFont(getFont().derive(0, getFont().getHeight() + 10));
			letterSeperation = 33;
		} else {
			displayDaysTxt = true;
			letterSeperation = 2;
		}
	}

	public void layout(int availWidth, int availHeight) {
		fieldWidth = availWidth;
		fieldHeight = getFont().getHeight() + 4;
		setExtent(fieldWidth, fieldHeight);
	}

	public void paint(Graphics g) {
		String labels[] = { "S", "M", "T", "W", "T", "F", "S" };
		int rightCoord = fieldWidth - 2;
		Font normalFont = g.getFont();

		if (displayDaysTxt) {
			g.drawText("Days", 0, 2);
		}

		boolean selected = false;
		for (int j = 0; j < 7; j++) {
			int i = 6 - j;
			if ((days & (1 << i)) > 0) {
				selected = true;
				g.setColor(Color.GREEN);
				g.setFont(normalFont.derive(Font.BOLD));
				g.setGlobalAlpha(255);
			} else {
				selected = false;
				g.setColor(Color.RED);
				g.setFont(normalFont);
				g.setGlobalAlpha(80);
			}
			
			if (UiUtilities.IS_TOUCH) {
				drawDayLetter(g, i, labels[i], 2, selected);				
			} else {
				rightCoord = drawTextRightAligned(g, labels[i], rightCoord, 2,
						selected, i);
				rightCoord -= letterSeperation;
			}
		}

		g.setFont(normalFont);
		g.setGlobalAlpha(255);
	}
	
	private void drawDayLetter(Graphics g, int focusDayIndx, String text,
			int y, boolean selected) {
		int leftX = focusDayIndx * 51;

		int textWidth = g.getFont().getAdvance(text);
		g.drawText(text, leftX + (51 - textWidth) / 2, y);

		if (selected) {
			g.drawRect(leftX, y, (focusDayIndx == 6) ? 54 : 51, fieldHeight);
		}
	}
	
	// Returns the left co-ord of the drawn text
	private int drawTextRightAligned(Graphics g, String text, int x, int y,
			boolean selected, int focusDayIndx) {
		int textWidth = g.getFont().getAdvance(text);
		g.drawText(text, x - textWidth, y);

		if (selected) {
			g.drawRect(x - textWidth - 2, 0, textWidth + 4, fieldHeight);
		}

		labelXCoords[focusDayIndx] = x - textWidth;
		labelXWidths[focusDayIndx] = textWidth;

		return x - textWidth - 4;
	}
	
	public boolean isFocusable() {
		return !UiUtilities.IS_TOUCH;
	}
	
	protected void onFocus(int direction) {
		if (!UiUtilities.IS_TOUCH) {
			if (direction >= 0) {
				// Focus the first day
				focusDay = 0;
			} else {
				// focus the last day
				focusDay = 6;
			}
		} else {
			super.onFocus(direction);
		}
	}

	protected int moveFocus(int amount, int status, int time) {
		if (!UiUtilities.IS_TOUCH) {
			// Handle changing the current day if the ALT key is down and they
			// scroll
			if ((status & (KeypadListener.STATUS_ALT | KeypadListener.STATUS_ALT_LOCK)) > 0) {
				if ((amount & 1) > 0) {
					toggleFocusedDay();
					return 0;
				}
			}
	
			if (amount > 0) {
				// move focus forwards
				focusDay += amount;
				if (focusDay > 6) {
					int ret = focusDay - 6;
					focusDay = 6;
					return ret;
				}
			} else {
				// move focus backwards
				focusDay += amount;
				if (focusDay < 0) {
					int ret = focusDay;
					focusDay = 0;
					return ret;
				}
			}
			return 0;
		} else {
			return super.moveFocus(amount, status, time);
		}
	}

	protected void drawFocus(Graphics graphics, boolean on) {
		if (!UiUtilities.IS_TOUCH) {
			drawHighlightRegion(graphics, Field.HIGHLIGHT_FOCUS, on,
					labelXCoords[focusDay], 2, labelXWidths[focusDay], getFont()
							.getHeight());
		}
	}

	
	protected boolean trackwheelClick(int status, int time) {
		if (!UiUtilities.IS_TOUCH) {
			toggleFocusedDay();
			return true;
		} else {
			return super.trackwheelClick(status, time);
		}
	}

	protected boolean keyDown(int keycode, int time) {
		if (!UiUtilities.IS_TOUCH) {
			if (keycode == Keypad.KEY_SPACE) {
				toggleFocusedDay();
				return true;
			}
			return false;
		} else {
			return super.keyDown(keycode, time);
		}
	}
	
	private void toggleFocusedDay() {
		days ^= (1 << focusDay);
		setMuddy(true);
		invalidate();
	}
}