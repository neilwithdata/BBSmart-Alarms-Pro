//#preprocess
package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import com.bbsmart.pda.blackberry.bbtime.AppInfo;
import com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro;
import com.bbsmart.pda.blackberry.bbtime.io.TrialManager;
import com.bbsmart.pda.blackberry.bbtime.models.Alarm;
import com.bbsmart.pda.blackberry.bbtime.models.AlarmList;
import com.bbsmart.pda.blackberry.bbtime.models.DefaultAlarm;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.AlarmListField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.NowHeadingField;
import com.bbsmart.pda.blackberry.bbtime.ui.fields.StyledButtonField;
import com.bbsmart.pda.blackberry.bbtime.ui.screens.AlarmOptionsScreen;
import com.bbsmart.pda.blackberry.bbtime.util.UiUtilities;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

//#ifndef 4.2
import net.rim.blackberry.api.messagelist.*;
//#endif

public class AlarmListScreen extends MainScreen implements
		AlarmList.AlarmListener {

	private AlarmListField alarmList;
	private StyledButtonField regularBtn;
	private StyledButtonField quickBtn;

	private VerticalFieldManager alarmsMan;
	private NoAlarmsVerticalFM noMan;

	private BitmapField trialStatusBar;

	// flag to indicate whether we are in no alarms mode
	private boolean noAlarmsMode;

	public AlarmListScreen() {
		noAlarmsMode = false;
		constructManagers();

		initStatusBar();

		AlarmList.addAlarmListener(this);
		refreshDisplay();
	}

	private void initStatusBar() {
		Bitmap bg = UiUtilities.get("title_bg_480.jpg");
		Graphics g = new Graphics(bg);
		trialStatusBar = new BitmapField(bg);

		int fontHeight = UiUtilities.DEVICE_480W ? 25 : 20;
		int yOffset = UiUtilities.DEVICE_480W ? 2 : 5;

		g.setFont(g.getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0));
		g.setColor(Color.WHITE);

		g.drawText("TRIAL VERSION ("
				+ TrialManager.getInstance().getTrialTimeRemaining() + ")", 2,
				yOffset);
	}

	private void constructManagers() {
		alarmsMan = new VerticalFieldManager();
		alarmsMan.add(new NowHeadingField("Alarm List", true,
				UiUtilities.ALARMBELL));
		alarmList = new AlarmListField(AlarmList.getInstance().alarmList);
		alarmsMan.add(alarmList);

		// Ininitialise the no alarms manager (special)
		noMan = new NoAlarmsVerticalFM();

		noMan.add(new LabelField("No Alarms Exist!", LabelField.FIELD_LEFT));
		noMan.add(new LabelField("Create one now?", LabelField.FIELD_HCENTER));

		int btnSize = UiUtilities.DEVICE_480W ? StyledButtonField.SIZE_XLARGE
				: StyledButtonField.SIZE_LARGE;
		int btnLen = UiUtilities.DEVICE_480W ? 210 : 140;

		regularBtn = new StyledButtonField("Regular Alarm", btnSize, btnLen,
				noMan.getBigBtnFont());
		regularBtn.setStrictMenu(true);
		regularBtn.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				launchRegularAlarm();
			}
		});
		noMan.add(regularBtn);

		quickBtn = new StyledButtonField("Quick Alarm", btnSize, btnLen, noMan
				.getBigBtnFont());
		quickBtn.setStrictMenu(true);
		quickBtn.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				launchQuickAlarm();
			}
		});
		noMan.add(quickBtn);
	}

	private void refreshDisplay() {
		Field focusField = getLeafFieldWithFocus();

		// Remove all the items on the screen
		noAlarmsMode = false;
		deleteAll();

		if (!AlarmList.getInstance().alarmList.isEmpty()) {
			alarmList.updateAlarms();
			add(alarmsMan);
		}

		if (getFieldCount() == 0) {
			// No snoozed or regular alarms exist
			noAlarmsMode = true;
			add(noMan);
		} else {
			// Attempt to return the focus to whatever it was prior to update
			try {
				focusField.setFocus();
			} catch (Exception e) {
				// Must no longer exist on screen...do nothing
			}
		}

		displayTrialNotice();
	}

	protected void onExposed() {
		// Update the status bar everytime the screen is exposed
		initStatusBar();
		displayTrialNotice();
		super.onExposed();
	}

	private void displayTrialNotice() {
		if (TrialManager.getInstance().state == TrialManager.STATE_TRIAL) {
			if (!noAlarmsMode) {
				setStatus(trialStatusBar);
			} else {
				setStatus(null);
			}
		} else {
			setStatus(null);
		}
	}

	public void alarmsChanged() {
		refreshDisplay();
	}

	protected boolean trackwheelRoll(int amount, int status, int time) {
		if (noAlarmsMode) {
			Field inFocus = getLeafFieldWithFocus();

			if (inFocus != null) {
				if (inFocus.equals(regularBtn)) {
					if (amount < 0) {
						// Focus is on first element and up move attempted
						return true;
					}
				} else {
					if (amount > 0) {
						// focus is on last element and attempted to move down
						return true;
					}
				}
			}
		}

		return super.trackwheelRoll(amount, status, time);
	}

	private void launchQuickAlarm() {
		BBSmartTimePro.instance.pushScreen(new QuickAlarmScreen());
	}

	private void launchRegularAlarm() {
		BBSmartTimePro.instance.pushScreen(new AlarmDetailScreen(new Alarm(
				DefaultAlarm.getInstance()), AlarmDetailScreen.MODE_NEW));
	}

	protected void makeMenu(Menu menu, int instance) {

		// No matter what, put these menu items on this screen!
		menu.add(new MenuItem("Create Regular Alarm", 0, 0) {
			public void run() {
				launchRegularAlarm();
			}
		});

		menu.add(new MenuItem("Create Quick Alarm", 0, 0) {
			public void run() {
				launchQuickAlarm();
			}
		});

		menu.add(MenuItem.separator(0));

		menu.add(new MenuItem("Options", 0, 0) {
			public void run() {
				BBSmartTimePro.instance.pushScreen(new AlarmOptionsScreen());
			}
		});

		menu.add(MenuItem.separator(0));

		if (TrialManager.getInstance().state == TrialManager.STATE_TRIAL) {
			// Show Buy/Register menu option
			menu.add(new MenuItem("Buy Full Version", 0, 0) {
				public void run() {
					BBSmartTimePro.instance.pushScreen(new RegisterScreen());
				}
			});
		}

		menu.add(new MenuItem("About", 0, 0) {
			public void run() {
				BBSmartTimePro.instance.pushScreen(new AboutScreen());
			}
		});

		menu.add(MenuItem.separator(0));

		menu.add(new MenuItem("Minimize", 0, 0) {
			public void run() {
				onClose();
			}
		});

		menu.add(new MenuItem("Force Exit", 0, 0) {
			public void run() {
				if (Dialog.ask(Dialog.D_YES_NO,
						"Alarms will not work if you exit.\nAre you sure?",
						Dialog.NO) != Dialog.YES) {
					return;
				}

				// Remove the alarm change listeners
				AlarmListScreen thisScreen = (AlarmListScreen) BBSmartTimePro.instance
						.getActiveScreen();
				AlarmList.removeAlarmListener(thisScreen);

				AlarmList.getInstance().save();
				
				// unregister the application indicator
				//#ifndef 4.2
				ApplicationIndicatorRegistry.getInstance().unregister();
				//#endif

				System.exit(0);
			}
		});
	}

	public boolean onClose() {
		AlarmList.getInstance().save();
		BBSmartTimePro.instance.requestBackground();
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		switch (c) {
		case 'q': // Create quick alarm (SureType & Full)
			launchQuickAlarm();
			break;
		case 'r': // Create regular alarm (Full)
			launchRegularAlarm();
			break;
		case 'e': // Create regular alarm (SureType)
			if (UiUtilities.DEVICE_SURETYPE) {
				launchRegularAlarm();
			}
			break;
		}

		return super.keyChar(c, status, time);
	}
	
	protected boolean openProductionBackdoor(int backdoorCode) {
		switch (backdoorCode) {
		case ('P' << 24) | ('P' << 16) | ('P' << 8) | 'P':
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					// Clear out the private key store
					PersistentStore
							.destroyPersistentObject(AppInfo.APP_PRIVATE_KEY);
					Dialog.alert("DEBUG: Persistence cleared");
				}
			});
			return true; // handled
		}
		return super.openProductionBackdoor(backdoorCode);
	}
}

