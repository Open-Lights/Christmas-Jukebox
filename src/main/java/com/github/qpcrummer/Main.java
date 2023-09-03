package com.github.qpcrummer;

import com.github.qpcrummer.directories.Directories;
import com.github.qpcrummer.gui.LightsDebugGUI;
import com.github.qpcrummer.gui.PlaylistGUI;

public class Main {
    public static void main(final String[] args) {
        Directories.createDirectories();
        new PlaylistGUI();
    }

    public static LightsDebugGUI lightsDebugGUI = new LightsDebugGUI();
}