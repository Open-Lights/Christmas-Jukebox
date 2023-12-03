package com.github.qpcrummer.cui;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.gui.NewJukeboxGUI;
import com.github.qpcrummer.gui.NewPlaylistGUI;
import com.github.qpcrummer.music.WAVPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class ConsoleReader {
    BufferedReader br;
    StringTokenizer st;

    public ConsoleReader() {
        br = new BufferedReader(new InputStreamReader(System.in));
        process(next());
    }

    public String next() {
        while (st == null || !st.hasMoreElements()) {
            try {
                st = new StringTokenizer(br.readLine());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return st.nextToken();
    }

    int nextInt() { return Integer.parseInt(next()); }

    long nextLong() { return Long.parseLong(next()); }

    double nextDouble()
    {
        return Double.parseDouble(next());
    }

    String nextLine() {
        String str = "";
        try {
            if(st.hasMoreTokens()){
                str = st.nextToken("\n");
            }
            else{
                str = br.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public void process(String str) {
        switch (str) {
            // Single string functions
            case "help" -> Main.logger.info("""
                    Welcome to Christmas Celebrator 1.0.0!
                    --------------------------------------
                    
                                Audio
                    - pause: Pauses the current song
                    - play: Resumes the current song
                    - mute: Sets volume to 0
                    - rewind: Restarts the current song
                    - shuffle: Randomizes the songs
                    - skip: Skips to the next song
                    - stop: Stops all music and resets
                    - volume <0-100>: Sets volume
                    
                             Informational
                    - list song | playlist: Lists all songs or playlists
                    - load song | playlist: Loads the song or playlist
                    """);
            case "play" -> {
                if (WAVPlayer.resume()) {
                    Main.logger.info("Resuming");
                } else {
                    Main.logger.warning("Already playing");
                }
            }
            case "pause" -> {
                if (WAVPlayer.pause()) {
                    Main.logger.warning("Pausing");
                } else {
                    Main.logger.warning("Already paused");
                }
            }
            case "skip" -> {
                WAVPlayer.skip();
                Main.logger.info("Skipped");
            }
            case "rewind" -> {
                WAVPlayer.rewind();
                Main.logger.info("Rewound");
            }
            case "mute" -> {
                WAVPlayer.calcVolume(0);
                Main.logger.info("Muted");
            }
            case "stop" -> {
                WAVPlayer.shutDown();
                Main.logger.info("Shutting down audio");
            }
            case "shuffle" -> {
                WAVPlayer.shuffle();
                Main.logger.info("Songs shuffled");
            }
            // Multi-string functions
            case "volume" -> {
                WAVPlayer.calcVolume(nextDouble());
                Main.logger.info("Setting new volume");
            }
            case "load" -> {
                switch (next()) {
                    case "song" -> {
                        int i = nextInt();
                        WAVPlayer.songOverride(i);
                        Main.logger.info("Playing " + NewJukeboxGUI.getTitle(NewJukeboxGUI.songPaths[i]));
                    }
                    case "playlist" -> {
                        int i = nextInt();
                        NewPlaylistGUI.selectedPlaylists[i] = true;
                        NewJukeboxGUI.initialize(NewPlaylistGUI.combinePlayLists());
                        Main.logger.info("Selected " + NewPlaylistGUI.playlists.get(i).getFileName());
                    }
                }
            }
            case "list" -> {
                switch (next()) {
                    case "song" -> {
                        for (int i = 0; i < NewJukeboxGUI.songPaths.length; i++) {
                            Main.logger.info(i + ". " + NewJukeboxGUI.getTitle(NewJukeboxGUI.songPaths[i]));
                        }
                    }
                    case "playlist" -> {
                        for (int i = 0; i < NewPlaylistGUI.playlists.size(); i++) {
                            Main.logger.info(i + ". " + NewPlaylistGUI.playlists.get(i).getFileName());
                        }
                    }
                }
            }

            default -> Main.logger.warning("This command doesn't exist; Type 'help' to see a list of commands");
        }
    }
}
