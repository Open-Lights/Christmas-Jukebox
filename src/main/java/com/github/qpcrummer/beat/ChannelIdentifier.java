package com.github.qpcrummer.beat;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ChannelIdentifier {
    public final int[] channels;
    public final List<Long> beats;
    private final ScheduledThreadPoolExecutor executor;
    private boolean isSleeping;
    public ChannelIdentifier(String fileName, List<Long> beats, ScheduledThreadPoolExecutor executor) {
        this.channels = extractIntArray(fileName);
        this.beats = beats;
        this.executor = executor;
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

    public void checkIfBeat() {
        if (!isSleeping) {
            this.executor.execute(() -> {
                
            });
        }
    }
}
