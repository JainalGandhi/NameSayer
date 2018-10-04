package sample.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Determines if the microphone is functioning correctly with tolerable volume levels
 */
public class MicrophoneTester {

	/**
	 * Return the maximum volume detected by the microphone to detect if its functioning
	 */
    public double determineFunctioning() throws IOException, InterruptedException {
        //Records audio and determines mean volume in the audio file
        String TEST_ADUIO_VOLUME_COMMAND ="ffmpeg -y -f alsa -i default -t 0.0075 ./names/temp/test.wav 2> /dev/null; ffmpeg -i ./names/temp/test.wav -af \"volumedetect\" -f null /dev/null 2>&1 | grep mean_volume | awk -F': ' '{print $2}' | cut -d' ' -f1";
        Process process = new ProcessBuilder("/bin/bash", "-c", TEST_ADUIO_VOLUME_COMMAND).start();
        process.waitFor();

        //Buffers standard input from bash script
        BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(process.getInputStream()));

        //Compares recognised loudness with MIN_VOLUME_ALLOWED constant and returns whether meets loudness requirement
        return Double.parseDouble(stdoutBuffered.readLine());
    }
}