class NoAlarmsVerticalFM extends VerticalFieldManager {
	private static final int LR_LABEL_FONT_HEIGHT = 26;
	private static final int HR_LABEL_FONT_HEIGHT = 39;
	
	private Bitmap bgBitmap;

	public NoAlarmsVerticalFM() {
		super(VerticalFieldManager.USE_ALL_HEIGHT
				| VerticalFieldManager.USE_ALL_WIDTH);

		setFont(getManagerFont());

		bgBitmap = UiUtilities.NOALARMS_BG;
	}

	protected void paint(Graphics graphics) {
		graphics.drawBitmap(0, 0, Graphics.getScreenWidth(), Graphics
				.getScreenHeight(), bgBitmap, 0, 0);
		super.paint(graphics);
	}

	private Font getManagerFont() {
		int fontHeight = UiUtilities.DEVICE_480W ? HR_LABEL_FONT_HEIGHT
				: LR_LABEL_FONT_HEIGHT;

		return getFont().derive(Font.BOLD | Font.ITALIC, fontHeight,
				Ui.UNITS_px, Font.ANTIALIAS_STANDARD, 0);
	}

	// Have to do this to avoid weird rendering bug...I swear it's not my fault
	protected boolean navigationMovement(int dx, int dy, int status, int time) {
		Field inFocus = getLeafFieldWithFocus();

		if (inFocus != null) {
			if (inFocus.getIndex() == 2) {
				if (dy < 0 || dx < 0) {
					// Focus is on first element and up move attempted - swallow
					return true;
				}
			} else {
				if (dy > 0 || dx > 0) {
					// focus is on last element and attempted to move down -
					// swallow
					return true;
				}
			}
		}

		return super.navigationMovement(dx, dy, status, time);
	}

