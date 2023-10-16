package com.github.qpcrummer.directories;

import com.github.qpcrummer.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Directories {

    private Directories() {
    }

    public static final Path music = Paths.get("celebrator/music");
    public static final Path main = Paths.get("celebrator");
    public static final Path beats = Paths.get("celebrator/beats");

    /**
     * Creates all the directories needed for this application
     */
    public static void createDirectories() {
        try {

            if (Files.notExists(main)) {
                Files.createDirectory(main);
            }
            if (Files.notExists(music)) {
                Files.createDirectory(music);
            }
            if (Files.notExists(Directories.beats)) {
                Files.createDirectory(Directories.beats);
            }
        } catch(IOException ignored) {
        }
    }

    public static void createBeatDirectory(final Path path) {
        final Path songBeatPath = Directories.getBeatPath(path);
        if (Files.notExists(songBeatPath)) {
            try {
                Files.createDirectory(songBeatPath);
            } catch (IOException e) {
                Main.logger.warning("Failed to create beats folder for Song: " + path);
            }
        }
    }

    public static Path getBeatPath(Path songInput) {
        return Path.of(Directories.beats + "/" + Directories.getFileNameWithoutExtension(songInput));
    }

    public static String getFileNameWithoutExtension(Path input) {
        String inputString = input.toString().replace("\\", "/");
        return inputString.substring(inputString.lastIndexOf("/") + 1).trim().replace(".wav", "");
    }
}
