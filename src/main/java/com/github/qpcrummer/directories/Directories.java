package com.github.qpcrummer.directories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class Directories {

    public static Path music = Paths.get("celebrator/music");
    public static Path main = Paths.get("celebrator");
    public static Path beats = Paths.get("celebrator/beats");

    /**
     * Creates all the directories needed for this application
     */
    public static void createDirectories() {
        try {

            if (Files.notExists(main)) {
                Files.createDirectory(main);
            }
            if (Files.notExists(music)) {
                Files.createDirectory(music);
            }
            if (Files.notExists(Directories.beats)) {
                Files.createDirectory(Directories.beats);
            }
        } catch(IOException ignored) {
        }
    }

    /**
     * This method lists all playlists that will be loaded
     */
    public static List<Playlist> listPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(music, Files::isDirectory)) {
            StreamSupport.stream(stream.spliterator(), false).forEach(file -> {
                File playlist = file.toFile();
                playlists.add(new Playlist(playlist.getName(), Path.of(playlist.getPath())));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playlists;
    }

    /**
     * This method combines two or more playlists
     * @param playlists Playlist object
     */
    public static List<Song> combinePlaylists(List<Playlist> playlists) {
        List<Song> combined_songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            combined_songs.addAll(playlist.getSongs());
        }
        return combined_songs;
    }
}
