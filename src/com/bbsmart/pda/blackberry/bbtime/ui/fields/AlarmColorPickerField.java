package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BitmapField;

public class AlarmColorPickerField extends BitmapField {
	private static final int NUM_ALARMSBG_COLORS = 10;

	private int initialColorIdx;
	private int selectedColorIdx;
	
	private boolean hasFocus;

	public AlarmColorPickerField(int initialColorIdx) {
		super(getAlarmColorBitmap(initialColorIdx), BitmapField.FOCUSABLE);
		this.initialColorIdx = initialColorIdx;
		this.selectedColorIdx = initialColorIdx;
		
		hasFocus = false;
	}

	protected void paint(Graphics graphics) {
		graphics.setGlobalAlpha(255);
		Bitmap bmp = UiUtilities.ALARM_BG_COLORS[selectedColorIdx];
		graphics.drawBitmap(0, 0, bmp.getWidth(), bmp.getHeight(), bmp, 0, 0);

		graphics.setColor(Color.YELLOW);
		graphics.setGlobalAlpha(hasFocus ? 200 : 100);

		// Write "Click to Change" in the middle
		int txtLen = graphics.getFont().getAdvance("Click to Change");
		graphics.drawText("Click to Change",
				(Graphics.getScreenWidth() - txtLen) / 2,
				(bmp.getHeight() - graphics.getFont().getHeight()) / 2);
	}
	
	protected void onFocus(int direction) {
		hasFocus = true;
		invalidate();
	}
	
	protected void onUnfocus() {
		hasFocus = false;
		invalidate();
	}
	
	public int getSelectedColorIndx() {
		return selectedColorIdx;
	}

	public static Bitmap getAlarmColorBitmap(int indx) {
		return Bitmap.getBitmapResource((UiUtilities.DEVICE_480W ? "hr_" : "")
				+ indx + ".jpg");
	}

	public boolean isDirty() {
		return initialColorIdx != selectedColorIdx;
	}

	protected boolean trackwheelClick(int status, int time) {
		selectedColorIdx = (++selectedColorIdx) % NUM_ALARMSBG_COLORS;

		invalidate();
		return true;
	}
}
