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

	private List<String> playlistNames = new ArrayList<>();
	private PopupAlert alert = new PopupAlert();
	private int currentNameIndex;
	private List<File> wavFilesPlaylist = new ArrayList<>();
	private List<PlayListItem> playList = new ArrayList<>();
	private List<PlayListItem> playListOriginal = new ArrayList<>();
	private String latestRecordedName;
	private MediaPlayer mediaPlayer;

    /**
     * Empty constructor for the Player class
     */
	public Player() {}
	
	private File getCurrentWav() {
		return playList.get(currentNameIndex).getWav();
	}

	/**
	 * Forms the initial playlist from the inputted text
	 * @param text
	 * @throws IOException
	 */
	public void formPlaylist(String text) throws IOException {
		playList.clear();
		playlistNames.clear();
		playListOriginal.clear();
		//Splits text by newline characters
		String[] splitText = text.split("\n");
		if (splitText.length == 0) {
			//No name was inputted
			alert.noNameSelected();
		} else {
			//Adding all items to the playlist and forming audio files
			playlistNames.addAll(Arrays.asList(splitText));
			currentNameIndex = 0;
			createWavFiles();
			playListOriginal.addAll(playList);
		}
	}

	/**
	 * Adds a single name to the currently created playlist
	 * @param name name to add to playlist
	 * @throws IOException
	 */
	public void addToPlaylist(String name) throws IOException {
		playlistNames.add(name);
		createSingleWavFile(name);
		playListOriginal.add(playList.get(playList.size()-1));
	}

	/**
	 * Randomises the order of the items in the playlist
	 */
	public void shufflePlayList() {
		Collections.shuffle(playList);
	}

	/**
	 * Re-orders playlist into original order to unshuffle
	 */
	public void orderPlayList() {
		playList.clear();
		playList.addAll(playListOriginal);
	}

	/**
	 * Increases currentNameIndex by 1 to emulate going to next name in playlist
	 */
	public void nextName() {
		int size = playList.size();
		if (currentNameIndex == size - 1) {
			currentNameIndex = 0;
		}
		else {
			currentNameIndex++;
		}
	}

	/**
	 * Reduces currentNameIndex by 1 to emulate going to previous name in playlist
	 */
	public void prevName() {
		int size = playList.size();
		if (currentNameIndex == 0) {
			currentNameIndex = size-1;
		}
		else {
			currentNameIndex--;
		}
	}

	/**
	 * Returns now playing text to be displayed to the user to represent the current name in the playlist.
	 * This includes warnings of missing or invalid names.
	 * @return string for now playing label
	 */
	public String getNowPlaying() {
		return playList.get(currentNameIndex).getNowPlayingText();
	}

	/**
	 * Returns whether or not the current name is valid (true if invalid, false if valid)
	 * @return boolean value associated with the current name being invalid
	 */
	public boolean currentNameNotValid() {
		return playList.get(currentNameIndex).disablePractice;
	}

	/**
	 * Creates all the wav files for the playlist
	 * @throws IOException
	 */
	private void createWavFiles() throws IOException {
		for (String name : playlistNames) {
			// Name needs concatenation
			createSingleWavFile(name);
		}
	}

	/**
	 * Segments/splits the name into array of type string. Split on occurances of -,_ or space
	 * @param name the name to split
	 * @return the substrings in an array - each element is a split
	 */
	public String[] getSegmentedNames(String name) {
		return name.split("[ \\-_]");
	}

	/**
	 * Returns string in file format. Replaces all occurances of spaces and hyhens with underscore
	 * @param name the name to convert string
	 * @return the file format string name
	 */
	public String getFileNamePart(String name) {
		name = name.replace(" ", "_");
		name = name.replace("-", "_");
		return name;
	}

	/**
	 * Creates the wa file associated with the inputted name. Concatenates individual names if required into single file
	 * @param name the name to create wav file of
	 * @throws IOException
	 */
	private void createSingleWavFile(String name) throws IOException {
		PlayListItem item = new PlayListItem(name);
		if (name.contains(" ") || name.contains("-") || name.contains("_")) {
			String[] names = getSegmentedNames(name);
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

	/**
	 * Creates the required file for a name that requires concatenation
	 * @param name the name of the resulting file
	 */
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

	/**
	 * Creates the concatenation text file used in the ffmpeg command. Each line is a path of the concatenated file.
	 * @param path the path to add to the text file
	 * @param name the name to call the text file
	 */
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

	/**
	 * Deletes the concat file from the temp directory
	 * @param name path to concat file
	 */
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

	/**
	 * Creates the file path of the inputted name. Takes into account the rating of the files to avoid badly rated names
	 * from being added to the concatenation.
	 * @param name the name to create the file path of
	 * @return the path of the name
	 * @throws IOException
	 */
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
		} else {
			// Because we don't make available to option for deletion, this else statement should never be reached
			String commandUser = "ls names/user | grep -i _" + name;
			Process processUser = new ProcessBuilder("/bin/bash", "-c", commandUser).start();
			InputStream stdoutUser = processUser.getInputStream();
			BufferedReader stdoutBufferedUser = new BufferedReader(new InputStreamReader(stdoutUser));
			String creationUser;
			if ((creationUser = stdoutBufferedUser.readLine()) != null) {
				return System.getProperty("user.dir") + "/names/user/" + creationUser;
			} else {
				// no such recording
				return "";
			}
		}
	}

    /**
     * Gets the name from the current index in the playlist
     * @return current name
     */
	public String getCurrentPlaylistName() {
	    return this.playList.get(this.currentNameIndex).getName();
	}

    /**
     * Plays the concatenated audio file of the current name to the user
     * @param volume the volume to set the mediaPlayer to and play the audio file with
     */
    public void playCurrentName(double volume) {
        stopAudioPlayback();
        File file = playList.get(currentNameIndex).getWav();
        Runnable task = new Thread( ()-> {
            try {
                //Attempts to play the audio file
                mediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
                mediaPlayer.setVolume(volume);
                mediaPlayer.play();
                mediaPlayer.setOnError( ()-> {
                    try {
                        //If error occurs, tries to format the wav file to get into correct format for playing
                        String RECORD_NAME_ATTEMPT_COMMAND = "ffmpeg -y -i " + file.toString() + " " + file.toString();
                        Process process = new ProcessBuilder("/bin/bash", "-c", RECORD_NAME_ATTEMPT_COMMAND).start();
                        process.waitFor();
                        Platform.runLater(() -> {
                            //Reattempts playing the audio file
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

    /**
     * Finds the latest user wav recorded file of the current name and plays it to the user
     * @param volume the volume to set the mediaPlayer to and play the audio file with
     * @throws IOException
     */
	public void playPastRecording(double volume) throws IOException {
		String searchUser = "ls names/user/*[0-9]_" + getFileNamePart(getCurrentPlaylistName()) + ".wav";
		Process process = new ProcessBuilder("/bin/bash", "-c", searchUser).start();
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String line = stdoutBuffered.readLine();
		if(line == null){
		    //Alerts user that no past recording exists
			this.alert.noPastRecording();
		}else {
		    //Plays the file to the user
			mediaPlayer = new MediaPlayer(new Media(new File(line).toURI().toString()));
			mediaPlayer.setVolume(volume);
			mediaPlayer.play();
		}
	}

    /**
     * Records the users attempt at saying the current name. Records a 5 second audio clip and removes any silence from it.
     * Clip saved in /names/temp as a file named after the inputted String.
     * @param latestRecordedName the file name (incl formatted date) for how to call the file
     * @throws InterruptedException
     * @throws IOException
     */
    public void recordAttempt(String latestRecordedName) throws IOException, InterruptedException {
        this.latestRecordedName = latestRecordedName;
        String RECORD_NAME_ATTEMPT_COMMAND = "rm names/temp/temp.wav;"
                + " ffmpeg -f alsa -i default -t 15 -acodec pcm_s16le -ar 22050 -ac 1 ./names/temp/temp.wav &>/dev/null";
        Process process = new ProcessBuilder("/bin/bash", "-c", RECORD_NAME_ATTEMPT_COMMAND).start();
        process.waitFor();
        RECORD_NAME_ATTEMPT_COMMAND = "ffmpeg -hide_banner -i ./names/temp/temp.wav -af silenceremove=0:0:0:-1:2:-45dB ./names/temp/" + this.latestRecordedName + ".wav 2> /dev/null";
        process = new ProcessBuilder("/bin/bash", "-c", RECORD_NAME_ATTEMPT_COMMAND).start();
        process.waitFor();
    }

    public void stopRecordAttempt() throws InterruptedException, IOException {
        String STOP_RECORDING = "killall -s SIGINT ffmpeg";
        Process process = new ProcessBuilder("/bin/bash", "-c", STOP_RECORDING).start();
        process.waitFor();
    }

    /**
     * Plays the latest user attempt at saying the name
     */
    public void playLatestUserRecording() {
        String path = System.getProperty("user.dir") + "/names/temp/" + this.latestRecordedName + ".wav";
        this.mediaPlayer = new MediaPlayer(new Media(new File(path).toURI().toString()));
        this.mediaPlayer.play();
    }

    /**
     * Saves the latest recorded name to a local directory to allow relisten upon user request. This directory is
     * permanently saved so name can be relistened to after program exit.
     * Will delete any other saved attempt of the name as only one version ever needed for playback as per requirements
     * @throws IOException
     */
    public void saveAttempt() throws IOException {
        //Deletes any current saved attempts for that name
        String searchUser = "ls names/user | grep -i [[:digit:]]_" + getFileNamePart(getCurrentPlaylistName()) + ".wav";
        Process process = new ProcessBuilder("/bin/bash", "-c", searchUser).start();
        InputStream stdout = process.getInputStream();
        BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
        String creation;
        while ((creation = stdoutBuffered.readLine()) != null) {
            //Deletes each name
            String delete = "rm names/user/" + creation;
            ProcessBuilder process2 = new ProcessBuilder("/bin/bash", "-c", delete);
            process2.start();
        }

        // Save current recording in names/user by copying recorded audio file from names/temp folder
        String saveCommand = "cp ./names/temp/" + this.latestRecordedName + ".wav ./names/user";
        ProcessBuilder process3 = new ProcessBuilder("/bin/bash", "-c", saveCommand);
        process3.start();
    }

    /**
     * Allows the user to compare the database version of the current name with their user recording a specified number
     * of times. Plays the database version, followed by the user recording an inputted number of times.
     * This is done recursively. On the completion of one audio file being complete, the next audio file is
     * linked to play
     * @param count the number of times to play the recording
     */
    public void compareRecordings(int count) {
        //Recursive criteria to mark the end of comparing names
        if (count == 0) {
            return;
        }

        //Decrements count and then plays database version, followed user recording version. Then calls method again
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

	/**
	 * Stops any audio currently playing by the mediaplayer
	 */
	public void stopAudioPlayback() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}
}
