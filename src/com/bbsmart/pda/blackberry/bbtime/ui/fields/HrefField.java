package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

public class HrefField extends Field {
	private String content;
	private Font fieldFont;
	private int fieldWidth;
	private int fieldHeight;
	private boolean active = false;
	private int backgroundColour = 0xffffff;
	private int textColour = 0x0000FF;
	private int maskColour = 0xBBBBBB;
	private int selectionColor = Color.YELLOW;

	private boolean selectable;
	private boolean selected;

	public HrefField(String content, boolean selectable, Font fieldFont) {
		super(Field.FOCUSABLE);
		this.content = content;
		this.selectable = selectable;

		this.fieldFont = fieldFont;

		fieldWidth = fieldFont.getAdvance(content);
		fieldHeight = fieldFont.getHeight();
		if (selectable) {
			fieldWidth += 6;
			fieldHeight += 7;
		} else {
			fieldWidth += 2;
			fieldHeight += 3;
		}
	}

	public void setColours(int backgroundColour, int textColour, int maskColour) {
		this.backgroundColour = backgroundColour;
		this.textColour = textColour;
		this.maskColour = maskColour;
		invalidate();
	}

	public void setBackgroundColour(int backgroundColour) {
		this.backgroundColour = backgroundColour;
		invalidate();
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		invalidate();
	}

	public boolean isSelected() {
		return selected;
	}

	public void setTextColour(int textColour) {
		this.textColour = textColour;
		invalidate();
	}

	public void setMaskColour(int maskColour) {
		this.maskColour = maskColour;
		invalidate();
	}

	public void setFont(Font fieldFont) {
		this.fieldFont = fieldFont;
	}

	public int getPreferredWidth() {
		return fieldWidth;
	}

	public int getPreferredHeight() {
		return fieldHeight;
	}

	protected void layout(int arg0, int arg1) {
		setExtent(getPreferredWidth(), getPreferredHeight());
	}

	protected void paint(Graphics graphics) {
		if (selectable) {
			int bgColor = selected ? selectionColor : backgroundColour;
			graphics.setColor(bgColor);
			graphics.fillRect(0, 0, fieldWidth, fieldHeight);

			int fgColor = active ? maskColour : bgColor;
			graphics.setColor(fgColor);
			graphics.fillRect(2, 2, fieldWidth - 4, fieldHeight - 4);

			graphics.setColor(textColour);
			graphics.setFont(fieldFont);
			graphics.drawText(content, 3, 3);
			graphics.drawLine(3, fieldHeight - 4, fieldWidth - 3,
					fieldHeight - 4);
		} else {
			int fgColor = active ? maskColour : backgroundColour;
			graphics.setColor(fgColor);
			graphics.fillRect(0, 0, fieldWidth, fieldHeight);

			graphics.setColor(textColour);
			graphics.setFont(fieldFont);
			graphics.drawText(content, 1, 1);
			graphics.drawLine(1, fieldHeight - 2, fieldWidth - 2,
					fieldHeight - 2);
		}
	}

	protected boolean navigationClick(int status, int time) {
		if (selectable) {
			if (!selected) {
				selected = true;
				setDirty(true);
				invalidate();
			}
		}
		fieldChangeNotify(1);
		return true;
	}

	protected void onFocus(int direction) {
		active = true;
		invalidate();
	}

	protected void onUnfocus() {
		active = false;
		invalidate();
	}
}
