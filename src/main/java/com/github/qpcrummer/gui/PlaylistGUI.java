package com.github.qpcrummer.gui;

import com.github.qpcrummer.directories.Directories;
import com.github.qpcrummer.directories.Playlist;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistGUI extends JFrame {
    private final List<Playlist> selectedPlaylists = new ArrayList<>();
    public PlaylistGUI() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Christmas Jukebox");
        final JPanel panel = new JPanel();
        this.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        final JLabel instructions = new JLabel("Select The Playlists You Want");
        panel.add(instructions);

        final JButton select = new JButton("Finish Selection");
        select.addActionListener(e -> {
            if (!this.selectedPlaylists.isEmpty()) {
                new JukeBoxGUI(Directories.combinePlaylists(this.selectedPlaylists));
                this.setVisible(false);
                this.dispose();
            }
        });
        panel.add(select);

        final JPanel selection = new JPanel();
        selection.setLayout(new BoxLayout(selection, BoxLayout.Y_AXIS));
        panel.add(selection);

        final List<Playlist> playlists = Directories.listPlaylists();
        for (Playlist playlist : playlists) {
            final JCheckBox checkBox = new JCheckBox(playlist.name);
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    this.selectedPlaylists.add(playlist);
                } else  {
                    this.selectedPlaylists.remove(playlist);
                }
            });
            selection.add(checkBox);
        }

        final JScrollPane scrollPane = new JScrollPane(selection);
        scrollPane.setPreferredSize(new Dimension(200,400));
        panel.add(scrollPane);

        this.pack();
        this.setVisible(true);
    }
}
