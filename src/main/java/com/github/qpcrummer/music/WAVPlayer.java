package com.github.qpcrummer.music;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.github.qpcrummer.directories.Song;
import com.github.qpcrummer.gui.FFTDebugGUI;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WAVPlayer {
    private final FFTDebugGUI debugGUI;
    private Clip wavClip;
    private long currentPosition;
    private AudioInputStream audioInputStream;
    private FloatControl volume;
    private boolean playing;
    private boolean looping;
    private final ScheduledExecutorService thread = Executors.newScheduledThreadPool(1);
    private final JProgressBar progressBar;
    private final List<Song> playList;
    private Song currentSong;
    private int index;
    public WAVPlayer(@Nullable FFTDebugGUI debugGUI, @NotNull JProgressBar bar, List<Song> playList) {
        this.debugGUI = debugGUI;
        this.progressBar = bar;
        this.playList = playList;
    }

    /**
     * Plays the selected clip
     */
    public void play(Song song) {
        // Create AudioInputStream and Clip Objects
        this.currentSong = song;
        String wavPath = String.valueOf(song.path);
        try {
            this.audioInputStream = AudioSystem.getAudioInputStream(new File(wavPath).getAbsoluteFile());
            this.wavClip = AudioSystem.getClip();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        // Start DebugGUI
        // TODO Implement more DebugGUI workings
        if (this.debugGUI != null) {
            this.debugGUI.startTracking(wavPath);
        }
        // Set Volume
        this.volume = (FloatControl) this.wavClip.getControl(FloatControl.Type.VOLUME);
        // Start Song Update Thread
        if (!playing) {
            startThread();
            // Set Playing True
            this.playing = true;
        }

        // Start the Music!!!
        this.wavClip.start();
    }

    /**
     * Resumes the selected audio clip at the time when it stopped
     */
    public void resume() {
        this.startThread();
        this.wavClip.setMicrosecondPosition(this.currentPosition);
        this.wavClip.start();
        this.playing = true;
    }

    /**
     * Pauses the selected audio clip
     */
    public void pause() {
        this.thread.shutdown();
        this.currentPosition = wavClip.getMicrosecondPosition();
        this.wavClip.stop();
        this.playing = false;
    }

    /**
     * Cancels and resets the audio clip
     */
    public void stop() {
        this.thread.shutdownNow();
        this.wavClip.stop();
        this.wavClip.close();
        this.wavClip = null;
        this.playing = false;
        this.currentPosition = 0L;
    }

    /**
     * Skips the song and moves to the next
     */
    public void skip() {
        this.stop();
        this.play(this.getNextSong());
    }

    /**
     * Increments the ProgressBar
     */
    private void updateProgressBar() {
        this.progressBar.setValue(Math.toIntExact(this.wavClip.getLongFramePosition()));
        this.progressBar.setString(formatTime(this.wavClip.getMicrosecondPosition()) + "/" + formatTime(this.wavClip.getMicrosecondLength()));
    }

    /**
     * Correctly format the progress bar
     * @param microseconds Current position of the song in microseconds
     * @return The formatted time
     */
    private String formatTime(long microseconds) {
        final int seconds = (int) TimeUnit.MICROSECONDS.toSeconds(microseconds);
        final Calendar time_format = new Calendar.Builder().build();
        time_format.set(Calendar.SECOND, seconds);

        String second;
        if (time_format.get(Calendar.SECOND) <= 9) {
            second = 0 + String.valueOf(time_format.get(Calendar.SECOND));
        } else {
            second = String.valueOf(time_format.get(Calendar.SECOND));
        }

        String min;
        if (time_format.get(Calendar.MINUTE) <= 9) {
            min = 0 + String.valueOf(time_format.get(Calendar.MINUTE));
        } else {
            min = String.valueOf(time_format.get(Calendar.MINUTE));
        }
        return min + ":" + second;
    }

    /**
     * Starts the WAV Watcher Thread
     */
    private void startThread() {
        this.thread.scheduleAtFixedRate(() -> {
            if (this.wavClip.getFrameLength() <= this.wavClip.getLongFramePosition()) {
                if (looping) {
                    this.wavClip.setFramePosition(0);
                    this.resume();
                } else {
                    skip();
                }
            }

            if (this.isWavLoaded()) {
                updateProgressBar();
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    // Info Methods

    /**
     * Gets how long a Clip is in Frames
     * @return Number of Frames in the selected Clip
     */
    public long getClipLengthFrames() {
        return this.wavClip.getFrameLength();
    }

    /**
     * Gets how long a Clip is in Microseconds
     * @return How long the selected Clip is in Microseconds
     */
    public long getClipLengthMicroSeconds() {
        return this.wavClip.getMicrosecondLength();
    }

    /**
     * Enables looping WAV files
     * @param setLooping boolean toggle
     */
    public void setLooping(boolean setLooping) {
        this.looping = setLooping;
    }

    /**
     * Checks if it should run the Play method or the Resume method
     * @return If the WAV Clip exists
     */
    public boolean isWavLoaded() {
        return this.wavClip != null;
    }

    /**
     * Checks if a WAV file is playing
     * @return if WAV is playing
     */
    public boolean isPlaying() {
        return this.playing;
    }

    /**
     * Grabs the next song in the List. If it is at the end, it goes to the beginning
     * @return Next song to play
     */
    public Song getNextSong() {
        this.index++;
        if (this.index >= this.playList.size()) {
            this.index = 0;
        }
        return this.playList.get(this.index);
    }

    /**
     * Gets the current song playing
     * @return Returns the current song
     */
    public Song getCurrentSong() {
        return this.currentSong;
    }

    /**
     * Gets the current Clip playing
     * @return Returns current Clip
     */
    public Clip getWavClip() {
        return this.wavClip;
    }
}
