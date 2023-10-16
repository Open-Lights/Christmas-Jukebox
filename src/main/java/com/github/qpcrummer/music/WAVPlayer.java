package com.github.qpcrummer.music;

import com.drew.lang.annotations.NotNull;
import com.github.qpcrummer.Main;
import com.github.qpcrummer.beat.BeatManager;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class WAVPlayer {
    private Clip wavClip;
    private long currentPosition;
    private FloatControl volume;
    private boolean playing;
    private boolean looping;
    private final JProgressBar progressBar;
    private int index = 0;
    private final JList<Path> songJList;
    private final ListSelectionListener listener;
    private final JFrame parent;
    private final BeatManager beatManager;
    private int progressBarIndex;
    private String cachedFinalTimeStamp;
    private int songLengthSeconds;
    private final int[] indexes;
    public WAVPlayer(@NotNull final JProgressBar bar, @NotNull final JList<Path> songJList, final ListSelectionListener songJListListener, final JFrame parent) {
        this.progressBar = bar;
        this.songJList = songJList;
        this.listener = songJListListener;
        this.parent = parent;
        // Beats
        this.beatManager = new BeatManager(this);

        // ProgressBarUpdater
        this.beatManager.getBeatExecutor().scheduleAtFixedRate(() -> {
            if (isPlaying()) {
                progressBar.setValue((progressBarIndex * 100)/ this.songLengthSeconds);
                progressBar.setString(formatTime(progressBarIndex) + "/" + this.cachedFinalTimeStamp);
                progressBarIndex++;
            }
        }, 0, 1, TimeUnit.SECONDS);

        this.indexes = new int[songJList.getModel().getSize()];
        for (int i = 0; i < songJList.getModel().getSize(); i++) {
            indexes[i] = i;
        }
    }

    /**
     * Plays the selected clip
     */
    public void play(final int index) {
        // Create AudioInputStream and Clip Objects
        this.index = this.indexes[index];
        this.updateSelectedValue();
        this.parent.setTitle("Playing " + this.getTitle(this.index));
        final String wavPath = String.valueOf(this.getPath(this.index));
        try (final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(wavPath).getAbsoluteFile())) {
            this.wavClip = AudioSystem.getClip();
            this.wavClip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            Main.logger.warning("AudioSystem failed to start!");
        }
        // Set Volume
        this.volume = (FloatControl) this.wavClip.getControl(FloatControl.Type.MASTER_GAIN);

        if (!playing) {
            // Add song finished listener
            this.wavClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    this.onSongEnd();
                }
            });

            // Set data for ProgressBar
            this.songLengthSeconds = (int) TimeUnit.MICROSECONDS.toSeconds(wavClip.getMicrosecondLength());
            this.cachedFinalTimeStamp = formatTime(songLengthSeconds);

            // Set Playing True
            this.playing = true;
        }

        this.beatManager.readBeats(this.getPath(this.index), this.index);

        // Start the Music!!!
        this.wavClip.start();

        // Start Beat Tracking
        this.beatManager.startBeatTracking();
    }

    /**
     * Resumes the selected audio clip at the time when it stopped
     */
    public void resume() {
        if (this.wavClip == null) {
            play(getCurrentSong());
            return;
        }
        this.wavClip.setMicrosecondPosition(this.currentPosition);
        this.wavClip.start();
        this.playing = true;
        this.beatManager.onResume();
    }

    /**
     * Pauses the selected audio clip
     */
    public void pause() {
        this.currentPosition = wavClip.getMicrosecondPosition();
        this.wavClip.stop();
        this.playing = false;
    }

    /**
     * Cancels and resets the audio clip
     */
    public void reset() {
        if (playing) {
            this.wavClip.stop();
            this.wavClip.close();
        }
        // TODO This probably doesn't need to be set to null
        this.wavClip = null;
        this.playing = false;
        this.currentPosition = 0L;
        this.progressBarIndex = 0;
    }

    /**
     * Completely removes all threads and data related to the Jukebox
     */
    public void shutDown() {
        reset();
        this.beatManager.stopThread();
    }

    /**
     * Skips the song and moves to the next
     */
    public void skip() {
        this.reset();
        this.play(this.getNextSong());
    }

    /**
     * Restarts a Clip from the beginning
     */
    public void rewind() {
        this.pause();
        this.currentPosition = 0L;
        this.progressBarIndex = 0;
        this.beatManager.onRewind();
        this.resume();
    }

    /**
     * Mixes up the order of Songs
     */
    public void shuffle() {
        this.reset();
        final int length = this.indexes.length;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            int randomIndexToSwap = random.nextInt(length);
            int temp = this.indexes[randomIndexToSwap];
            this.indexes[randomIndexToSwap] = this.indexes[i];
            this.indexes[i] = temp;
        }
        this.play(0);
    }

    /**
     * Index when clicked on in the JLIst
     * @param index index of the song clicked on
     */
    public void songOverride(final int index) {
        this.reset();
        this.play(index);
    }

    /**
     * Correctly format the progress bar
     * @param seconds Current position of the song in seconds
     * @return The formatted time
     */
    private String formatTime(final int seconds) {
        final Calendar timeFormat = new Calendar.Builder().build();
        timeFormat.set(Calendar.SECOND, seconds);

        String second;
        if (timeFormat.get(Calendar.SECOND) <= 9) {
            second = 0 + String.valueOf(timeFormat.get(Calendar.SECOND));
        } else {
            second = String.valueOf(timeFormat.get(Calendar.SECOND));
        }

        String min;
        if (timeFormat.get(Calendar.MINUTE) <= 9) {
            min = 0 + String.valueOf(timeFormat.get(Calendar.MINUTE));
        } else {
            min = String.valueOf(timeFormat.get(Calendar.MINUTE));
        }
        return min + ":" + second;
    }

    /**
     * Executes when a song has completed
     */
    private void onSongEnd() {
        if (this.wavClip.getFrameLength() <= this.wavClip.getLongFramePosition()) {
            if (this.looping) {
                this.wavClip.setFramePosition(0);
                this.beatManager.resetBeats();
                this.progressBarIndex = 0;
                this.resume();
            } else {
                this.skip();
            }
        }
    }

    /**
     * Updates the selected index of the JList
     */
    private void updateSelectedValue() {
        this.songJList.removeListSelectionListener(this.listener);
        this.songJList.setSelectedValue(this.getPath(this.getCurrentSong()), true);
        this.songJList.addListSelectionListener(this.listener);
    }

    // Info Methods

    /**
     * Enables looping WAV files
     * @param setLooping boolean toggle
     */
    public void setLooping(final boolean setLooping) {
        this.looping = setLooping;
    }

    /**
     * Checks if a WAV file is playing
     * @return if WAV is playing
     */
    public boolean isPlaying() {
        return this.playing;
    }

    /**
     * Gets the current position of a paused song
     * @return current position as a long
     */
    public long getCurrentPosition() {
        if (this.isPlaying()) {
            return this.getWavClip().getMicrosecondPosition();
        } else {
            return this.currentPosition;
        }
    }

    /**
     * Grabs the next song's index. If it is at the end, it goes to the beginning
     * @return Next song's index to play
     */
    public int getNextSong() {
        if (this.index >= this.indexes.length - 1) {
            return 0;
        } else {
            this.index++;
            return this.index;
        }
    }

    /**
     * Gets the current song playing
     * @return Returns the current song's index
     */
    public int getCurrentSong() {
        return this.index;
    }

    /**
     * Gets the current Clip playing
     * @return Returns current Clip
     */
    public Clip getWavClip() {
        return this.wavClip;
    }

    /**
     * Calculates the volume based on slider
     * @param sliderValue JSlider value
     */
    public void calcVolume(final double sliderValue) {
        double newVolume;
        if (sliderValue == 0) {
            newVolume = -80;
        } else {
            newVolume = 30 * Math.log10(sliderValue) - 60;
        }
        this.volume.setValue((float) newVolume);
    }

    /**
     * Gets the name of the Song at the specific index
     * @param index index in songJList
     * @return Name and Author as a String
     */
    public String getTitle(int index) {
        Component renderedComponent = this.songJList.getCellRenderer().getListCellRendererComponent(this.songJList, this.songJList.getModel().getElementAt(index), index, false, false);
        return ((JLabel) renderedComponent).getText();
    }

    /**
     * Gets the path of the Song at the specific index
     * @param index index in songJList
     * @return Path
     */
    public Path getPath(int index) {
        return this.songJList.getModel().getElementAt(index);
    }
}
