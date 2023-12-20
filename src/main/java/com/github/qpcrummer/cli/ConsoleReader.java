package com.github.qpcrummer.cli;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.light.LightUtils;
import com.github.qpcrummer.music.MusicUtils;
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
                Main.logger.warning("Failed to read console");
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

    public void process(String str) {
        switch (str) {
            // Single string functions
            case "help" -> Main.logger.info("""
                    Welcome to Christmas Celebrator 1.0.0!
                    --------------------------------------
                    
                                Audio
                    - loop: Loops the current song
                    - pause: Pauses the current song
                    - play: Resumes the current song
                    - mute: Sets volume to 0
                    - rewind: Restarts the current song
                    - shuffle: Randomizes the songs
                    - skip: Skips to the next song
                    - stop: Stops all music and resets
                    - volume <0-100>: Sets volume
                    
                             Informational
                    - info: Gets information about the current song
                    - list song | playlist: Lists all songs or playlists
                    - load song | playlist: Loads the song or playlist
                    
                                Testing
                    - blink <channel>
                    - allon
                    - alloff
                    """);
            case "info" -> Main.logger.info("\nSong: " + MusicUtils.getTitle(WAVPlayer.getPath(WAVPlayer.getCurrentSong())) + "\nIndex: " + WAVPlayer.getCurrentSong() + "\nLength: " + MusicUtils.formatTime((int) WAVPlayer.getSongLength()) + "\nVolume: " + WAVPlayer.getVolume());
            case "loop" -> {
                if (WAVPlayer.toggleLooping()) {
                    Main.logger.info("Looping song");
                } else {
                    Main.logger.info("Disabling looping");
                }
            }
            case "play" -> {
                if (WAVPlayer.resume()) {
                    Main.logger.info("Resuming");
                } else {
                    Main.logger.warning("Already playing");
                }
            }
            case "pause" -> {
                if (WAVPlayer.pause()) {
                    Main.logger.info("Pausing");
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
                MusicUtils.quit();
                Main.logger.info("Shutting down audio");
            }
            case "shuffle" -> {
                WAVPlayer.shuffle();
                Main.logger.info("Songs shuffled");
            }
            case "allon" -> {
                LightUtils.allOn();
                Main.logger.info("All Lights On");
            }
            case "alloff" -> {
                LightUtils.allOff();
                Main.logger.info("All Lights Off");
            }
            // Multi-string functions
            case "blink" -> {
                LightUtils.blinkLED(nextInt());
                Main.logger.info("Light Blinks For 2 Seconds");
            }
            case "volume" -> {
                WAVPlayer.calcVolume(nextDouble());
                Main.logger.info("Setting new volume");
            }
            case "load" -> {
                switch (next()) {
                    case "song" -> {
                        int i = nextInt();
                        WAVPlayer.songOverride(i);
                        Main.logger.info("Playing " + MusicUtils.getTitle(WAVPlayer.songPaths[i]));
                    }
                    case "playlist" -> {
                        int i = nextInt();
                        MusicUtils.selectedPlaylists[i] = true;
                        MusicUtils.initializeJukebox(MusicUtils.combinePlayLists());
                        Main.logger.info("Selected " + MusicUtils.playlists.get(i).getFileName());
                    }
                }
            }
            case "list" -> {
                switch (next()) {
                    case "song" -> {
                        StringBuilder output = new StringBuilder();
                        for (int i = 0; i < WAVPlayer.songPaths.length; i++) {
                            output.append(Main.newLine);
                            output.append(i).append(". ").append(MusicUtils.getTitle(WAVPlayer.songPaths[i]));
                        }
                        Main.logger.info(output.toString());
                    }
                    case "playlist" -> {
                        StringBuilder output = new StringBuilder();
                        for (int i = 0; i < MusicUtils.playlists.size(); i++) {
                            output.append(Main.newLine);
                            output.append(i).append(". ").append(MusicUtils.playlists.get(i).getFileName());
                        }
                        Main.logger.info(output.toString());
                    }
                }
            }

            default -> Main.logger.warning("This command doesn't exist; Type 'help' to see a list of commands");
        }
    }
}
