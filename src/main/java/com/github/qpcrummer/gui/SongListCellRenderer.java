package com.github.qpcrummer.gui;

import com.github.qpcrummer.directories.Song;

import javax.swing.*;
import java.awt.*;

public class SongListCellRenderer extends DefaultListCellRenderer {

    public SongListCellRenderer() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        // Call super to get the default rendering
        final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Set the text representation of the Song
        if (value instanceof Song song) {
            setText(song.title);
        }

        return component;
    }
}
