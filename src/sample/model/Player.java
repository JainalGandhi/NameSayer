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
	private List<PlayListItem> playList = new ArrayList<PlayListItem>();
	
	public Player() {}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void formPlaylist() throws IOException {
		playList.clear();
		playlistNames.clear();
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
	
	public void nextName() {
		int size = playList.size();
		if (currentNameIndex == size - 1) {
			currentNameIndex = 0;
		}
		else {
			currentNameIndex++;
		}
	}
	
	public void prevName() {
		int size = playList.size();
		if (currentNameIndex == 0) {
			currentNameIndex = size-1;
		}
		else {
			currentNameIndex--;
		}

	}
	
	public String getNowPlaying() {
		return playList.get(currentNameIndex).getNowPlayingText();
	}
	
	public void createWavFiles() throws IOException {
		for (String name : playlistNames) {
			// Name needs concatenation
			// TODO only basic concatenation if possible implemented, need to equalize volume etc
			PlayListItem item = new PlayListItem(name);
			
			if (name.contains(" ") || name.contains("-")) {
				removeConcatFile();
				String[] names = name.split(" |-");
				item.setNamesAmount(names.length);
				name = name.replace(" ", "_");
				name = name.replace("-", "_");
				for (String singleName : names) {
					String path = createFilePath(singleName);
					if (!path.equals("")) {
						addToConcatFile(path);
					}
					else {
						item.addWarning(singleName);
					}
				}
				createConcatFile(name);
				String path = System.getProperty("user.dir") + "/names/temp/" + name + ".wav";
				File file = new File(path);
				wavFilesPlaylist.add(file);
				item.setWav(file);
			}
			else {
				// add singular wav file to the files playlist
				// if no such name exists, file is not added
				String path = createFilePath(name);
				item.setNamesAmount(1);
				if (!path.equals("")) {
					File file = new File(path);
					wavFilesPlaylist.add(file);
					item.setWav(file);
				}
				else {
					item.addWarning(name);
				}
			}
			playList.add(item);
		}
	}
	
	public void createConcatFile(String names) {
		String command = "ffmpeg -f concat -safe 0 -i names/concat.txt -c copy names/temp/" + names + ".wav";
		try {
			Process process = new ProcessBuilder("/bin/bash", "-c", command).start();
		} catch (IOException e) {
			alert.unkownError();
		}
	}
	
	public void addToConcatFile(String path) {
		String concatPath = "\'" + System.getProperty("user.dir") + "/names/concat.txt" + "\'";
		String command = "echo file " + path + " >> " + concatPath;
		try {
			Process process = new ProcessBuilder("/bin/bash", "-c", command).start();
		} catch (IOException e) {
			alert.unkownError();
		}
	}
	
	public void removeConcatFile() {
		String path = System.getProperty("user.dir") + "/names/concat.txt";
		File concat = new File(path);
		if (concat.exists()) {
			String command = "rm " + path;
			try {
				Process process = new ProcessBuilder("/bin/bash", "-c", command).start();
			} catch (IOException e) {
				alert.unkownError();
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
