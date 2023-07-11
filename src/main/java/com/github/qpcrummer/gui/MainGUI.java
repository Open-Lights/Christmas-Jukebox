package com.github.qpcrummer.gui;

import com.github.qpcrummer.directories.Playlist;
import com.github.qpcrummer.lights.LightsDebug;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

import static com.github.qpcrummer.directories.Directories.*;
import static com.github.qpcrummer.Main.*;

public class MainGUI {
    private final JFrame mainframe = new JFrame("Christmas Celebrator");
    private final JFrame playlistframe = new JFrame("Christmas Celebrator");
    private final JPanel mainplaylistpanel = new JPanel();
    private final JPanel mainpanel = new JPanel();
    private final JPanel control = new JPanel();
    private final JPanel top = new JPanel();
    private final JPanel multitop = new JPanel();
    private final JPanel multibottom = new JPanel();
    private final JButton play = new JButton("Play");
    private final JButton skip = new JButton("Skip");
    private final JButton rewind = new JButton("Rewind");
    private final JCheckBox loop = new JCheckBox("Loop");
    private final JButton mute = new JButton("Mute");
    private final JButton shuffle = new JButton("Shuffle");
    private final JButton back = new JButton("Back");
    public JButton[] playlist_buttons;
    private final JLabel nowplaying = new JLabel();
    private final JLabel playlist_label = new JLabel("Select a Playlist");
    private final JSlider volume_slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
    private final DefaultListModel<String> song_list = new DefaultListModel<>();
    private final JList<String> visible_song_list = new JList<>(song_list);
    private final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
    private final JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
    private final JScrollPane music_scroll = new JScrollPane();
    private final JProgressBar music_bar = new JProgressBar();
    private final Dimension dimension = new Dimension(mainframe.getWidth(), 100);

