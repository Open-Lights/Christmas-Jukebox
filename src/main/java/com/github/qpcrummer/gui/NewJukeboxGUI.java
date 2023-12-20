package com.github.qpcrummer.gui;

import com.github.qpcrummer.music.MusicUtils;
import com.github.qpcrummer.music.WAVPlayer;
import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import java.util.concurrent.TimeUnit;

public class NewJukeboxGUI {
    public static boolean shouldRender;
    public static String title = "Christmas Celebrator";
    private static int selectedListItem = -1;
    private static float volume = 100.0f;
    public static String[] titleList;
    private static final float width = ImGui.getIO().getDisplaySizeX() - 15;

    private static boolean looping;
    public static String cachedFormattedSongLength;

    public static void render() {
        if (!shouldRender) {
            return;
        }

        ImGui.begin(title, ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar);

        ImGui.setWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.setWindowPos(0F, 0F);

        ImGui.text(title);

        if (ImGui.button("Back", width, 20)) {
            MusicUtils.quit();
        }


        ImGui.separator();

        ImGui.text("Song Playlist");

        // List of Strings
        GuiUtils.setFont(1.3F);
        if (ImGui.beginListBox("##", width, ImGui.getIO().getDisplaySizeY() * 0.75F)) {
            for (int i = 0; i < titleList.length; i++) {
                boolean isSelected = i == selectedListItem;
                if (ImGui.selectable(titleList[i], isSelected)) {
                    selectedListItem = i;
                    WAVPlayer.songOverride(i);
                }
            }
            ImGui.endListBox();
        }
       GuiUtils.clearFontSize();

        ImGui.separator();

        // Progress Bar
        setProgressBar();

        // Buttons and Slider (in a horizontal layout)
        ImGui.beginGroup();

        if (ImGui.button("Shuffle")) {
            WAVPlayer.shuffle();
        }

        ImGui.sameLine();

        if (ImGui.button("Skip")) {
            WAVPlayer.skip();
        }

        ImGui.sameLine();

        if (ImGui.button("Play")) {
            if (WAVPlayer.isPlaying()) {
                WAVPlayer.pause();
            } else {
                WAVPlayer.resume();
            }
        }

        ImGui.sameLine();

        if (ImGui.button("Rewind")) {
            WAVPlayer.rewind();
        }

        ImGui.sameLine();

        if (ImGui.checkbox("Loop", looping)) {
            looping = !looping;
            WAVPlayer.setLooping(looping);
        }

        ImGui.text("Volume");
        float[] volumeArray = new float[1];
        volumeArray[0] = volume;

        ImGui.sameLine();

        if (ImGui.sliderFloat("##Volume", volumeArray, 0.0f, 100.0f, "%.1f")) {
            volume = volumeArray[0];
            WAVPlayer.calcVolume(volume);
        }

        ImGui.endGroup();

        ImGui.end();
    }

    private static void setProgressBar() {
        float textWidth = GuiUtils.calcTextSize("99:99/99:99").x;
        float progressX = (width - textWidth) / 2;
        ImGui.pushStyleColor(ImGuiCol.PlotHistogram, ImColor.rgb(21, 66, 0));

        long currentPosSec = TimeUnit.MICROSECONDS.toSeconds(WAVPlayer.getCurrentPositionLessAccurate());
        long songLength = WAVPlayer.getSongLength();

        ImGui.progressBar((float) currentPosSec /songLength, width, 25, "##");
        ImGui.popStyleColor(1);
        ImGui.sameLine(progressX);
        GuiUtils.setFont(1.3F);

        if (cachedFormattedSongLength == null) {
            cachedFormattedSongLength = MusicUtils.formatTime((int) songLength);
        }

        ImGui.text(MusicUtils.formatTime((int) currentPosSec) + "/" + cachedFormattedSongLength);
        GuiUtils.clearFontSize();
    }

    public static void quit() {
        volume = 100.0f;
        selectedListItem = -1;
        title = "Christmas Celebrator";

        shouldRender = false;
    }

    // Useful methods
    public static void setSelectedSong(int index) {
        selectedListItem = index;
    }
}
