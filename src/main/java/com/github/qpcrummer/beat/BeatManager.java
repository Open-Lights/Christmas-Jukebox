package com.github.qpcrummer.beat;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.directories.Directories;
import com.github.qpcrummer.music.WAVPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

public class BeatManager {
    private ScheduledExecutorService executorService;
    private final List<ChannelIdentifier> channels = new ArrayList<>();
    private int lastSong;
    public BeatManager() {
    }

    /**
     * Sets up the Threads for the BeatManager
     */
    public void initialize() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Finds and Reads all beat txt files
     * @param song current Song playing
     */
    public void readBeats(final Path song, final int index) {
        if (index != this.lastSong) {
            channels.clear();

            try {
                final Path beatDirectory = Directories.getBeatPath(song);
                if (Files.isDirectory(beatDirectory)) {
                    try (final Stream<Path> filesStream = Files.list(beatDirectory)) {
                        filesStream
                                .filter(Files::isRegularFile)
                                .forEach(this::readBeatsFromFile);
                    }
                }
            } catch (IOException e) {
                Main.logger.warning("Failed to read beats for Song: " + song);
            }

            lastSong = index;
        }
    }

    private void readBeatsFromFile(final Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            final List<Object> beats = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.contains("[")) {
                    final String[] elements = line.replaceAll("[\\[\\]]", "").split(",\\s*");

                    final long[] longArray = new long[elements.length];
                    for (int i = 0; i < elements.length; i++) {
                        longArray[i] = Long.parseLong(elements[i], 16);
                    }
                    beats.add(longArray);
                } else {
                    beats.add(Long.parseLong(line, 16));
                }
            }
            channels.add(new ChannelIdentifier(filePath.toFile().getName(), beats, this.executorService));
        } catch (IOException e) {
            Main.logger.warning("Failed to read beats from File: " + filePath);
        }
    }

    /**
     * Starts testing for beats every ms
     */
    public void startBeatTracking() {
        if (!this.channels.isEmpty()) {
            for (ChannelIdentifier channel : channels) {
                channel.checkIfBeat();
            }
        }
    }

    /**
     * Called when the song is resumed
     */
    public void onResume() {
        for (ChannelIdentifier channel : this.channels) {
            channel.resume();
        }
    }


    /**
     * Called when the song is rewound
     */
    public void onRewind() {
        for (ChannelIdentifier channel : this.channels) {
            channel.reset();
        }
    }

    /**
     * Completely stops the Thread and sends it off to the GC
     */
    public void stopThread() {
        executorService.shutdownNow();
    }

    /**
     * Rewinds the indexes of all channels to the beginning
     */
    public void rewindBeats() {
        if (!this.channels.isEmpty()) {
            for (ChannelIdentifier channel : channels) {
                channel.reset();
            }
        }
    }

    /**
     * Cancels all current tasks.
     * This is usually called after the song that it was initially tracking was removed (skipped)
     */
    public void resetBeats() {
        this.channels.clear();
    }
}
