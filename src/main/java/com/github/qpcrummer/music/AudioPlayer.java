package com.github.qpcrummer.music;


import com.github.qpcrummer.Main;
import com.github.qpcrummer.gui.AudioVisualizer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.qpcrummer.Main.*;
import static com.github.qpcrummer.gui.MainGUI.*;

public class AudioPlayer {

    // to store current position
    public static Long currentFrame;
    public static Clip clip;

    // current status of clip
    public static String status;

    public static AudioInputStream audioInputStream;
    public static String filePath;

    public static FloatControl volume;

    /**
     * Main bulk of Audio code
     */
    public static void initMusic() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // create AudioInputStream object
        audioInputStream =
                AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());

        // create clip reference
        clip = AudioSystem.getClip();

        // open audioInputStream to the clip
        clip.open(audioInputStream);

        // set volume
        volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        // set status
        status = "paused";

        // set music_bar
        music_bar.setMaximum(clip.getFrameLength());
        music_bar.setString("00:00" + "/" + timeCalc(clip.getMicrosecondLength()));

        // start thread
        thread.scheduleAtFixedRate(() -> {
            if (clip.getFrameLength() <= clip.getLongFramePosition() && !thread_halt) {
                if (looping) {
                    clip.setFramePosition(0);
                    play();
                } else {
                    try {
                        skip();
                    } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (clip != null) {
                music_bar.setValue(Math.toIntExact(clip.getLongFramePosition()));
                music_bar.setString(timeCalc(clip.getMicrosecondPosition()) + "/" + timeCalc(clip.getMicrosecondLength()));
            }
        }, 0,1, TimeUnit.MILLISECONDS);
    }

    /**
     * Method to play audio
     */
    public static void play()
    {
        //start the clip
        clip.start();

        status = "play";
        play.setText("Playing");
    }

    /**
     * Method to pause audio
     */
    public static void pause()
    {
        if (status.equals("paused")) {
            return;
        }
        currentFrame = clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
        play.setText("Paused");
    }

    /**
     * Method to resume audio
     */
    public static void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (status.equals("play")) {
            return;
        }
        clip.close();
        resetAudioStream();
        if (currentFrame != null) {
            clip.setMicrosecondPosition(currentFrame);
        }
        play();
        play.setText("Playing");
    }

    /**
     * Method to stop/end audio
     */
    public static void stop() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {
        currentFrame = 0L;
        clip.stop();
        clip.close();
    }

    /**
     * Method to restart the audio system
     */
    public static void resetAudioStream() throws UnsupportedAudioFileException, IOException,
            LineUnavailableException
    {
        audioInputStream = AudioSystem.getAudioInputStream(
                new File(filePath).getAbsoluteFile());
        clip.open(audioInputStream);
    }

    /**
     * Method to skip to the next song
     */
    public static void skip() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        thread_halt = true;
        stop();
        musicSetup(current_song = current_songs.get(song_playing).path);
        play();
        thread_halt = false;
    }
}
