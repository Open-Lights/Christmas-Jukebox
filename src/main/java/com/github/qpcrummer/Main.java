package com.github.qpcrummer;

import com.github.qpcrummer.audio_computation.VisualizerInit;
import com.github.qpcrummer.directories.Song;
import com.github.qpcrummer.music.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.event.ListSelectionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.qpcrummer.directories.Directories.createDirectories;
import static com.github.qpcrummer.directories.Directories.string2Song;
import static com.github.qpcrummer.gui.MainGUI.*;
import static com.github.qpcrummer.music.AudioPlayer.initMusic;
import static com.github.qpcrummer.music.AudioPlayer.skip;

public class Main {

    public static int song_playing;
    public static int song_list_size;

    public static Path current_song;

    public static final ScheduledExecutorService thread = Executors.newScheduledThreadPool(1);

    public static boolean thread_halt;
    public static ListSelectionListener listener;
    public static ArrayList<Song> current_songs = new ArrayList<>();

    public static void main(final String[] args) {
        createDirectories();
        initGUI();
        VisualizerInit.init();
    }

    /**
     * Initializes methods needed to start playing music
     * @param current_song The path of the song currently being played
     */
    public static void musicSetup(final Path current_song) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioPlayer.filePath = String.valueOf(current_song);
        nowplaying.setText("Now Playing " + current_songs.get(song_playing).formatted_name);
        initMusic();

        // Song list action listeners
        listener = e -> {
            try {
                song_playing = string2Song(visible_song_list.getSelectedValue());
                skip();
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        };

        visible_song_list.removeListSelectionListener(listener);
        
        visible_song_list.setSelectedValue(current_songs.get(song_playing).title, true);

        visible_song_list.addListSelectionListener(listener);

        // Prepare for next song
        if (song_playing == song_list_size - 1) {
            song_playing = 0;
        } else {
            song_playing++;
        }
    }
}