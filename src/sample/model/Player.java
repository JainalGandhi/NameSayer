package sample.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import sample.gui.PopupAlert;

public class Player {

	private String text;
	private List<String> playlistNames = new ArrayList<>();
	private PopupAlert alert = new PopupAlert();
	private int currentNameIndex;
	private List<File> wavFilesPlaylist = new ArrayList<>();
	private List<PlayListItem> playList = new ArrayList<>();
	private List<PlayListItem> playListOriginal = new ArrayList<>();
	private String latestRecordedName;

	private MediaPlayer mediaPlayer;
	
	public Player() {}
	
	public void setText(String text) {
		this.text = text;
	}
	
	private File getCurrentWav() {
		return playList.get(currentNameIndex).getWav();
	}
	
	public void playCurrentName(double volume) {
		stopAudioPlayback();
		File file = playList.get(currentNameIndex).getWav();
            Runnable task = new Thread( ()-> {
                try {
                mediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
                mediaPlayer.setVolume(volume);
                mediaPlayer.play();
                mediaPlayer.setOnError( ()-> {

                        try {
                            String RECORD_NAME_ATTEMPT_COMMAND = "ffmpeg -y -i " + file.toString() + " " + file.toString();
                            Process process = new ProcessBuilder("/bin/bash", "-c", RECORD_NAME_ATTEMPT_COMMAND).start();
                            process.waitFor();

                            Platform.runLater(() -> {
                                mediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
                                mediaPlayer.play();
                            });
                        }catch(IOException | InterruptedException e) {
                            alert.unknownError();
                        }

                });
            	} catch (Exception e) {}
            });
            new Thread(task).start();
	}
	
