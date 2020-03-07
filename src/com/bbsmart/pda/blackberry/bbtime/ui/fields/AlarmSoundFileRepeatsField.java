package com.bbsmart.pda.blackberry.bbtime.ui.fields;

import net.rim.device.api.ui.component.ObjectChoiceField;

public class AlarmSoundFileRepeatsField extends ObjectChoiceField {

	private static final String CHOICES[] = { "1", "2", "3", "5", "10", "100",
			"Forever" };

	public AlarmSoundFileRepeatsField(int initialValue) {
		super("Repeat", CHOICES, 0);

		int initialIndex = 0;
		if (initialValue == 0) {
			initialIndex = CHOICES.length - 1;
		} else {
			for (int i = 1; i < CHOICES.length; i++) {
				if (String.valueOf(initialValue).equalsIgnoreCase(CHOICES[i])) {
					initialIndex = i;
				}
			}
		}
		setSelectedIndex(initialIndex);
	}

	public int getValue() {
		if (getSelectedIndex() < CHOICES.length - 1) {
			return Integer.parseInt((String) CHOICES[getSelectedIndex()]);
		} else {
			return 0;
		}
	}
}