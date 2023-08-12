package com.github.qpcrummer.beat;

import com.github.qpcrummer.directories.Song;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BeatTranslator {
    private final List<ChannelIdentifier> channels = new ArrayList<>();
    private Song lastSong;

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
            channels.add(new ChannelIdentifier(filePath.toFile().getName(), beats));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ChannelIdentifier> getChannels() {
        return this.channels;
    }
}
