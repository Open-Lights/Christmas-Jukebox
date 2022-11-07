package com.github.qpcrummer.Directories;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.qpcrummer.Main.*;

public class Directories {

    public static Path music = Paths.get("celebrator/music");
    public static Path main = Paths.get("celebrator");
    public static List<Path> current_song_list;

    public static void createDirectories() {
        try {

            if (Files.notExists(main)) Files.createDirectory(main);
            if (Files.notExists(music)) Files.createDirectory(music);
            Path config = Paths.get("celebrator/config");
            if (Files.notExists(config)) Files.createDirectory(config);

            Path config_file = Paths.get(config + "/config.properties");
            if (Files.notExists(config_file)) {
                OutputStream outputStream = Files.newOutputStream(config_file);
                outputStream.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Path> listSongs(Path path) throws IOException {
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static List<Path> listPlaylistPaths() throws IOException {
        try (var stream = Files.newDirectoryStream(music, Files::isDirectory)) {
            return StreamSupport.stream(stream.spliterator(), false).toList();
        }
    }

    public static List<String> listPlaylists() throws IOException {
        List<String> playlists = new ArrayList<>();
        for (int i = 0; i < listPlaylistPaths().size(); i++) {
            playlists.add(listPlaylistPaths().get(i).toFile().getName());
        }
        return playlists;
    }

    public static Path playlist2Path(String playlist) {
        return Paths.get(music + "\\" + playlist);
    }

    public static void populateHashMap() throws IOException {
        for (int i = 0; i < listSongs(music).size(); i++) {
            File song = listSongs(music).get(i).toFile();
            stored_data.put(path2Name(listSongs(music).get(i)), song);
        }

        //Test
        try {
            for (int i = 0; i < listSongs(music).size(); i++) {
                Metadata metadata = ImageMetadataReader.readMetadata(songAsFile(listSongs(music).get(i)));
                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        if (Objects.equals(tag.getTagName(), "Artist")) {
                            stored_authors.put(path2Name(listSongs(music).get(i)), tag.getDescription());
                        }
                    }
                }
            }
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println(stored_authors.toString());
    }
}
