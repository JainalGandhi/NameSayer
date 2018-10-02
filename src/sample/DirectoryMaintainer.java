package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

public class DirectoryMaintainer {

    public DirectoryMaintainer() { }

    private Pattern pattern = Pattern.compile("se206_(\\d+)-(\\d+)-(\\d+)_(\\d+)-(\\d+)-(\\d+)_(.+).wav");

    public void create() throws InterruptedException, IOException {
        String CREATE_DIR = "mkdir -p ./names ./names/temp ./names/user ./names/database; touch ./names/ratings.txt";
        Process process = new ProcessBuilder("/bin/bash", "-c", CREATE_DIR).start();
        process.waitFor();
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

    public void deleteBadFiles() throws InterruptedException, IOException {
        String CREATE_DIR = "mkdir ./names/database/hello";
        System.out.println(CREATE_DIR);
        Process process = new ProcessBuilder("/bin/bash", "-c", CREATE_DIR).start();
        process.waitFor();
    }
}
