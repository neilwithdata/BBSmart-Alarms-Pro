package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.ui.fields.ColorLabelField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.HrefField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.NowHeadingField;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public class RegisterScreen extends MainScreen {
	private String buyNowURL = "https://www.mobihand.com/mobilecart/mc1.asp?posid=16&pid=19069&did="
			+ Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase();

	private int fontHeight;

	public RegisterScreen() {
		setScreenFont();
		initDisplay();
	}

	private void setScreenFont() {
		setFont(getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));
	}

	private void initDisplay() {
		add(new NowHeadingField("Buy BBSmart Alarms Pro", false, UiUtilities.KEY_SMALL));

		heading("> HOW TO PURCHASE", true);
		text("You can purchase the full version of BBSmart Alarms Pro either of the following ways:\n");

		heading(">> From your PC", false);
		text("From your home PC, head on over to the BBSmart website and purchase a copy.");
		link("www.blackberrysmart.com", "http://www.blackberrysmart.com");

		spacer();

		heading(">> From your BlackBerry", false);
		text("Alternatively you can buy online right now from our secure mobile-friendly store by clicking the \"Buy Now\" link below.");
		link("Buy Now", buyNowURL);
	}

	public boolean onClose() {
		close();
		return true;
	}

	private void heading(String headingText, boolean major) {
		add(new ColorLabelField(headingText, major ? Color.BLUE : Color.GREEN));
	}

	private void text(String text) {
		add(new BasicEditField("", text.toString(),
				BasicEditField.DEFAULT_MAXCHARS, BasicEditField.READONLY));
	}

	private void link(String show, final String url) {
		HrefField siteLink = new HrefField(show, false, getFont());
		siteLink.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Browser.getDefaultSession().displayPage(url);
			}
		});

		HorizontalFieldManager hfm = new HorizontalFieldManager(
				HorizontalFieldManager.FIELD_HCENTER);
		hfm.add(siteLink);
		add(hfm);
	}

	private void spacer() {
		add(new LabelField("", LabelField.READONLY));
	}
}

class HighlightedEditField extends BasicEditField {
	public HighlightedEditField(String label, String initialValue,
			int maxNumChars, long style) {
		super(label, initialValue, maxNumChars, style
				| BasicEditField.NO_NEWLINE);
	}

	protected void drawFocus(net.rim.device.api.ui.Graphics graphics, boolean on) {
		super.drawFocus(graphics, on);

		XYRect r = graphics.getClippingRect();

		if (on) {
			graphics.drawRect(r.x, r.y, r.width - r.x, r.height);
		} else {
			int base = graphics.getColor();
			graphics.setColor(0xFFFFFF);
			graphics.drawRect(r.x, r.y, r.width - r.x, r.height);
			graphics.setColor(base);
		}

		invalidate();
	}

	protected void paint(Graphics graphics) {
		graphics.clear(graphics.getClippingRect());
		super.paint(graphics);
	}
}