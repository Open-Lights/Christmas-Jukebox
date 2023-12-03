package com.github.qpcrummer.gui;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.directories.Directories;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NewPlaylistGUI {
    public static boolean shouldRender = true;
    public static final List<Path> playlists = listPlaylists();
    public static final boolean[] selectedPlaylists = new boolean[playlists.size()];

    public static void render() {
        if (!shouldRender) {
            return;
        }

        ImGui.begin("Select Playlists", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar);

        ImGui.setWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.setWindowPos(0F, 0F);

        GuiUtils.setFont(1.3F);
        ImGui.text("Playlists");

        for (int i = 0; i < playlists.size(); i++) {
            if (ImGui.checkbox(Directories.getFileNameWithoutExtension(playlists.get(i)), selectedPlaylists[i])) {
                selectedPlaylists[i] = !selectedPlaylists[i];
            }
        }

        GuiUtils.clearFontSize();

        ImGui.separator();

        if (ImGui.button("Confirm")) {
            if (isOneSelected()) {
                NewJukeboxGUI.initialize(combinePlayLists());
                shouldRender = false;
                NewJukeboxGUI.shouldRender = true;
            }
        }

        ImGui.end();
    }

    private static Path[] createSongArrayList(final Path path) {

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

    public static Path[] combinePlayLists() {
        final List<Path> combined = new ArrayList<>();
        for (Path path : getSelectedPlaylists()) {
            combined.addAll(Arrays.asList(createSongArrayList(path)));
        }
        return combined.toArray(new Path[0]);
    }

    private static List<Path> getSelectedPlaylists() {
        List<Path> path = new ArrayList<>();
        for (int i = 0; i < selectedPlaylists.length; i++) {
            if (selectedPlaylists[i]) {
                path.add(playlists.get(i));
            }
        }
        return path;
    }

    /**
     * This method lists all playlists that will be loaded
     */
    private static List<Path> listPlaylists() {
        final List<Path> playlists = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(Directories.music, Files::isDirectory)) {
            StreamSupport.stream(stream.spliterator(), false).forEach(playlists::add);
        } catch (IOException e) {
            Main.logger.warning("Playlist path not accessible!");
        }
        return playlists;
    }

    private static boolean isOneSelected() {
        for (boolean bool : selectedPlaylists) {
            if (bool) {
                return true;
            }
        }
        return false;
    }
}
