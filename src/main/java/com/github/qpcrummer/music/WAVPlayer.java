package com.github.qpcrummer.music;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.beat.BeatManager;
import com.github.qpcrummer.gui.NewJukeboxGUI;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class WAVPlayer {
    private Clip wavClip;
    private long currentPosition;
    private long songLength;
    private FloatControl volume;
    private boolean playing;
    private boolean looping;
    private int index = 0;
    private final BeatManager beatManager;
    private int[] indexes;
    public WAVPlayer() {
        // Beats
        this.beatManager = new BeatManager(this);
    }

    /**
     * Run this if you change "songPaths"
     */
    public void initialize() {
        this.indexes = new int[NewJukeboxGUI.songPaths.length];
        for (int i = 0; i < NewJukeboxGUI.songPaths.length; i++) {
            indexes[i] = i;
        }

        this.beatManager.initialize();
    }

    /**
     * Plays the selected clip
     */
    public void play(final int index) {
        System.gc();
        // Create AudioInputStream and Clip Objects
        this.index = this.indexes[index];
        this.updateSelectedValue();
        NewJukeboxGUI.title = "Playing " + this.getTitle(this.index);
        try (final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.getPath(this.index).toFile())) {
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
            reset();
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
        NewJukeboxGUI.cachedFormattedSongLength = null;
        this.playing = false;
        this.currentPosition = 0L;
        this.songLength = 0L;
        this.looping = false;
        this.beatManager.resetBeats();
    }

    /**
     * Completely removes all threads and data related to the Jukebox
     */
    public void shutDown() {
        reset();
        this.index = 0;
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
     * Executes when a song has completed
     */
    private void onSongEnd() {
        if (this.wavClip.getFrameLength() <= this.wavClip.getLongFramePosition()) {
            if (this.looping) {
                this.wavClip.setFramePosition(0);
                this.beatManager.rewindBeats();
                this.resume();
            } else {
                this.skip();
            }
        }
    }

    /**
     * Updates the selected index of the ImList
     */
    private void updateSelectedValue() {
        NewJukeboxGUI.setSelectedSong(this.getCurrentSong());
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
     * Returns song length in seconds
     * @return song lenght in seconds as long value
     */
    public long getSongLength() {
        if (this.isPlaying()) {
            this.songLength = TimeUnit.MICROSECONDS.toSeconds(wavClip.getMicrosecondLength());
        }
        return this.songLength;
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
        return NewJukeboxGUI.titleList[index];
    }

    /**
     * Gets the path of the Song at the specific index
     * @param index index in songJList
     * @return Path
     */
    public Path getPath(int index) {
        return NewJukeboxGUI.songPaths[index];
    }
}
