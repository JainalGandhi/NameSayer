package sample.model;

import java.io.IOException;

public class QualityMaintainer {

    public QualityMaintainer() { }

    public void writeBadQuality(String path) {
        Runnable task = new Thread( ()-> {
            String ADD_RATING_TO_DOCUMENT = "if [[ $(grep \"" + path + "\" ./names/badQualityRecordings.txt | wc -l) -eq 0 ]]; then echo \"" + path + "\" >> ./names/badQualityRecordings.txt; fi";
            System.out.println(ADD_RATING_TO_DOCUMENT);
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
