package com.github.qpcrummer.directories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static com.github.qpcrummer.Main.current_songs;

public class Directories {

    public static Path music = Paths.get("celebrator/music");
    public static Path main = Paths.get("celebrator");
    public static ArrayList<Playlist> playlists = new ArrayList<>();

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
            listPlaylists();
        } catch(IOException ignored) {
        }
    }

    /**
     * This method lists all playlists that will be loaded
     */
    public static void listPlaylists() throws IOException {
        try (var stream = Files.newDirectoryStream(music, Files::isDirectory)) {
            StreamSupport.stream(stream.spliterator(), false).forEach(file -> {
                File playlist = file.toFile();
                playlists.add(new Playlist(playlist.getName(), Path.of(playlist.getPath())));
            });
        }
    }

    //TODO Make playlists JCheckboxes rather than JButtons
    /**
     * This method combines two or more playlists
     * @param playlists Playlist object
     */
    public static ArrayList<Song> combinePlaylists(ArrayList<Playlist> playlists) {
        ArrayList<Song> combined_songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            combined_songs.addAll(playlist.songs);
        }
        return combined_songs;
    }

    /**
     * Converts a title to a Song Class's index
     * @param title The selected value's text
     * @return The index of the Song
     */
    public static int string2Song(final String title) throws IOException {
        for(int i = 0; i < current_songs.size(); i++) {
            if (Objects.equals(current_songs.get(i).title, title)) {
                return i;
            }
        }
        return 0;
    }
}
