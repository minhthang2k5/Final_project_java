package client.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private TargetDataLine targetLine;
    private File audioFile;
    private boolean isRecording = false;
    private long startTime;

    public void startRecording(File file) throws LineUnavailableException {
        this.audioFile = file;
        AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported on this system.");
        }

        targetLine = (TargetDataLine) AudioSystem.getLine(info);
        targetLine.open(format);
        targetLine.start();
        isRecording = true;
        startTime = System.currentTimeMillis();

        Thread recordThread = new Thread(() -> {
            AudioInputStream audioStream = new AudioInputStream(targetLine);
            try {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        recordThread.start();
    }

    public long stopRecording() {
        if (targetLine != null && isRecording) {
            targetLine.stop();
            targetLine.close();
            isRecording = false;
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
}
