package com.github.qpcrummer.directories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Playlist {

    public final String name;
    public final Path path;
    private final List<Song> songs = new ArrayList<>();

    public Playlist(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    /**
     * This method fills the ArrayList that contains all songs in a directory
     */
    private void createSongArrayList() {
        try (Stream<Path> walk = Files.walk(this.path)) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                File song = file.toFile();
                Song newSong = new Song(song.getName(), Path.of(song.getPath()));
                this.songs.add(newSong);
                createBeatsFolder(newSong);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Grab and cache Songs of the Playlist
     * @return Gets Songs
     */
    public List<Song> getSongs() {
        if (this.songs.isEmpty()) {
            createSongArrayList();
        }
        return this.songs;
    }

    /**
     * Creates and sets the Path for beat files
     * @param song Song class
     */
    private void createBeatsFolder(Song song) {
        Path songBeatPath = Path.of(Directories.beats + "/" + song.name.replace(".wav", ""));
        if (Files.notExists(songBeatPath)) {
            try {
                Files.createDirectory(songBeatPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        song.beatPath = songBeatPath;
    }
}
