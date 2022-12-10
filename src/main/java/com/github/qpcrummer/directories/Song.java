package com.github.qpcrummer.directories;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Song {
    public final String name;
    public final String formatted_name;
    public final Path path;
    public final String author;
    public final String title;

    public Song(String name, Path path) {
        this.name = name;
        this.formatted_name = name.replace(".wav", "").replace("_", " ");
        this.path = path;
        this.author = getAuthor();
        this.title = formatted_name + " by " + this.author;
    }

    /**
     * This method returns the author of the song in the Metadata
     * @return The name of the author
     */
    private String getAuthor() {
        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(songAsFile());
            for (final Directory directory : metadata.getDirectories()) {
                for (final Tag tag : directory.getTags()) {
                    if (Objects.equals(tag.getTagName(), "Artist")) {
                        return tag.getDescription();
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            throw new RuntimeException(e);
        }
        return "Unknown";
    }

    /**
     * Converts a song's path to a File
     * @return The song as a file
     */
    private File songAsFile() {
        return new File(String.valueOf(path));
    }
}
