package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NamesCollection {

    private List<String> allNamesLower = new ArrayList<>();
    private List<String> allNamesFirstCap = new ArrayList<>();

    public NamesCollection() {}

    /**
     * Get all names from the database, format
     */
    public void solveAllNames() throws IOException {
        //Gets unique names from database
        String LIST_DATABASE_FILES_COMMAND = "ls -l ./names/database | awk '{split($NF, a, \"[_.]\"); print a[4]}' | sed 1d | sort -u";
        Process process = new ProcessBuilder("/bin/bash", "-c", LIST_DATABASE_FILES_COMMAND).start();
        InputStream stdout = process.getInputStream();
        BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
        String line;
        while ((line = stdoutBuffered.readLine()) != null ) {
            if(!allNamesLower.contains(line.toLowerCase())) {
                this.allNamesLower.add(line);
                this.allNamesFirstCap.add(line.substring(0, 1).toUpperCase() + line.substring(1));
            }
        }
    }

    /**
     * Return all names in the database
     */
    public List<String> getAllNamesLower() {
        return this.allNamesLower;
    }

    public List<String> getAllNamesFirstCap() {
        return this.allNamesFirstCap;
    }

}
