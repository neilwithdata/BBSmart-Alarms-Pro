package com.bbsmart.pda.blackberry.bbtime.util;

import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import com.bbsmart.pda.blackberry.bbtime.models.Alarm;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.media.control.AudioPathControl;

public class AlarmSoundHelper {
	private int soundType;
	private String soundFilename;
	private Player soundPlayer;
	private int soundFileRepeats;

	private int volume;

	public AlarmSoundHelper(Alarm alarm) {
		soundType = alarm.soundType;

		if (soundType == Alarm.SOUND_PRE) {
			soundFilename = Alarm.PRE_NAMES[alarm.soundPreIndx] + ".mp3";
		} else {
			soundFilename = alarm.soundFileName;
		}

		soundFileRepeats = alarm.soundRepeats;
		volume = alarm.soundVolume;
	}

	public void startAlarmSound() {
		String filePath = "file:///" + soundFilename;
		String mimeType = getMimeType(filePath);

		// Do not attempt to play if filetype is not support by the media player
		if (!isSupported(filePath)) {
			return;
		}

		InputStream is = getInputStream(filePath);
		if (is != null) {
			try {
				// create an instance of the player from the InputStream
				soundPlayer = Manager.createPlayer(is, mimeType);
				soundPlayer.realize();
				soundPlayer.prefetch();
				if (soundFileRepeats < 1) {
					soundFileRepeats = -1; // loop indefinitely
				}
				soundPlayer.setLoopCount(soundFileRepeats);

				try {
					((VolumeControl) soundPlayer
							.getControl("javax.microedition.media.control.VolumeControl"))
							.setLevel(volume);
				} catch (Exception vce) {
					BBLogger.logEvent("Count not obtain VolumeControl: "
							+ vce.getMessage());
				}

				// Ensure that the sound is always only played through the
				// handset speaker (not paired bluetooth headset, etc.)
				try {
					((AudioPathControl) soundPlayer
							.getControl("net.rim.device.api.media.control.AudioPathControl"))
							.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSFREE);
				} catch (Exception vce) {
					BBLogger.logEvent("Count not obtain AudioPathControl: "
							+ vce.getMessage());
				}

				soundPlayer.start();
			} catch (Exception e) {
				BBLogger.logEvent(e.toString() + ":" + e.getClass().toString()
						+ ":" + "Exception attempting to play sound file "
						+ soundFilename + ": " + e.getMessage());
			}
		} else {
			BBLogger.logEvent("On attempting to play " + soundFilename + ", the input stream was found to be null");
		}
	}

	private InputStream getInputStream(String filePath) {
		if (soundType == Alarm.SOUND_FILE) {
			FileConnection fc = null;

			try {
				fc = (FileConnection) Connector.open(filePath);
				return fc.openInputStream();
			} catch (Exception e) {
				BBLogger
						.logEvent("Could not open file connection to sound file "
								+ soundFilename + ": " + e.getMessage());
				return null;
			}
		} else {
			try {
				return Class.forName(
						"com.bbsmart.pda.blackberry.bbtime.BBSmartTimePro")
						.getResourceAsStream("/" + soundFilename);
			} catch (Exception e) {
				BBLogger.logEvent("Could not open preloaded song "
						+ soundFilename + ": " + e.getMessage());
				return null;
			}
		}
	}

	private String getMimeType(String filePath) {
		if (soundType == Alarm.SOUND_FILE) {
			return MIMETypeAssociations.getMIMEType(filePath);
		} else {
			return "audio/mpeg"; // mp3 mime type
		}
	}

	// Check that the media player can handle this file type
	private boolean isSupported(String filePath) {
		String[] supportedTypes = Manager.getSupportedContentTypes(null);
		String mimeType = MIMETypeAssociations.getMIMEType(filePath);

		for (int i = 0; i < supportedTypes.length; i++) {
			if (supportedTypes[i].equalsIgnoreCase(mimeType)) {
				return true;
			}
		}

		return false;
	}

	public void stopAlarmSound() {
		try {
			soundPlayer.stop();
			soundPlayer.close();
		} catch (Exception e) {
			BBLogger.logEvent("Exception stoping play of sound file "
					+ soundFilename + ": " + e.getMessage());
		}
	}
}