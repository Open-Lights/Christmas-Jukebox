package com.github.qpcrummer.gui;

import com.github.qpcrummer.directories.Directories;
import com.github.qpcrummer.music.MusicUtils;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class NewPlaylistGUI {
    public static boolean shouldRender = true;
    public static void render() {
        if (!shouldRender) {
            return;
        }

        ImGui.begin("Select Playlists", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar);

        ImGui.setWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.setWindowPos(0F, 0F);

        GuiUtils.setFont(1.3F);
        ImGui.text("Playlists");

        for (int i = 0; i < MusicUtils.playlists.size(); i++) {
            if (ImGui.checkbox(Directories.getFileNameWithoutExtension(MusicUtils.playlists.get(i)), MusicUtils.selectedPlaylists[i])) {
                MusicUtils.selectedPlaylists[i] = !MusicUtils.selectedPlaylists[i];
            }
        }

        GuiUtils.clearFontSize();

        ImGui.separator();

        if (ImGui.button("Confirm")) {
            if (isOneSelected()) {
                MusicUtils.initializeJukebox(MusicUtils.combinePlayLists());
                shouldRender = false;
                NewJukeboxGUI.shouldRender = true;
            }
        }

        ImGui.end();
    }

    private static boolean isOneSelected() {
        for (boolean bool : MusicUtils.selectedPlaylists) {
            if (bool) {
                return true;
            }
        }
        return false;
    }
}
