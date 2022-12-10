package com.github.qpcrummer.directories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import static com.github.qpcrummer.directories.Directories.music;

public class Playlist {

    public final String name;
    public final Path path;
    public final ArrayList<Song> songs = new ArrayList<>();

    public Playlist(String name, Path path) {
        this.name = name;
        this.path = path;
        try {
            createSongArrayList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method fills the ArrayList that contains all songs in a directory
     */
    private void createSongArrayList() throws IOException {
        Path path = playlist2Path(this.name);
        try (Stream<Path> walk = Files.walk(path)) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                File song = file.toFile();
                songs.add(new Song(song.getName(), Path.of(song.getPath())));
            });
        }
    }

    /**
     * This method converts a playlist name to its path
     * @param playlist The name of the playlist selected
     * @return The path of the playlist
     */
    private Path playlist2Path(String playlist) {
        return Paths.get(music + "\\" + playlist);
    }
}
