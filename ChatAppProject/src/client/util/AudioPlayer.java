package client.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {
    private Clip clip;

    public void play(File audioFile, Runnable onComplete) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        
        AudioFormat format = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        clip = (Clip) AudioSystem.getLine(info);
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        clip.open(audioStream);
        clip.start();
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}
