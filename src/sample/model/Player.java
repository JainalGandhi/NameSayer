package sample.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sample.gui.PopupAlert;

public class Player {

	private String text;
	private List<String> playlistNames = new ArrayList<String>();
	private PopupAlert alert = new PopupAlert();
	private String currentName;
	private int currentNameIndex;
	private List<File> wavFilesPlaylist = new ArrayList<File>();
	
	public Player(String text) {
		this.text = text;
	}
	
	public void formPlaylist() throws IOException {
		String[] splitText = text.split("\n");
		if (splitText.length == 0) {
			alert.noNameSelected();
		}
		else {
			for (String str : splitText) {
				playlistNames.add(str);
				currentName = playlistNames.get(0);
				currentNameIndex = 0;
			}
			createWavFiles();
		}			
	}
	
	public void createWavFiles() throws IOException {
		for (String name : playlistNames) {
			if (name.contains(" ") || name.contains("-")) {
				String[] names = name.split(" |-");
				for (String singleName : names) {
					String path = createFilePath(singleName);
				}
			}
			else {
				// add singular wav file to the files playlist
				// if no such name exists, file is not added
				String path = createFilePath(name);
				if (!path.equals("")) {
					File file = new File(path);
					wavFilesPlaylist.add(file);
				}
			}
		}
	}
	
	public String createFilePath(String name) throws IOException {
		// TODO currently takes first match regardless or quality
		// name is a singular name, find file
		String commandDatabase = "ls names/database | grep -i _" + name;
		Process processDatabase = new ProcessBuilder("/bin/bash", "-c", commandDatabase).start();
		InputStream stdout = processDatabase.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String creation;
		// if a file exists in the database, add the wav file to the playlist of files, otherwise search the user files
		if ((creation = stdoutBuffered.readLine()) != null) {
			String path = System.getProperty("user.dir") + "/names/database/" + creation;
			return path;
		}
		else {
			String commandUser = "ls names/user | grep -i _" + name;
			Process processUser = new ProcessBuilder("/bin/bash", "-c", commandUser).start();
			InputStream stdoutUser = processUser.getInputStream();
			BufferedReader stdoutBufferedUser = new BufferedReader(new InputStreamReader(stdoutUser));
			String creationUser;
			if ((creationUser = stdoutBufferedUser.readLine()) != null) {
				String path = System.getProperty("user.dir") + "/names/user/" + creationUser;
				return path;
			}
			else {
				// no such recording
				return "";
			}
		}
	}
	
	public String getCurrentName() {
		return currentName;
	}

}