	protected void sublayout(int maxWidth, int maxHeight) {
		LabelField first = (LabelField) getField(0);
		LabelField second = (LabelField) getField(1);
		StyledButtonField third = (StyledButtonField) getField(2);
		StyledButtonField fourth = (StyledButtonField) getField(3);
		
		// First two fields are both label fields
		setPositionChild(first, 5, 5);
		layoutChild(first, maxWidth, maxHeight);

		// Center align the second labelfield
		int textLen = getFont().getAdvance(second.getText());
		setPositionChild(second, (Graphics.getScreenWidth() - textLen) / 2,
				getFont().getHeight() + 15);
		layoutChild(second, maxWidth, maxHeight);
		
		int top = second.getTop() + second.getHeight();
		int remaining = Graphics.getScreenHeight() - top;
		int ygap = (remaining - 2*third.getPreferredHeight())/3;

		// Position first button specially
		int btnLen = third.getPreferredWidth();
		setPositionChild(third, (Graphics.getScreenWidth() - btnLen)/2, top + ygap);
		layoutChild(third, maxWidth, maxHeight);

		top = top + ygap + third.getPreferredHeight();
		
		btnLen = fourth.getPreferredWidth();
		setPositionChild(fourth, (Graphics.getScreenWidth() - btnLen)/2, top + ygap);
		layoutChild(fourth, maxWidth, maxHeight);

		setExtent(Graphics.getScreenWidth(), Graphics.getScreenHeight());
	}
	
	public Font getBigBtnFont() {
		Font f;

		int fontHeight = UiUtilities.DEVICE_480W ? 25 : 17;

		try {
			f = FontFamily.forName("BBSansSerif")
					.getFont(Font.BOLD, fontHeight);
			f = f.derive(f.getStyle(), f.getHeight(), Ui.UNITS_px,
					Font.ANTIALIAS_STANDARD, 0);
		} catch (ClassNotFoundException cnfe) {
			f = getFont().derive(Font.BOLD, fontHeight, Ui.UNITS_px,
					Font.ANTIALIAS_STANDARD, 0);
		}

		return f;
	}
}