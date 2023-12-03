package com.github.qpcrummer.music;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.github.qpcrummer.Main;
import com.github.qpcrummer.directories.Directories;
import com.github.qpcrummer.gui.NewJukeboxGUI;
import com.github.qpcrummer.gui.NewPlaylistGUI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class MusicUtils {

    public static final List<Path> playlists = listPlaylists();
    public static final boolean[] selectedPlaylists = new boolean[playlists.size()];

    /**
     * Combines multiple Playlist paths into one
     * @return Combined song paths for both playlists
     */
    public static Path[] combinePlayLists() {
        final List<Path> combined = new ArrayList<>();
        for (Path path : getSelectedPlaylists()) {
            combined.addAll(Arrays.asList(createSongArrayList(path)));
        }
        return combined.toArray(new Path[0]);
    }

    /**
     * Iterates through files to find songs
     * @param path Playlist path
     * @return All songs' paths within a playlist
     */
    private static Path[] createSongArrayList(final Path path) {

        List<Path> paths = new ArrayList<>();

        try (final Stream<Path> walk = Files.walk(path)) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                Directories.createBeatDirectory(file);
                paths.add(file);
            });
        } catch (IOException e) {
            Main.logger.warning("File path not accessible!");
        }
        return paths.toArray(new Path[0]);
    }

    /**
     * Gets all playlists that have 'true' in selectedPlaylists
     * @return All playlists' paths that are selected
     */
    private static List<Path> getSelectedPlaylists() {
        List<Path> path = new ArrayList<>();
        for (int i = 0; i < selectedPlaylists.length; i++) {
            if (selectedPlaylists[i]) {
                path.add(playlists.get(i));
            }
        }
        return path;
    }

    /**
     * This method lists all playlists that will be loaded
     */
    private static List<Path> listPlaylists() {
        final List<Path> playlists = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(Directories.music, Files::isDirectory)) {
            StreamSupport.stream(stream.spliterator(), false).forEach(playlists::add);
        } catch (IOException e) {
            Main.logger.warning("Playlist path not accessible!");
        }
        return playlists;
    }

    /**
     * Initializes values for WAVPlayer.
     * If it is in GUI mode, it also initializes the titles for the GUI
     * @param paths Song paths
     */
    public static void initializeJukebox(Path[] paths) {
        WAVPlayer.songPaths = paths;

        if (!Main.cli) {
            NewJukeboxGUI.titleList = new String[WAVPlayer.songPaths.length];
            for (int i = 0; i < WAVPlayer.songPaths.length; i++) {
                NewJukeboxGUI.titleList[i] = getTitle(WAVPlayer.songPaths[i]);
            }
        }

        WAVPlayer.initialize();
    }

    /**
     * Retrieves the Author and Song name from a path
     * @param path Song path
     * @return String: "song_name by  author"
     */
    public static String getTitle(Path path) {
        final File file = new File(String.valueOf(path));
        return file.getName().replace(".wav", "").replace("_", " ") + " by " + getAuthor(path);
    }

    /**
     * Gets the author value from WAV metadata
     * @param path Song path
     * @return "Artist" value in WAV metadata as a String
     */
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

    /**
     * Shuts down the WAVPlayer.
     * If in GUI mode, it also resets the GUIs
     */
    public static void quit() {
        WAVPlayer.shutDown();

        if (!Main.cli) {
            NewJukeboxGUI.quit();
            NewPlaylistGUI.shouldRender = true;
        }

        Arrays.fill(selectedPlaylists, false);
    }

    /**
     * Correctly format the progress bar
     * @param seconds Current position of the song in seconds
     * @return The formatted time
     */
    public static String formatTime(final int seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        return String.format("%02d:%02d", duration.toMinutesPart(), duration.toSecondsPart());
    }
}
