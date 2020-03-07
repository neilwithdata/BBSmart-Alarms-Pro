package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import java.util.Calendar;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.RealtimeClockListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BitmapField;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

public class NowHeadingField extends BitmapField implements
		RealtimeClockListener {
	private String headingText;
	private boolean displayTime;
	private Bitmap icon;

	public NowHeadingField(String headingText, boolean displayTime, Bitmap icon) {
		super(UiUtilities.ALARM_HEADING_BG);
		this.headingText = headingText;
		this.displayTime = displayTime;
		this.icon = icon;
	}

	protected void onDisplay() {
		super.onDisplay();

		if (displayTime) {
			if (BBSmartTimePro.instance != null) {
				BBSmartTimePro.instance.addRealtimeClockListener(this);
			}
		}
	}

	public void clockUpdated() {
		invalidate(); // refresh when time updates
	}

	protected void paint(Graphics graphics) {
		super.paint(graphics);
		
		int fontHeight = UiUtilities.DEVICE_480W ? 25 : 20;
		int yOffset = UiUtilities.DEVICE_480W ? 2 : 5;

		graphics.setFont(graphics.getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));

		graphics
				.drawBitmap(0, 0, icon.getWidth(), icon.getHeight(), icon, 0, 0);

		graphics.drawText(headingText, icon.getWidth() + 5, yOffset);

		// Don't display the time on suretype devices (not enough screen real-estate)
		if (displayTime && !UiUtilities.DEVICE_SURETYPE) {
			String currTime = "(Now: " + getCurrTimeString() + ")";
			int tDrawLen = graphics.getFont().getAdvance(currTime);
			graphics
					.drawText(currTime, Graphics.getScreenWidth() - tDrawLen, yOffset);
		}
	}

	private String getCurrTimeString() {
		return DateFormat.getInstance(DateFormat.TIME_DEFAULT).formatLocal(
				Calendar.getInstance().getTime().getTime());
	}

	protected void onUndisplay() {
		super.onUndisplay();

		if (displayTime) {
			if (BBSmartTimePro.instance != null) {
				BBSmartTimePro.instance.removeRealtimeClockListener(this);
			}
		}
	}
}