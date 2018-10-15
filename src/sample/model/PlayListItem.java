package sample.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayListItem {
	
	private String name;
	private File wav;
	private List<String> warnings = new ArrayList<String>();
	private int names;
	public Boolean disablePractice = false;
	
	public PlayListItem(String name) {
		this.name = name;
	}
	
	public void addWarning(String warning) {
		warnings.add(warning);
	}
	
	public void setWav(File wav) {
		this.wav = wav;
	}
	
	public File getWav() {
		return this.wav;
	}
	
	public void setNamesAmount(int names) {
		this.names = names;
	}

	public String getName() {
		return this.name;
	}

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
	
	public String getWarnings() {
		String warning = "     Warning: Could not find ";
		int iterator = 0;
		for (String str: warnings) {
			iterator++;
			if (iterator == 1) {
				warning = warning + str;
			}
			else {
				warning = warning + ", " + str;
			}
		}
		return warning;
	}

}
