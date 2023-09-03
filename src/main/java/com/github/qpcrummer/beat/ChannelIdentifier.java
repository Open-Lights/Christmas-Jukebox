package com.github.qpcrummer.beat;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.music.WAVPlayer;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChannelIdentifier {
    public final int[] channels;
    public final List<Long> beats;
    private final ScheduledExecutorService executor;
    private final WAVPlayer player;
    private boolean isSleeping;
    private int index;
    public ChannelIdentifier(String fileName, List<Long> beats, ScheduledExecutorService executor, WAVPlayer player) {
        this.channels = extractIntArray(fileName);
        this.beats = beats;
        this.executor = executor;
        this.player = player;
    }

    private int[] extractIntArray(String input) {
        StringBuilder digitString = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                digitString.append(c);
            }
        }

        int[] intArray = new int[digitString.length()];

        for (int i = 0; i < digitString.length(); i++) {
            intArray[i] = Character.getNumericValue(digitString.charAt(i));
        }

        return intArray;
    }

    /**
     * If the channel is waiting for an execution, it will execute
     */
    public void checkIfBeat() {
        try {
            if (!isSleeping) {
                if (index >= beats.size()) {
                    return;
                }
                long beat = beats.get(index);
                int difference = (int) ((beat - player.getWavClip().getMicrosecondPosition()) / 1000);
                isSleeping = true;

                this.executor.schedule(() -> {
                    if (player.getWavClip().getMicrosecondPosition() >= beat) {
                        Main.lightsDebugGUI.blinkBoxes(channels);
                        index++;
                    }
                    isSleeping = false;
                    checkIfBeat();
                }, difference, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        this.index = 0;
        checkIfBeat();
    }
}
