package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import java.util.Calendar;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.util.TimeUtil;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.RealtimeClockListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

public class DigitalClockField extends Field implements RealtimeClockListener {
	private Bitmap numberBitmap[];
	private Bitmap dotsBitmap;
	private Bitmap bezelBitmap;

	// the actual numerals take up 168x42!

	private final static int numberAdvance = 37;
	private final static int dotsAdvance = 12;
	private final static int horizSpace = 6;
	private final static int vertSpace = 6;

	private int fieldWidth;
	private int fieldHeight;

	private int hour1 = 2;
	private int hour2 = 1;
	private int minute1 = 5;
	private int minute2 = 3;

	public DigitalClockField() {
		setup(0);
	}

	public DigitalClockField(int minHeight) {
		setup(minHeight);
	}

	public void clockUpdated() {
		synchronized (BBSmartTimePro.getEventLock()) {
			update();
		}
	}
	
	public int getPreferredHeight() {
		return fieldHeight; 
	}
	
	public int getPreferredWidth() {
		return fieldWidth;
	}

	protected void onDisplay() {
		super.onDisplay();
		BBSmartTimePro.instance.addRealtimeClockListener(this);
	}

	protected void onUndisplay() {
		super.onUndisplay();
		BBSmartTimePro.instance.removeRealtimeClockListener(this);
	}

	private void setup(int minHeight) {
		numberBitmap = new Bitmap[10];
		int i;
		for (i = 0; i < 10; i++) {
			numberBitmap[i] = Bitmap.getBitmapResource("7seg-"
					+ String.valueOf(i) + ".png");
		}
		dotsBitmap = Bitmap.getBitmapResource("7seg-dots.png");
		bezelBitmap = Bitmap.getBitmapResource("bezel4.png");

		fieldHeight = Math.max(minHeight, bezelBitmap.getHeight());
	}

	protected void layout(int width, int height) {
		fieldWidth = width;
		// fieldHeight = bezelBitmap.getHeight();
		setExtent(fieldWidth, fieldHeight);
		update();
	}

	public void update() {
		Calendar currTime = Calendar.getInstance();
		String timeStr = TimeUtil.getTimeString(currTime);

		int colon = timeStr.indexOf(':');
		String hours = timeStr.substring(0, colon);
		String minutes = timeStr.substring(colon + 1);

		if (hours.length() == 1) {
			// only one hour to show
			hour1 = -1;
			hour2 = Integer.parseInt(hours);
		} else {
			hour1 = Integer.parseInt(hours.substring(0, 1));
			hour2 = Integer.parseInt(hours.substring(1, 2));
		}

		minute1 = Integer.parseInt(minutes.substring(0, 1));
		minute2 = Integer.parseInt(minutes.substring(1, 2));
		invalidate();
	}

	protected void paintNumeral(Graphics g, int x, int y, int number) {
		if (number < 0) {
			return;
		}

		if (number > 9) {
			return;
		}
		Bitmap paintMe = numberBitmap[number];
		g.drawBitmap(x, y, paintMe.getWidth(), paintMe.getHeight(), paintMe, 0,
				0);
	}

	protected void paintDots(Graphics g, int x, int y) {

		g.drawBitmap(x, y, dotsBitmap.getWidth(), dotsBitmap.getHeight(),
				dotsBitmap, 0, 0);
	}

	protected void paint(Graphics g) {
		int centerOffset = (fieldWidth - bezelBitmap.getWidth()) / 2;
		int heightOffset = (fieldHeight - bezelBitmap.getHeight()) / 2;

		g.drawBitmap(centerOffset, heightOffset, bezelBitmap.getWidth(),
				bezelBitmap.getHeight(), bezelBitmap, 0, 0);

		paintNumeral(g, centerOffset + horizSpace, heightOffset + vertSpace,
				hour1);
		paintNumeral(g, centerOffset + horizSpace + numberAdvance, heightOffset
				+ vertSpace, hour2);
		paintDots(g, centerOffset + horizSpace + numberAdvance * 2,
				heightOffset + vertSpace);
		paintNumeral(g, centerOffset + horizSpace + numberAdvance * 2
				+ dotsAdvance, heightOffset + vertSpace, minute1);
		paintNumeral(g, centerOffset + horizSpace + numberAdvance * 3
				+ dotsAdvance, heightOffset + vertSpace, minute2);
	}
}