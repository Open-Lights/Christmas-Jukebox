package com.github.qpcrummer.gui;

import com.github.qpcrummer.directories.Song;
import com.github.qpcrummer.music.WAVPlayer;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class JukeBoxGUI extends JFrame {
    private final List<Song> songs;
    private final WAVPlayer wavPlayer;
    public JukeBoxGUI(List<Song> songs) {
        this.songs = songs;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                wavPlayer.shutDown();
                new PlaylistGUI();
                dispose();
            }
        });


        // Main Panel
        JPanel panel = new JPanel();
        this.add(panel);
        panel.setLayout(new BorderLayout());

        // Song List
        DefaultListModel<Song> song_list = new DefaultListModel<>();
        this.updateJList(song_list);
        JList<Song> visibleSongList = new JList<>(song_list);
        visibleSongList.setCellRenderer(new SongListCellRenderer());

        // Top Panel
        JPanel top = new JPanel();
        JPanel multitop = new JPanel();
        JSeparator separator = new JSeparator();
        top.add(multitop);
        top.add(separator);
        panel.add(top, BorderLayout.PAGE_START);

        // Bottom Panel
        JPanel multibottom = new JPanel();
        JProgressBar music_bar = new JProgressBar();
        JPanel control = new JPanel();
        multibottom.setLayout(new BoxLayout(multibottom, BoxLayout.Y_AXIS));
        multibottom.add(music_bar);
        multibottom.add(control);
        panel.add(multibottom, BorderLayout.PAGE_END);

        // More Top
        JLabel nowplaying = new JLabel();
        JButton back = new JButton("Back");
        multitop.add(nowplaying);
        multitop.add(back);

        // Selection Bar Bottom
        JCheckBox loop = new JCheckBox("Loop");
        JButton shuffle = new JButton("Shuffle");
        JButton rewind = new JButton("Rewind");
        JButton play = new JButton("Play");
        JButton skip = new JButton("Skip");
        JSlider volume_slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
        control.add(loop);
        control.add(shuffle);
        control.add(rewind);
        control.add(play);
        control.add(skip);
        control.add(volume_slider);

        // Scroll Pane
        JScrollPane music_scroll = new JScrollPane(visibleSongList);
        panel.add(music_scroll, BorderLayout.CENTER);
        music_scroll.getVerticalScrollBar().setPreferredSize(new Dimension(30, Integer.MAX_VALUE));

        // WAV Player
        AtomicReference<WAVPlayer> wavReference = new AtomicReference<>();
        ListSelectionListener listSelectionListener = e -> {
            wavReference.get().songOverride(visibleSongList.getSelectedValue());
            play.setText("Pause");
        };
        this.wavPlayer = new WAVPlayer(music_bar, this.songs, visibleSongList, listSelectionListener, this);
        wavReference.set(this.wavPlayer);

        // Configuration
        nowplaying.setFont(new Font("Serif", Font.BOLD, 20));

        volume_slider.setPaintTicks(true);
        volume_slider.setPaintLabels(true);
        volume_slider.setMajorTickSpacing(20);

        visibleSongList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visibleSongList.setLayoutOrientation(JList.VERTICAL);
        visibleSongList.setFixedCellHeight(40);

        music_bar.setValue(0);
        music_bar.setStringPainted(true);
        music_bar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.black; }
        });
        music_bar.setForeground(new Color(0,100,0));

        // ActionListeners

        play.addActionListener(e -> {
            if (this.wavPlayer.isPlaying()) {
                this.wavPlayer.pause();
                play.setText("Play");
            } else {
                this.wavPlayer.resume();
                play.setText("Pause");
            }
        });

        rewind.addActionListener(e -> {
            this.wavPlayer.rewind();
            play.setText("Pause");
        });

        volume_slider.addChangeListener(e -> this.wavPlayer.calcVolume(volume_slider.getValue()));

        volume_slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent event) {
                final JSlider sourceSlider=(JSlider)event.getSource();
                final BasicSliderUI sliderUI = (BasicSliderUI)sourceSlider.getUI();
                final int value = sliderUI.valueForXPosition( event.getX() );
                volume_slider.setValue(value);
                wavPlayer.calcVolume(value);
            }
        });

        skip.addActionListener(e -> {
            this.wavPlayer.skip();
            play.setText("Pause");
        });

        loop.addActionListener(e -> this.wavPlayer.setLooping(loop.isSelected()));

        shuffle.addActionListener(e -> {
            this.wavPlayer.shuffle();
            play.setText("Pause");
        });

        back.addActionListener(e -> {
            this.wavPlayer.shutDown();
            new PlaylistGUI();
            this.dispose();
        });

        visibleSongList.addListSelectionListener(listSelectionListener);

        // Finalize
        this.setMinimumSize(new Dimension(600, 400));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    /**
     * Updates the JList
     * @param songJList DefaultListModel of JList
     */
    private void updateJList(DefaultListModel<Song> songJList) {
        songJList.clear();
        for (Song song : this.songs) {
            songJList.addElement(song);
        }
    }
}
