package com.github.qpcrummer.beat;

import com.github.qpcrummer.light.LightUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Channel {
    public final int[] channels;
    public final List<Object> beats;
    public int index = 0;

    public Channel(final String fileName, final List<Object> beats) {
        this.channels = this.extractIntArray(fileName);
        this.beats = beats;
    }

    private int[] extractIntArray(final String input) {
        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(input);

        final ArrayList<Integer> numberList = new ArrayList<>();
        while (matcher.find()) {
            final int number = Integer.parseInt(matcher.group());
            numberList.add(number);
        }

        final int[] numbers = new int[numberList.size()];
        for (int i = 0; i < numberList.size(); i++) {
            numbers[i] = numberList.get(i);
        }

        return numbers;
    }

    public void beatCheck(long currentPosition) {
        if (isTimeClose(currentPosition, getBeat())) {
            event();
        }
    }

    private long getBeat() {
        final Object preBeat = beats.get(index);

        if (preBeat instanceof long[] array) {
            return array[0];
        } else {
            return (long)preBeat;
        }
    }

    private boolean isTimeClose(final long clipPos, final long expectedPos) {
        return clipPos >= expectedPos;
    }

    public void reset() {
        LightUtils.allOff();
        this.index = 0;
    }

    public void event() {
        if (beats.get(index) instanceof long[] array) {
            int holdDuration = (int) (array[1] - array[0]);
            for (int channel : this.channels) {
                LightUtils.blinkLED(channel, holdDuration);
            }
        } else {
            for (int channel : this.channels) {
                LightUtils.blinkLED(channel);
            }
        }

        this.index++;
    }
}
