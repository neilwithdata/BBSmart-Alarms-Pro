package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.LabelField;

public class ColorLabelField extends LabelField {
	private int color;

	public ColorLabelField(String text, int color) {
		super(text);
		this.color = color;
	}

	protected void paint(Graphics graphics) {
		graphics.setColor(color);
		graphics.clear();
		super.paint(graphics);
	}
}