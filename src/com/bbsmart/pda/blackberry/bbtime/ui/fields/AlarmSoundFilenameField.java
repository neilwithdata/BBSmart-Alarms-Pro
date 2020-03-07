package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.ui.screens.AlarmSoundFileChooserScreen;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;

public class AlarmSoundFilenameField extends Field {

	private int fieldWidth;
	private int fieldHeight;

	private int nameLabelWidth;

	private String filename;

	public AlarmSoundFilenameField(String file) {
		if (file != null) {
			filename = file;
		} else {
			filename = "";
		}
	}

	public String getFilename() {
		return filename;
	}

	protected void layout(int availWidth, int availHeight) {
		// Uses whole width, one font height
		fieldWidth = availWidth;

		if (UiUtilities.IS_TOUCH) {
			fieldHeight = getFont().getHeight() + 8;
		} else {
			fieldHeight = Font.getDefault().getHeight();
		}

		setExtent(fieldWidth, fieldHeight);
	}

	protected void paint(Graphics g) {
		final int labelPadding = 20;

		int labelWidth = g.getFont().getAdvance("File");
		g.drawText("File", 0, 0);

		String name = "- None -";
		int slash = filename.lastIndexOf('/');
		if (slash != -1) {
			name = filename.substring(slash + 1);
		}

		// limit the width of the filename to something sensible!
		// base it on how much room there is left after the label above...
		nameLabelWidth = g.getFont().getAdvance(name);
		if (nameLabelWidth + labelWidth + labelPadding > fieldWidth) {
			nameLabelWidth = fieldWidth - labelWidth - labelPadding;
			int ellipsisWidth = g.getFont().getAdvance("...");
			g.drawText(name, fieldWidth - nameLabelWidth, 0, 0, nameLabelWidth
					- ellipsisWidth);
			g.drawText("...", fieldWidth - ellipsisWidth, 0);
		} else {
			g.drawText(name, fieldWidth - nameLabelWidth, 0);
		}
	}

	public boolean isFocusable() {
		return true;
	}

	protected void drawFocus(Graphics graphics, boolean on) {
		drawHighlightRegion(graphics, Field.HIGHLIGHT_FOCUS, on, fieldWidth
				- nameLabelWidth - 1, 0, nameLabelWidth + 1, fieldHeight);
	}

	protected boolean trackwheelClick(int status, int time) {
		selectAction();
		return true;
	}

	protected boolean keyDown(int keycode, int time) {
		if (keycode == Keypad.KEY_SPACE) {
			selectAction();
			return true;
		}
		return false;
	}

	private void selectAction() {
		AlarmSoundFileChooserScreen chooser = new AlarmSoundFileChooserScreen(
				filename);
		BBSmartTimePro.instance.pushModalScreen(chooser);
		String newFilename = chooser.getFilename();
		if (filename.compareTo(newFilename) != 0) {
			setDirty(true);
			filename = newFilename;
			this.invalidate();
		}
	}

}
