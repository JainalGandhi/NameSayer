package sample.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DirectoryMaintainer {

    public DirectoryMaintainer() { }

    private Pattern pattern = Pattern.compile("se206_(\\d+)-(\\d+)-(\\d+)_(\\d+)-(\\d+)-(\\d+)_(.+).wav");

    public void create() throws InterruptedException, IOException {
        String CREATE_DIR = "mkdir -p ./names ./pastplaylists ./names/temp ./names/user ./names/database; touch ./names/badQualityRecordings.txt";
        Process process = new ProcessBuilder("/bin/bash", "-c", CREATE_DIR).start();
        process.waitFor();
    }

    public void clearTempDirectory() throws IOException {
        String CREATE_DIR = "rm -r ./names/temp; mkdir -p ./names/temp";
        ProcessBuilder process = new ProcessBuilder("/bin/bash", "-c", CREATE_DIR);
        process.start();
    }

    public boolean copyFileList(List<File> allFiles) throws IOException {
        boolean badFiles = false;
        String copyTo = System.getProperty("user.dir") + "/names/database/";
        for(File file : allFiles) {
            if(!Files.exists(Paths.get(copyTo + file.getName()))) {
                if(pattern.matcher(file.getName()).matches()) {
                    Files.copy(Paths.get(file.toURI()), Paths.get(copyTo + file.getName()));
                }else {
                    badFiles = true;
                }
            }
        }
        return badFiles;
    }

    public boolean copyWavFiles(File poo) throws IOException {
        boolean badFiles = false;
        String copyTo = System.getProperty("user.dir") + "/names/database/";
        for(Path file : findWavs(poo)) {
            if(!Files.exists(Paths.get(copyTo + file.toFile().getName()))) {
                if(pattern.matcher(file.toFile().getName()).matches()) {
                    Files.copy(Paths.get(file.toFile().toURI()), Paths.get(copyTo + file.toFile().getName()));
                }else {
                    badFiles = true;
                }
            }
        }
        return badFiles;
    }

    private static Path[] findWavs(File directory) throws IOException {
        Path dir = directory.toPath();
        try (Stream<Path> stream = Files.find(dir, Integer.MAX_VALUE, (path, attributes) -> path.getFileName().toString().endsWith(".wav"))) {
            return stream.toArray(Path[]::new);
        }
    }

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
