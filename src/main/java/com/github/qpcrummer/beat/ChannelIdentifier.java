package com.github.qpcrummer.beat;

import com.github.qpcrummer.Main;
import com.github.qpcrummer.music.WAVPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelIdentifier {
    public final int[] channels;
    public final List<Object> beats;
    private final ScheduledExecutorService executor;
    private final WAVPlayer player;
    private boolean isSleeping;
    private int index;
    public ChannelIdentifier(String fileName, List<Object> beats, ScheduledExecutorService executor, WAVPlayer player) {
        this.channels = extractIntArray(fileName);
        this.beats = beats;
        this.executor = executor;
        this.player = player;
    }

    private int[] extractIntArray(String input) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        ArrayList<Integer> numberList = new ArrayList<>();
        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group());
            numberList.add(number);
        }

        int[] numbers = new int[numberList.size()];
        for (int i = 0; i < numberList.size(); i++) {
            numbers[i] = numberList.get(i);
        }

        return numbers;
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
                Object preBeat = beats.get(index);
                long beat;
                int holdDuration;

                if (preBeat instanceof long[] array) {
                    beat = array[0];
                    holdDuration = (int) (array[1] - beat);
                } else {
                    holdDuration = 0;
                    beat = (long)preBeat;
                }
                int difference = (int) ((beat - player.getWavClip().getMicrosecondPosition()) / 1000);

                isSleeping = true;

                this.executor.schedule(() -> {
                    if (player.getWavClip().getMicrosecondPosition() >= beat) {
                        Main.lightsDebugGUI.blinkBoxes(channels, holdDuration);
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
