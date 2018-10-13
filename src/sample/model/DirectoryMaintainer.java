package sample.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DirectoryMaintainer {

    /**
     * Maintains the file/directory structure of the stored files and manages writing of bad quality files into badQualityRecordings.txt
     */
    public DirectoryMaintainer() { }

    private Pattern pattern = Pattern.compile("se206_(\\d+)-(\\d+)-(\\d+)_(\\d+)-(\\d+)-(\\d+)_(.+).wav");

    /**
     * Creates the required names, /names/temp, /names/user, /names/database, /pastplaylists directories and badQualityRecordings.txt file
     * @throws InterruptedException error thrown
     * @throws IOException error thrown
     */
    public void create() throws InterruptedException, IOException {
        String CREATE_DIR = "mkdir -p ./names ./pastplaylists ./names/temp ./names/user ./names/database; touch ./names/badQualityRecordings.txt";
        Process process = new ProcessBuilder("/bin/bash", "-c", CREATE_DIR).start();
        process.waitFor();
    }

    /**
     * Clears the temp directory of unnecessary files
     * @throws IOException error thrown
     */
    public void clearTempDirectory() throws IOException {
        String CREATE_DIR = "rm -r ./names/temp; mkdir -p ./names/temp";
        ProcessBuilder process = new ProcessBuilder("/bin/bash", "-c", CREATE_DIR);
        process.start();
    }

    /**
     * Copies the wav files inside a given directory into the database file
     * @param directory the directory searching in
     * @return boolean result suggesting if names were validly named
     * @throws IOException error thrown
     */
    public boolean copyWavFiles(File directory) throws IOException {
        boolean badFiles = false;
        String copyTo = System.getProperty("user.dir") + "/names/database/";
        for(Path path : findWavs(directory)) {
            if(!Files.exists(Paths.get(copyTo + path.toFile().getName()))) {
                if(pattern.matcher(path.toFile().getName()).matches()) {
                    //file is of valid naming convention so is added to the database
                    Files.copy(Paths.get(path.toFile().toURI()), Paths.get(copyTo + path.toFile().getName()));
                }else {
                    //warns user that one or more file did not follow naming convention
                    badFiles = true;
                }
            }
        }
        return badFiles;
    }

    /**
     * Gets the path of all wav files contained in an inputted directory
     * @param directory the directory searching in
     * @return all paths of wav files contained in directory
     * @throws IOException error thrown
     */
    private static Path[] findWavs(File directory) throws IOException {
        Path dir = directory.toPath();
        try (Stream<Path> stream = Files.find(dir, Integer.MAX_VALUE, (path, attributes) -> path.getFileName().toString().endsWith(".wav"))) {
            return stream.toArray(Path[]::new);
        }
    }

    /**
     * Writes the inputted path into the badQualityRecordings.txt file to symbolise it is of bad quality
     * @param path the path of the bad quality file
     */
    public void writeBadQuality(String path) {
        Runnable task = new Thread( ()-> {
            String ADD_RATING_TO_DOCUMENT = "if [[ $(grep \"" + path + "\" ./names/badQualityRecordings.txt | wc -l) -eq 0 ]]; then echo \"" + path + "\" >> ./names/badQualityRecordings.txt; fi";
            try {
                ProcessBuilder process = new ProcessBuilder("/bin/bash", "-c", ADD_RATING_TO_DOCUMENT);
                process.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        new Thread(task).start();
    }

}