    /**
     * The main bulk of the GUI code
     */
    public void initGUI() {
        //Main GUI
        mainframe.add(mainpanel);
        mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
        mainpanel.add(playlist_label);
        mainpanel.add(separator1);

        playlist_buttons = new JButton[playlists.size()+1];


        for (int i = 0; i < playlists.size()+1; i++) {
            if (i == playlists.size()) {
                playlist_buttons[i] = new JButton("All Songs");
            } else {
                playlist_buttons[i] = new JButton(playlists.get(i).name);
            }
            final int finalI = i;

            mainpanel.add(playlist_buttons[i]);
            playlist_buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            playlist_buttons[i].setMinimumSize(dimension);

            playlist_buttons[i].addActionListener(e -> {

                if (!Objects.equals(playlist_buttons[finalI].getText(), "All Songs")) {
                    Playlist selected_playlist = playlists.get(finalI);
                    if (current_songs != selected_playlist.songs) {
                        current_songs = selected_playlist.songs;
                        song_list_size = current_songs.size();
                    }
                } else {
                    current_songs = combinePlaylists(playlists);
                    song_list_size = current_songs.size();
                }
                song_playing = 0;


                mainframe.setVisible(false);
                try {
                    reloadPlaylistGUI();
                    musicSetup(current_song = current_songs.get(song_playing).path);
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        //Lights Configuration
        mainpanel.add(lights_debug);
        lights_debug.setAlignmentX(Component.CENTER_ALIGNMENT);
        lights_debug.setMinimumSize(dimension);
        lights_debug.addActionListener(e -> LightsDebug.initLightGUI());

        //Configuration
        playlist_label.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator1.setMaximumSize(new Dimension(mainframe.getWidth(), 50));



        //Playlist GUI
        playlistframe.add(mainplaylistpanel);
        mainplaylistpanel.setLayout(new BorderLayout());
        mainplaylistpanel.add(top, BorderLayout.PAGE_START);
        mainplaylistpanel.add(multibottom, BorderLayout.PAGE_END);
        mainplaylistpanel.add(music_scroll, BorderLayout.CENTER);

        top.add(multitop);
        top.add(separator);

        multibottom.setLayout(new BoxLayout(multibottom, BoxLayout.Y_AXIS));
        multibottom.add(music_bar);
        multibottom.add(control);

        multitop.add(nowplaying);
        multitop.add(back);

        control.add(loop);
        control.add(shuffle);
        control.add(rewind);
        control.add(play);
        control.add(skip);
        control.add(volume_slider);

        //Configuration
        nowplaying.setFont(new Font("Serif", Font.BOLD, 20));

        volume_slider.setPaintTicks(true);
        volume_slider.setPaintLabels(true);
        volume_slider.setMajorTickSpacing(20);

        music_scroll.setViewportView(visible_song_list);
        music_scroll.getVerticalScrollBar().setPreferredSize(new Dimension(30, Integer.MAX_VALUE));

        visible_song_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visible_song_list.setLayoutOrientation(JList.VERTICAL);
        visible_song_list.setFixedCellHeight(40);

        music_bar.setValue(0);
        music_bar.setStringPainted(true);
        music_bar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.black; }
        });
        music_bar.setForeground(new Color(0,100,0));

        //Action Listeners

        mainframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                super.windowClosing(event);
                try {
                    if (clip != null) {
                        stop();
                    }
                    thread.shutdownNow();
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        play.addActionListener( e -> {
            if (Objects.equals(status, "play")) {
                pause();
            } else {
                try {
                    resumeAudio();
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        rewind.addActionListener( e -> {
            clip.setFramePosition(0);
            if (Objects.equals(status, "paused")) {
                play.setText("Playing");
                play();
            }
        });

        mute.addActionListener( e -> {
            if (volume.getValue() < 0) {
                volume.setValue(0);
                mute.setText("Mute");
            } else {
                volume.setValue(-80);
                mute.setText("Muted");
            }
        });

        volume_slider.addChangeListener( e -> calcVolume());

        volume_slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent event) {
                final JSlider sourceSlider=(JSlider)event.getSource();
                final BasicSliderUI sliderUI = (BasicSliderUI)sourceSlider.getUI();
                final int value = sliderUI.valueForXPosition( event.getX() );
                volume_slider.setValue(value);
                calcVolume();
            }
        });

        skip.addActionListener( e -> {
            try {
                skip();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        });

        loop.addActionListener( e -> looping = loop.isSelected());

        shuffle.addActionListener( e -> {
            try {
                thread_halt = true;
                stop();
                final var list = current_songs;
                Collections.shuffle(list);
                current_songs = list;
                musicSetup(current_song = current_songs.get(song_playing).path);
                play();
                thread_halt = false;
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
        });

        back.addActionListener( e -> {
            try {
                stop();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                throw new RuntimeException(ex);
            }
            mainframe.setVisible(true);
            playlistframe.setVisible(false);
            music = Paths.get("celebrator/music");
            visible_song_list.removeListSelectionListener(listener);
            song_list.clear();
        });

        // Additional Configuration
        mainframe.setMinimumSize(new Dimension(600, 400));
        mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainframe.pack();
        mainframe.setVisible(true);

        playlistframe.setMinimumSize(new Dimension(600, 400));
        playlistframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        playlistframe.pack();
        playlistframe.setVisible(false);
    }

    /**
     * Resets the Song List
     */
    private void reloadPlaylistGUI() {
        for (int i = 0; i < song_list_size; i++) {
            final String name = current_songs.get(i).title;
            song_list.addElement(name);
        }

        // Additional Configuration
        playlistframe.setVisible(true);
    }

    /**
     * Calculates the volume for the slider
     */
    private void calcVolume() {
        double slider_value = volume_slider.getValue();
        double new_volume;
        if (slider_value == 0) {
            new_volume = -80;
        } else {
            new_volume = 30 * Math.log10(slider_value) - 60;
        }
        volume.setValue((float) new_volume);
    }
}
