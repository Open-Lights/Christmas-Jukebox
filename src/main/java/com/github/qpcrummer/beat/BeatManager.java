package com.github.qpcrummer.beat;

import com.github.qpcrummer.directories.Song;
import com.github.qpcrummer.music.WAVPlayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

public class BeatManager {
    private final WAVPlayer player;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();;
    private final List<ChannelIdentifier> channels = new ArrayList<>();
    private Song lastSong;
    public BeatManager(WAVPlayer player) {
        this.player = player;
    }

    /**
     * Finds and Reads all beat txt files
     * @param song current Song playing
     */
    public void readBeats(Song song) {
        if (song != lastSong) {
            channels.clear();

            try {
                Path beatDirectory = song.beatPath;
                if (Files.isDirectory(beatDirectory)) {
                    try (Stream<Path> filesStream = Files.list(beatDirectory)) {
                        filesStream
                                .filter(Files::isRegularFile)
                                .forEach(this::readBeatsFromFile);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            lastSong = song;
        }
    }

    private void readBeatsFromFile(Path filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            final List<Long> beats = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                beats.add(Long.parseLong(line, 16));
            }
            channels.add(new ChannelIdentifier(filePath.toFile().getName(), beats, this.executorService, this.player));
        } catch (Exception e) {
            e.printStackTrace();
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
     * Completely stops the Thread and sends it off to the GC
     */
    public void stopThread() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        executorService = null;
    }

    /**
     * Resets the indexes of all channels
     */
    public void resetBeats() {
        if (!this.channels.isEmpty()) {
            for (ChannelIdentifier channel : channels) {
                channel.reset();
            }
        }
    }

    /**
     * Gets the Beat ScheduledExecutorService so that other threads can schedule tasks
     * @return ScheduledExecutorService
     */
    public ScheduledExecutorService getBeatExecutor() {
        return this.executorService;
    }
}
