package com.github.qpcrummer;

import com.github.qpcrummer.Music.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.event.ListSelectionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.github.qpcrummer.Directories.Directories.*;
import static com.github.qpcrummer.GUI.MainGUI.*;
import static com.github.qpcrummer.Music.AudioPlayer.initMusic;
import static com.github.qpcrummer.Music.AudioPlayer.skip;

public class Main {

    public static int song_playing = 0;
    public static int song_list_size;

    public static Path current_song;

    public static final ScheduledExecutorService thread = Executors.newScheduledThreadPool(1);

    public static boolean thread_halt;
    public static ListSelectionListener l;
    public static HashMap<String, File> stored_data = new HashMap<>();
    public static HashMap<String, String> stored_authors = new HashMap<>();

    public static void main(String[] args) throws IOException {
        createDirectories();
        populateHashMap();
        initGUI();
    }

    public static void musicSetup(Path current_song) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioPlayer.filePath = String.valueOf(current_song);
        nowplaying.setText("Now Playing " + songAsFile(current_song).getName().replace(".wav", "").replace("_", " "));
        initMusic();

        // Song list action listeners
        l = e -> {
            try {
                song_playing = string2Song(visible_song_list.getSelectedValue().split(" by ")[0]);
                skip();
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        };

        visible_song_list.removeListSelectionListener(l);
        
        visible_song_list.setSelectedValue(int2Name(song_playing) + " by " + stored_authors.get(path2Name(current_song_list.get(song_playing))), true);

        visible_song_list.addListSelectionListener(l);

        // Prepare for next song
        if (song_playing == song_list_size - 1) {
            song_playing = 0;
        } else {
            song_playing++;
        }
    }

    public static File songAsFile(Path path) {
        return new File(String.valueOf(path));
    }

    public static int string2Song(String name) throws IOException {
        return current_song_list.indexOf(stored_data.get(name).toPath());
    }

    public static String int2Name(int number) {
        return path2Name(current_song_list.get(number));
    }

    public static String path2Name(Path path) {
        return songAsFile(path).getName().replace(".wav", "").replace("_", " ");
    }
}