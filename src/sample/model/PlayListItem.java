package sample.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayListItem {
	
	private String name;
	private File wav;
	private List<String> warnings = new ArrayList<>();
	private int names;
	public Boolean disablePractice = false;

	/**
	 * Constructs the playlist item for a given name
	 * @param name name of the item
	 */
	public PlayListItem(String name) {
		this.name = name;
	}

	/**
	 * Adds warning to an item
	 * @param warning item warning
	 */
	public void addWarning(String warning) {
		warnings.add(warning);
	}

	/**
	 * Sets the wav file associated with the item
	 * @param wav file associated with the item
	 */
	public void setWav(File wav) {
		this.wav = wav;
	}

	/**
	 * Returns the wav file
	 * @return wav file
	 */
	public File getWav() {
		return this.wav;
	}

	/**
	 * Sets the number of names that are associated with the item
	 * @param names number of names
	 */
	public void setNamesAmount(int names) {
		this.names = names;
	}

	/**
	 * Returns the name of the playlistitem
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Creates the now playing text for the current playlist item. Takes the name of the item and includes any labels
	 * @return now playing label text
	 */
	public String getNowPlayingText() {
		String text = "Now Playing: ";
		if (warnings.size() >= names) {
			text = text + name + "     Warning: Could not find or create name";
			this.disablePractice = true;
		}
		else if (warnings.size() >= 1) {
			text = text + name + getWarnings();
			this.disablePractice = false;
		}
		else {
			text = text + name;
			this.disablePractice = false;
		}
		return text;
	}

	/**
	 * Returns the warnings associated with the current playlist item
	 * @return warnings for item
	 */
	private String getWarnings() {
		StringBuilder warning = new StringBuilder("     Warning: Could not find ");
		int iterator = 0;
		for (String str: warnings) {
			iterator++;
			if (iterator == 1) {
				warning.append(str);
			}
			else {
				warning.append(", ").append(str);
			}
		}
		return warning.toString();
	}

}