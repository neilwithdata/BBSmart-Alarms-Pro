package com.bbsmart.pda.blackberry.bbtime.ui.screens;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class AlarmSoundFileChooserScreen extends MainScreen {

	private String filename;
	private LabelField currentPathField;
	private FileListField fileListField;
	private String currentPath;

	public AlarmSoundFileChooserScreen(String filename) {
		if (filename != null) {
			this.filename = filename;
		} else {
			this.filename = "";
		}

		setTitle(new LabelField("Select a Sound File"));

		currentPathField = new LabelField("");
		fileListField = new FileListField();
		add(currentPathField);
		add(new SeparatorField());
		add(fileListField);

		currentPath = "";

		int slash = filename.lastIndexOf('/');
		if (slash != -1) {
			// be sure to include the slash
			currentPath = filename.substring(0, slash + 1);
		}
		listDirectory(currentPath);
		currentPathField.setText("/" + currentPath);
	}

	public String getFilename() {
		return filename;
	}

	protected boolean navigationClick(int status, int time) {
		// Navigate to the selected item if it is a directory
		// otherwise set the filename and return
		FilesystemElement element = fileListField.get(fileListField
				.getSelectedIndex());
		if (element.isDir) {
			if (element.filename == "../") {
				currentPath = getParentPath();
			} else {
				currentPath = element.path + element.filename;
			}
			listDirectory(currentPath);
			currentPathField.setText("/" + currentPath);
		} else {
			String fullName = "file:///" + element.path + element.filename;

			if (MIMETypeAssociations.getMIMEType(fullName).startsWith("audio")) {
				filename = element.path + element.filename;
				close();
			} else {
				// Not an audio file selected
				Dialog.alert("Please select an audio file (eg. mp3, wav, wma)");
			}
		}
		return true;
	}

	protected boolean keyCharUnhandled(char key, int status, int time) {
		if (key == Characters.ESCAPE) {
			// if the current path is "", then close the screen
			if (currentPath.length() == 0) {
				close();
			} else {
				// go back one directory
				currentPath = getParentPath();
				listDirectory(currentPath);
				currentPathField.setText("/" + currentPath);
				return false;
			}
		}
		return false;
	}

	private void listDirectory(String path) {
		// Clear the file list, ready for a new directory
		fileListField.clear();
		if (path.length() > 0) {
			fileListField.addElement(new FilesystemElement("", "../", true));
		}

		FileConnection fc = null;
		Enumeration entries = null;

		if (path.length() > 0) {
			// open the file system and get the list of directories/files
			try {
				fc = (FileConnection) Connector.open("file:///" + path);
				entries = fc.list();
			} catch (Exception ioex) {
			} finally {
				if (fc != null) { // everything is read, make sure to close
					// the connection
					try {
						fc.close();
						fc = null;
					} catch (Exception ioex) {
					}
				}
			}
		} else {
			// If no path was provided, list the system root directories
			entries = FileSystemRegistry.listRoots();
		}

		if (entries == null) {
			// damn no files!
			return;
		}

		// display the list of directories/files
		while (entries.hasMoreElements()) {
			String filename = (String) entries.nextElement();

			// allow access to the store, just not to samples?
			if (!DeviceInfo.isSimulator()) {
				if ((path + filename).equalsIgnoreCase("store/samples/")) {
					// don't add "store/samples/" to the list!
					continue;
				}
			}

			FilesystemElement element = new FilesystemElement(path, filename,
					isPathADirectory(path + filename));
			fileListField.addElement(element);
		}
	}

	private boolean isPathADirectory(String path) {
		boolean ret = false;

		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open("file:///" + path);

			ret = fc.isDirectory();
		} catch (Exception e) {
		} finally {
			if (fc != null) {
				try {
					fc.close();
					fc = null;
				} catch (Exception ioex) {
				}
			}
		}

		return ret;
	}

	private String getParentPath() {
		String ret;
		int slash = currentPath.lastIndexOf('/');
		if (slash == -1) {
			ret = "";
		} else {
			// cut off the trailing slash (should be the last char, but do this
			// anyway)
			ret = currentPath.substring(0, slash);
		}
		slash = ret.lastIndexOf('/');
		if (slash == -1) {
			ret = "";
		} else {
			// be sure to include the slash
			ret = ret.substring(0, slash + 1);
		}
		return ret;
	}

	private static class FilesystemElement {
		public String filename;
		public String path;
		public boolean isDir;

		FilesystemElement(String path, String filename, boolean isDir) {
			this.path = path;
			this.filename = filename;
			this.isDir = isDir;
		}
	}

	private static class FileListField extends ListField implements
			ListFieldCallback {
		private Vector elements;

		public FileListField() {
			elements = new Vector();
			this.setCallback(this);
		}

		public void addElement(FilesystemElement element) {
			elements.addElement(element);
			setSize(elements.size());
		}

		public void clear() {
			elements.removeAllElements();
			setSize(0);
		}

		public void drawListRow(ListField listField, Graphics graphics,
				int index, int y, int width) {
			FilesystemElement element = get(index);
			graphics.drawText(element.filename, 0, y);
		}

		public FilesystemElement get(int index) {
			return (FilesystemElement) elements.elementAt(index);
		}

		// Boring bits
		// //////////////////////////////////////////////////////////////////////////
		public Object get(ListField listField, int index) {
			return null;
		}

		public int getPreferredWidth(ListField listField) {
			return Graphics.getScreenWidth();
		}

		public int indexOfList(ListField listField, String prefix, int start) {
			return -1;
		}
	}
}
