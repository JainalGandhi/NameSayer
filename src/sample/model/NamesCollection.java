package sample.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NamesCollection {

    private List<String> allNamesFirstCap = new ArrayList<>();

    /**
     * Gets all names of files in the database in decoded format
     */
    public NamesCollection() {}

    /**
     * Get all unique names from the database and stores in local fields. Extracts into lower and upper first letter form
     */
    public void solveAllNames() throws IOException {
        String LIST_DATABASE_FILES_COMMAND = "ls -l ./names/database | awk '{split($NF, a, \"[_.]\"); print a[4]}' | sed 1d | sort -u";
        Process process = new ProcessBuilder("/bin/bash", "-c", LIST_DATABASE_FILES_COMMAND).start();
        InputStream stdout = process.getInputStream();
        BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
        String line;
        while ((line = stdoutBuffered.readLine()) != null ) {
            //Adds name to list if does not exist
            if(!this.allNamesFirstCap.contains(line.substring(0, 1).toUpperCase() + line.substring(1).toLowerCase())) {
                this.allNamesFirstCap.add(line.substring(0, 1).toUpperCase() + line.substring(1).toLowerCase());
            }
        }
    }

    /**
     * Return all names in the database
     */
    public List<String> getAllNamesFirstCap() {
        return this.allNamesFirstCap;
    }

}
