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
    private transient final WAVPlayer wavPlayer;
    private final String pauseString = "Pause";
    private final String playString = "Play";
    public JukeBoxGUI(final List<Song> songs) {
        this.songs = songs;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                super.windowClosing(e);
                wavPlayer.shutDown();
                new PlaylistGUI();
                dispose();
            }
        });


        // Main Panel
        final JPanel panel = new JPanel();
        this.add(panel);
        panel.setLayout(new BorderLayout());

        // Song List
        final DefaultListModel<Song> songList = new DefaultListModel<>();
        this.updateJList(songList);
        final JList<Song> visibleSongList = new JList<>(songList);
        visibleSongList.setCellRenderer(new SongListCellRenderer());

        // Top Panel
        final JPanel top = new JPanel();
        final JPanel multitop = new JPanel();
        final JSeparator separator = new JSeparator();
        top.add(multitop);
        top.add(separator);
        panel.add(top, BorderLayout.PAGE_START);

        // Bottom Panel
        final JPanel multibottom = new JPanel();
        final JProgressBar musicBar = new JProgressBar();
        final JPanel control = new JPanel();
        multibottom.setLayout(new BoxLayout(multibottom, BoxLayout.Y_AXIS));
        multibottom.add(musicBar);
        multibottom.add(control);
        panel.add(multibottom, BorderLayout.PAGE_END);

        // More Top
        final JLabel nowplaying = new JLabel();
        final JButton back = new JButton("Back");
        multitop.add(nowplaying);
        multitop.add(back);

        // Selection Bar Bottom
        final JCheckBox loop = new JCheckBox("Loop");
        final JButton shuffle = new JButton("Shuffle");
        final JButton rewind = new JButton("Rewind");
        final JButton play = new JButton(this.playString);
        final JButton skip = new JButton("Skip");
        final JSlider volumeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
        control.add(loop);
        control.add(shuffle);
        control.add(rewind);
        control.add(play);
        control.add(skip);
        control.add(volumeSlider);

        // Scroll Pane
        final JScrollPane musicScroll = new JScrollPane(visibleSongList);
        panel.add(musicScroll, BorderLayout.CENTER);
        musicScroll.getVerticalScrollBar().setPreferredSize(new Dimension(30, Integer.MAX_VALUE));

        // WAV Player
        final AtomicReference<WAVPlayer> wavReference = new AtomicReference<>();
        final ListSelectionListener listSelectionListener = e -> {
            wavReference.get().songOverride(visibleSongList.getSelectedValue());
            play.setText(this.pauseString);
        };
        this.wavPlayer = new WAVPlayer(musicBar, this.songs, visibleSongList, listSelectionListener, this);
        wavReference.set(this.wavPlayer);

        // Configuration
        nowplaying.setFont(new Font("Serif", Font.BOLD, 20));

        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setMajorTickSpacing(20);

        visibleSongList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visibleSongList.setLayoutOrientation(JList.VERTICAL);
        visibleSongList.setFixedCellHeight(40);

        musicBar.setValue(0);
        musicBar.setStringPainted(true);
        musicBar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionBackground() { return Color.black; }
        });
        musicBar.setForeground(new Color(0,100,0));

        // ActionListeners

        play.addActionListener(e -> {
            if (this.wavPlayer.isPlaying()) {
                this.wavPlayer.pause();
                play.setText(this.playString);
            } else {
                this.wavPlayer.resume();
                play.setText(this.pauseString);
            }
        });

        rewind.addActionListener(e -> {
            this.wavPlayer.rewind();
            play.setText(this.pauseString);
        });

        volumeSlider.addChangeListener(e -> this.wavPlayer.calcVolume(volumeSlider.getValue()));

        volumeSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent event) {
                final JSlider sourceSlider=(JSlider)event.getSource();
                final BasicSliderUI sliderUI = (BasicSliderUI)sourceSlider.getUI();
                final int value = sliderUI.valueForXPosition( event.getX() );
                volumeSlider.setValue(value);
                wavPlayer.calcVolume(value);
            }
        });

        skip.addActionListener(e -> {
            this.wavPlayer.skip();
            play.setText(this.pauseString);
        });

        loop.addActionListener(e -> this.wavPlayer.setLooping(loop.isSelected()));

        shuffle.addActionListener(e -> {
            this.wavPlayer.shuffle();
            play.setText(this.pauseString);
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
    private void updateJList(final DefaultListModel<Song> songJList) {
        songJList.clear();
        for (Song song : this.songs) {
            songJList.addElement(song);
        }
    }
}
