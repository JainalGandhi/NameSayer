package sample.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayListItem {
	
	private String name;
	private File wav;
	private List<String> warnings = new ArrayList<String>();
	private int names;
	
	public PlayListItem(String name) {
		this.name = name;
	}
	
	public void addWarning(String warning) {
		warnings.add(warning);
	}
	
	public void setWav(File wav) {
		this.wav = wav;
	}
	
	public void setNamesAmount(int names) {
		this.names = names;
	}
	
	public String getNowPlayingText() {
		String text = "Now Playing: ";
		if (warnings.size() >= names) {
			text = text + name + "     Warning: Could not find or create name";
		}
		else if (warnings.size() >= 1) {
			text = text + name + getWarnings();
		}
		else {
			text = text + name;
		}
		return text;
	}
	
	public String getWarnings() {
		String warning = "     Warning: Could not find ";
		int iterator = 0;
		for (String str: warnings) {
			iterator++;
			if (iterator == 1) {
				warning = warning + warnings.get(iterator-1);
			}
			else {
				warning = warning + ", " + warnings.get(iterator-1);
			}
		}
		return warning;
	}

}