	public void stopAudioPlayback() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}
	
	public void formPlaylist() throws IOException {
		playList.clear();
		playlistNames.clear();
		playListOriginal.clear();
		String[] splitText = text.split("\n");
		if (splitText.length == 0) {
			alert.noNameSelected();
		}
		else {
			playlistNames.addAll(Arrays.asList(splitText));
			currentNameIndex = 0;
			createWavFiles();
			playListOriginal.addAll(playList);
		}
	}

	public void addToPlaylist(String text) throws IOException {
		playlistNames.add(text);
		createSingleWavFile(text);
		playListOriginal.add(playList.get(playList.size()-1));
	}
	
	public void shufflePlayList() {
		Collections.shuffle(playList);
	}
	
	public void orderPlayList() {
		playList.clear();
		playList.addAll(playListOriginal);
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
	
	public PlayListItem getCurrentItem() {
		return playList.get(currentNameIndex);
	}
	
	private void createWavFiles() throws IOException {
		for (String name : playlistNames) {
			// Name needs concatenation
			createSingleWavFile(name);
		}
	}

	public String[] getSegmentedNames(String name) {
		return name.split("[ \\-_]");
	}

	public String getFileNamePart(String name) {
		name = name.replace(" ", "_");
		name = name.replace("-", "_");
		return name;
	}

	private void createSingleWavFile(String name) throws IOException {
		PlayListItem item = new PlayListItem(name);

		if (name.contains(" ") || name.contains("-") || name.contains("_")) {
			String[] names = name.split("[ \\-_]");
			item.setNamesAmount(names.length);
			name = name.replace(" ", "_");
			name = name.replace("-", "_");
			String workingName = name;
			removeConcatFile(workingName);
			for (String singleName : names) {
				String path = createFilePath(singleName);
				if (!path.equals("")) {
					addToConcatFile(path, workingName);
				} else {
					item.addWarning(singleName);
				}
			}
			createConcatFile(workingName);
			String path = System.getProperty("user.dir") + "/names/temp/" + workingName + ".wav";
			File file = new File(path);
			wavFilesPlaylist.add(file);
			item.setWav(file);
		} else {
			// add singular wav file to the files playlist
			// if no such name exists, file is not added
			String path = createFilePath(name);
			item.setNamesAmount(1);
			if (!path.equals("")) {
				File file = new File(path);
				wavFilesPlaylist.add(file);
				item.setWav(file);
			} else {
				item.addWarning(name);
			}
		}
		playList.add(item);
	}
	
	private void createConcatFile(String name) {
		String command = "ffmpeg -f concat -safe 0 -i names/temp/" + name + ".txt -c copy names/temp/" + name + ".wav; "
				+ "ffmpeg -i names/temp/" + name + ".wav -filter:a loudnorm names/temp/" + name + ".wav; "
						+ "ffmpeg -hide_banner -i names/temp/" + name + ".wav -af silenceremove=0:0:0:-1:2:-45dB names/temp/" + name + ".wav 2> /dev/null";
		try {
			Process process = new ProcessBuilder("/bin/bash", "-c", command).start();
		} catch (IOException e) {
			alert.unknownError();
		}
	}
	
	private void addToConcatFile(String path, String name) {
		String concatPath = "\'" + System.getProperty("user.dir") + "/names/temp/" + name + ".txt" + "\'";
		String command = "echo file " + path + " >> " + concatPath;
		try {
			ProcessBuilder process = new ProcessBuilder("/bin/bash", "-c", command);
			process.start();
		} catch (IOException e) {
			alert.unknownError();
		}
	}
	
	private void removeConcatFile(String name) {
		String path = System.getProperty("user.dir") + "/names/temp/" + name + ".txt";
		File concat = new File(path);
		if (concat.exists()) {
			String command = "rm " + path;
			try {
				ProcessBuilder process = new ProcessBuilder("/bin/bash", "-c", command);
				process.start();
			} catch (IOException e) {
				alert.unknownError();
			}
		}
	}
	
	public String createFilePath(String name) throws IOException {

		// Find badly rated names
		List<String> badCreations = new ArrayList<>();
		File badRated = new File(System.getProperty("user.dir") + "/names/badQualityRecordings.txt");
		BufferedReader br = new BufferedReader(new FileReader(badRated));
		String str;
		while ((str = br.readLine()) != null) {
			badCreations.add(str);
		}

		// name is a singular name, find file
		String commandDatabase = "ls names/database | grep -i _" + name;
		Process processDatabase = new ProcessBuilder("/bin/bash", "-c", commandDatabase).start();
		InputStream stdout = processDatabase.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String creation;

		// Go through files present
		// If such a file is rated badly, go on to the next file present
		// If all files are rated badly, take the first one always
		if ((creation = stdoutBuffered.readLine()) != null) {
			String path = System.getProperty("user.dir") + "/names/database/" + creation;
			String firstPath = path;
			for (String creations : badCreations) {
				if (creations.equals(path)) {
					creation = stdoutBuffered.readLine();
					if (creation == null) {
						return firstPath;
					}
					path = System.getProperty("user.dir") + "/names/database/" + creation;
				}
			}
			return path;
		}
		// Because we don't make available to option for deletion, this else statement should never be reached
		else {
			String commandUser = "ls names/user | grep -i _" + name;
			Process processUser = new ProcessBuilder("/bin/bash", "-c", commandUser).start();
			InputStream stdoutUser = processUser.getInputStream();
			BufferedReader stdoutBufferedUser = new BufferedReader(new InputStreamReader(stdoutUser));
			String creationUser;
			if ((creationUser = stdoutBufferedUser.readLine()) != null) {
				return System.getProperty("user.dir") + "/names/user/" + creationUser;
			}
			else {
				// no such recording
				return "";
			}
		}
	}

	public String getCurrentPlaylistName() { return this.playList.get(this.currentNameIndex).getName(); }

	public void playLatestUserRecording() {
		String path = System.getProperty("user.dir") + "/names/temp/" + getLatestRecordedName() + ".wav";
		this.mediaPlayer = new MediaPlayer(new Media(new File(path).toURI().toString()));
		this.mediaPlayer.play();
	}

	public void recordAttempt(String latestRecordedName) throws InterruptedException, IOException {
		this.latestRecordedName = latestRecordedName;
		String RECORD_NAME_ATTEMPT_COMMAND = "rm names/temp/temp.wav;"
				+ " ffmpeg -f alsa -i default -t 5 -acodec pcm_s16le -ar 22050 -ac 1 ./names/temp/temp.wav 2> /dev/null; " +
				"ffmpeg -hide_banner -i ./names/temp/temp.wav -af silenceremove=0:0:0:-1:2:-45dB ./names/temp/" + this.latestRecordedName + ".wav 2> /dev/null";
		Process process = new ProcessBuilder("/bin/bash", "-c", RECORD_NAME_ATTEMPT_COMMAND).start();
		process.waitFor();
	}

	public void saveAttempt() throws IOException {
		String searchUser = "ls names/user | grep -i [[:digit:]]_" + getFileNamePart(getCurrentPlaylistName()) + ".wav";
		Process process = new ProcessBuilder("/bin/bash", "-c", searchUser).start();
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String creation;
		while ((creation = stdoutBuffered.readLine()) != null) {
			String delete = "rm names/user/" + creation;
			ProcessBuilder process2 = new ProcessBuilder("/bin/bash", "-c", delete);
			process2.start();
		}

		// Save current recording in names/user
		String saveCommand = "cp ./names/temp/" + this.latestRecordedName + ".wav ./names/user";
		ProcessBuilder process3 = new ProcessBuilder("/bin/bash", "-c", saveCommand);
		process3.start();
	}

	public String getLatestRecordedName() {
		return this.latestRecordedName;
	}

	public void compareRecordings(int count) {
		if (count == 0) {
			return;
		}

		count--;
		this.mediaPlayer = new MediaPlayer(new Media(getCurrentWav().toURI().toString()));
		this.mediaPlayer.play();

		int finalCount = count;
		this.mediaPlayer.setOnEndOfMedia(() -> {
			String path = System.getProperty("user.dir") + "/names/temp/" + this.latestRecordedName + ".wav";
			mediaPlayer = new MediaPlayer(new Media(new File(path).toURI().toString()));
			mediaPlayer.play();

			mediaPlayer.setOnEndOfMedia(() -> compareRecordings(finalCount));
		});
	}

	public void playPastRecording(double volume) throws IOException {
		String searchUser = "ls names/user/*[0-9]_" + getFileNamePart(getCurrentPlaylistName()) + ".wav";
		Process process = new ProcessBuilder("/bin/bash", "-c", searchUser).start();
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String line = stdoutBuffered.readLine();
		if(line == null){
			this.alert.noPastRecording();
		}else {
			mediaPlayer = new MediaPlayer(new Media(new File(line).toURI().toString()));
			mediaPlayer.setVolume(volume);
			mediaPlayer.play();
		}
	}
}
