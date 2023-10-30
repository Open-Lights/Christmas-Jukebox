package com.github.qpcrummer.gui;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.github.qpcrummer.Main;
import com.github.qpcrummer.music.WAVPlayer;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class NewJukeboxGUI {
    public static boolean shouldRender;
    public static Path[] songPaths;
    public static String title = "Christmas Celebrator";
    private static int selectedListItem = -1;
    private static float volume = 100.0f;
    public static String[] titleList;
    private static final float width = ImGui.getIO().getDisplaySizeX() - 15;

    // ProgressBar variables
    public static float progressBar;
    public static String timeStamp = "0:00";

    private static boolean looping;
    private static final WAVPlayer wavPlayer = new WAVPlayer();

    public static void render() {
        if (!shouldRender) {
            return;
        }
        ImGui.begin(title, ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar);

        ImGui.setWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.setWindowPos(0F, 0F);

        if (ImGui.button("Back", width, 20)) {
            shouldRender = false;
            NewPlaylistGUI.shouldRender = true;
        }

        ImGui.separator();

        ImGui.text("List of Items");

        // List of Strings
        GuiUtils.setFont(1.3F);
        if (ImGui.beginListBox("List of Items", width, ImGui.getIO().getDisplaySizeY() * 0.75F)) {
            for (int i = 0; i < titleList.length; i++) {
                boolean isSelected = i == selectedListItem;
                if (ImGui.selectable(titleList[i], isSelected)) {
                    selectedListItem = i;
                    wavPlayer.songOverride(i);
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
            wavPlayer.shuffle();
        }

        ImGui.sameLine();

        if (ImGui.button("Skip")) {
            wavPlayer.skip();
        }

        ImGui.sameLine();

        if (ImGui.button("Play")) {
            wavPlayer.resume();
        }

        ImGui.sameLine();

        if (ImGui.button("Rewind")) {
            wavPlayer.rewind();
        }

        ImGui.sameLine();

        if (ImGui.checkbox("Loop", looping)) {
            looping = !looping;
            wavPlayer.setLooping(looping);
        }

        ImGui.text("Volume");
        float[] volumeArray = new float[1];
        volumeArray[0] = volume;

        ImGui.sameLine();

        if (ImGui.sliderFloat("##Volume", volumeArray, 0.0f, 100.0f, "%.1f")) {
            volume = volumeArray[0];
            wavPlayer.calcVolume(volume);
        }

        ImGui.endGroup();

        ImGui.end();
    }

    private static void setProgressBar() {
        float textWidth = calcTextSize("99:99").x;
        float progressX = (width - textWidth) / 2;
        ImGui.pushStyleColor(ImGuiCol.PlotHistogram, ImColor.intToColor(21, 66, 0));
        ImGui.progressBar(progressBar, width, 25, "##");
        ImGui.popStyleColor(1);
        ImGui.sameLine(progressX);
        GuiUtils.setFont(1.3F);
        ImGui.text(timeStamp);
        GuiUtils.clearFontSize();
    }

    private static ImVec2 calcTextSize(String text) {
        ImVec2 value = new ImVec2();
        ImGui.calcTextSize(value, text);
        return value;
    }

    public static void initialize(Path[] paths) {
        songPaths = paths;

        titleList = new String[songPaths.length];
        for (int i = 0; i < songPaths.length; i++) {
            titleList[i] = getTitle(songPaths[i]);
        }

        wavPlayer.initialize();
    }

    // Useful methods
    public static void setSelectedSong(int index) {
        selectedListItem = index;
    }

    private static String getTitle(Path path) {
        final File file = new File(String.valueOf(path));
        return file.getName().replace(".wav", "").replace("_", " ") + " by " + getAuthor(path);
    }

    private static String getAuthor(Path path) {
        try (InputStream stream = Files.newInputStream(path)) {
            final Metadata metadata = ImageMetadataReader.readMetadata(stream);
            for (final Directory directory : metadata.getDirectories()) {
                for (final Tag tag : directory.getTags()) {
                    if (Objects.equals(tag.getTagName(), "Artist")) {
                        return tag.getDescription();
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            Main.logger.warning("Failed to read Author metadata for Song: " + path);
        }
        return "Unknown";
    }
}
