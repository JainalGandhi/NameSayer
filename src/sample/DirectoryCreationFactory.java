package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DirectoryCreationFactory {

    public DirectoryCreationFactory() { }

    public void create() throws InterruptedException, IOException {
        String CREATE_DIR = "mkdir -p ./names ./names/temp ./names/user ./names/database; touch ./names/ratings.txt";
        Process process = new ProcessBuilder("/bin/bash", "-c", CREATE_DIR).start();
        process.waitFor();
    }

    public void copyFileList(List<File> allFiles) throws IOException {
        String copyTo = System.getProperty("user.dir") + "/names/database/";
        for(File file : allFiles) {
            if(!Files.exists(Paths.get(copyTo + file.getName()))) {
                Files.copy(Paths.get(file.toURI()), Paths.get(copyTo + file.getName()));
            }
        }
    }
}
