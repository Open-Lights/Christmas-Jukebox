package com.github.qpcrummer.GUI;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
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

import static com.github.qpcrummer.Directories.Directories.*;
import static com.github.qpcrummer.Main.*;
import static com.github.qpcrummer.Music.AudioPlayer.*;

public class MainGUI {
    public static JFrame mainframe = new JFrame("Christmas Celebrator");
    public static JFrame playlistframe = new JFrame("Christmas Celebrator");
    public static JPanel mainplaylistpanel = new JPanel();
    public static JPanel mainpanel = new JPanel();
    public static JPanel control = new JPanel();
    public static JPanel top = new JPanel();
    public static JPanel multitop = new JPanel();
    public static JButton play = new JButton("Play");
    public static JButton skip = new JButton("Skip");
    public static JButton rewind = new JButton("Rewind");
    public static JCheckBox loop = new JCheckBox("Loop");
    public static JButton mute = new JButton("Mute");
    public static JButton shuffle = new JButton("Shuffle");
    public static JButton back = new JButton("Back");
    public static JButton[] playlist_buttons;
    public static JLabel nowplaying = new JLabel();
    public static JLabel playlist_label = new JLabel("Select a Playlist");
    public static JSlider volume_slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
    public static DefaultListModel<String> song_list = new DefaultListModel<>();
    public static JList<String> visible_song_list = new JList<>(song_list);
    public static JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
    public static JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
    public static JScrollPane music_scroll = new JScrollPane();
    public static boolean looping;

    public static void initGUI() throws IOException {
        //Main GUI
        mainframe.add(mainpanel);
        mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
        mainpanel.add(playlist_label);
        mainpanel.add(separator1);

        playlist_buttons = new JButton[listPlaylists().size()+1];

        for (int i = 0; i < listPlaylists().size()+1; i++) {
            if (i == listPlaylists().size()) {
                playlist_buttons[i] = new JButton("All Songs");
            } else {
                playlist_buttons[i] = new JButton(listPlaylists().get(i));
            }
            int finalI = i;

            mainpanel.add(playlist_buttons[i]);
            playlist_buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            playlist_buttons[i].setMinimumSize(new Dimension(mainframe.getWidth(), 100));

            playlist_buttons[i].addActionListener(e -> {
                if (!Objects.equals(playlist_buttons[finalI].getText(), "All Songs")) {
                    music = playlist2Path(playlist_buttons[finalI].getText());
                }

                try {
                    song_playing = 0;
                    current_song_list = listSongs(music);
                    song_list_size = current_song_list.size();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                mainframe.setVisible(false);
                try {
                    reloadPlaylistGUI();
                    musicSetup(current_song = current_song_list.get(song_playing));
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        //Configuration
        playlist_label.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator1.setMaximumSize(new Dimension(mainframe.getWidth(), 50));



        //Playlist GUI
        playlistframe.add(mainplaylistpanel);
        mainplaylistpanel.setLayout(new BorderLayout());
        mainplaylistpanel.add(top, BorderLayout.PAGE_START);
        mainplaylistpanel.add(control, BorderLayout.PAGE_END);
        mainplaylistpanel.add(music_scroll, BorderLayout.CENTER);

        top.add(multitop);
        top.add(separator);

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

        visible_song_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visible_song_list.setLayoutOrientation(JList.VERTICAL);
        visible_song_list.setFixedCellHeight(40);

        //Action Listeners

        mainframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
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
            public void mousePressed(MouseEvent e) {
                JSlider sourceSlider=(JSlider)e.getSource();
                BasicSliderUI ui = (BasicSliderUI)sourceSlider.getUI();
                int value = ui.valueForXPosition( e.getX() );
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
                var list = listSongs(music);
                Collections.shuffle(list);
                current_song_list = list;
                musicSetup(current_song = current_song_list.get(song_playing));
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
            visible_song_list.removeListSelectionListener(l);
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

    public static void reloadPlaylistGUI() {
        for (int i = 0; i < song_list_size; i++) {
            String name = songAsFile(current_song_list.get(i)).getName().replace(".wav", "").replace("_", " ") + " by " + stored_authors.get(path2Name(current_song_list.get(i)));
            song_list.addElement(name);
        }

        // Additional Configuration
        playlistframe.setVisible(true);
    }

    public static void calcVolume() {
        double percentage = volume_slider.getValue() * 0.01;
        double new_volume = 80 * percentage - 80;
        volume.setValue((float) new_volume);
    }
}
