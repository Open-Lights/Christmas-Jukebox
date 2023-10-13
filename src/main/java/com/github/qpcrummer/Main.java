package com.github.qpcrummer;

import com.github.qpcrummer.directories.Directories;
import com.github.qpcrummer.gui.LightsDebugGUI;
import com.github.qpcrummer.gui.PlaylistGUI;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {

    private Main() {
    }

    public static final LightsDebugGUI lightsDebugGUI = new LightsDebugGUI();
    public static final Logger logger = Logger.getLogger("Christmas Celebrator");

    public static void main(final String[] args) {
        logger.info("Loading Christmas Celebrator");
        Directories.createDirectories();
        SwingUtilities.invokeLater(PlaylistGUI::new);
    }
}