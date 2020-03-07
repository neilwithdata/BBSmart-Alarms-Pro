package com.bbsmart.pda.blackberry.bbtime;

import com.bbsmart.pda.blackberry.bbtime.io.TrialManager;

/**
 * Container class for application meta information
 */
public final class AppInfo {
	// Determines the start state of the application
	public static final int APP_START_STATE = TrialManager.STATE_REG;

	// Duration of the trial version
	public static final int TRIAL_DURATION_DAYS = 10;

	// Version number of the application
	public static final String VERSION_STRING = "2.2";

	// key is hash of string "com.bbsmart.pda.blackberry.bbtime"
	public static final long APP_KEY = 0xd71c14acdb246bc9L;
	public static final long APP_PRIVATE_KEY = 0xe17c3bed37e3a941L;
}