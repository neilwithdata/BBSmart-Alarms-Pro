package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

public class SpacerField extends Field {
	private int fieldWidth;
	private int fieldHeight;
	private int backgroundColour;

	public static final int MODE_HORIZ = 0;
	public static final int MODE_VERT = 1;

	public SpacerField(int mode, int len, int backgroundColour) {
		super(Field.NON_FOCUSABLE);
		this.backgroundColour = backgroundColour;

		if (mode == MODE_HORIZ) { // Horizontal spacer
			this.fieldWidth = len;
			this.fieldHeight = 1;
		} else {
			this.fieldWidth = Graphics.getScreenWidth();
			this.fieldHeight = len;
		}
	}

	protected void layout(int width, int height) {
		setExtent(getPreferredWidth(), getPreferredHeight());
	}

	public int getPreferredWidth() {
		return fieldWidth;
	}

	public int getPreferredHeight() {
		return fieldHeight;
	}

	protected void paint(Graphics graphics) {
		graphics.setColor(backgroundColour);
		graphics.fillRect(0, 0, fieldWidth, fieldHeight);
	}
}
