package com.github.qpcrummer.gui;

import com.github.qpcrummer.directories.Song;

import javax.swing.*;
import java.awt.*;

public class SongListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // Call super to get the default rendering
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Set the text representation of the Song
        if (value instanceof Song song) {
            setText(song.title);
        }

        return component;
    }
}
