package com.github.qpcrummer.gui;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.github.qpcrummer.Main;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SongListCellRenderer extends DefaultListCellRenderer {

    public SongListCellRenderer() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        // Call super to get the default rendering
        final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Set the text representation of the Song
        if (value instanceof Path path) {
            setText(this.getTitle(path));
        }

        return component;
    }

    //TODO Migrate away from using the Song class
    private String getTitle(Path path) {
        final File file = new File(String.valueOf(path));
        return file.getName().replace(".wav", "").replace("_", " ") + " by " + this.getAuthor(path);
    }

    private String getAuthor(Path path) {
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
