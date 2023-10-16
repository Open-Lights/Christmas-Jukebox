package com.github.qpcrummer.gui;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.directories.Directories;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PlaylistGUI extends JFrame {
    private final List<Path> selectedPlaylists = new ArrayList<>();
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
                new JukeBoxGUI(this.combinePlayLists(this.selectedPlaylists));
                this.setVisible(false);
                this.dispose();
            }
        });
        panel.add(select);

        final JPanel selection = new JPanel();
        selection.setLayout(new BoxLayout(selection, BoxLayout.Y_AXIS));
        panel.add(selection);

        final List<Path> playlists = this.listPlaylists();
        for (Path playlist : playlists) {
            final JCheckBox checkBox = new JCheckBox(Directories.getFileNameWithoutExtension(playlist));
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

    private Path[] createSongArrayList(final Path path) {

        List<Path> paths = new ArrayList<>();

        try (final Stream<Path> walk = Files.walk(path)) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                Directories.createBeatDirectory(file);
                paths.add(file);
            });
        } catch (IOException e) {
            Main.logger.warning("File path not accessible!");
        }
        return paths.toArray(new Path[0]);
    }

    private Path[] combinePlayLists(final List<Path> paths) {
        List<Path> combined = new ArrayList<>();
        for (Path path : paths) {
            combined.addAll(Arrays.asList(this.createSongArrayList(path)));
        }
        return combined.toArray(new Path[0]);
    }

    /**
     * This method lists all playlists that will be loaded
     */
    private List<Path> listPlaylists() {
        final List<Path> playlists = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(Directories.music, Files::isDirectory)) {
            StreamSupport.stream(stream.spliterator(), false).forEach(playlists::add);
        } catch (IOException e) {
            Main.logger.warning("Playlist path not accessible!");
        }
        return playlists;
    }
}
