package com.github.qpcrummer.beat;

import com.diozero.devices.LED;
import com.github.qpcrummer.Main;
import com.github.qpcrummer.music.WAVPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelIdentifier {
    public final int[] channels;
    public final List<Object> beats;
    private final ScheduledExecutorService executor;
    private boolean isSleeping;
    private int index;
    //private final LED[] controller;
    public ChannelIdentifier(final String fileName, final List<Object> beats, final ScheduledExecutorService executor) {
        this.channels = extractIntArray(fileName);
        this.beats = beats;
        this.executor = executor;
        //this.controller = new LED[this.channels.length];
        //for (int i = 0; i < channels.length; i++) {
        //    controller[i] = new LED(channels[i]);
        //}
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

    /**
     * If the channel is waiting for an execution, it will execute
     */
    public void checkIfBeat() {
        if (!isSleeping && WAVPlayer.isPlaying()) {
            if (index >= beats.size()) {
                return;
            }
            final Object preBeat = beats.get(index);
            long beat;
            int holdDuration;

            if (preBeat instanceof long[] array) {
                beat = array[0];
                holdDuration = (int) (array[1] - beat);
            } else {
                holdDuration = 0;
                beat = (long)preBeat;
            }
            final int difference = (int) ((beat - WAVPlayer.getWavClip().getMicrosecondPosition()) / 1000);

            if (this.isTimeClose(WAVPlayer.getWavClip().getMicrosecondPosition(), beat)) {
                this.scheduleLEDTask(holdDuration);
            }

            this.isSleeping = true;

            this.executor.schedule(() -> {
                isSleeping = false;
                checkIfBeat();
            }, difference, TimeUnit.MILLISECONDS);
        }
    }

    public void reset() {
        this.index = 0;
        turnAllLightsOff();
        checkIfBeat();
    }

    public void resume() {
        if (this.isSleeping) {
            return;
        }

        final Object currentBeat = this.beats.get(this.index);

        long time;

        if (currentBeat instanceof long[] beat) {
            time = beat[0];
        } else {
            time = (long)currentBeat;
        }

        if (time <= WAVPlayer.getCurrentPosition()) {
            index++;
        }
        checkIfBeat();
    }

    private boolean isTimeClose(final long clipPos, final long expectedPos) {
        return clipPos >= expectedPos;
    }

    private void scheduleLEDTask(long holdTime) {
        this.turnAllLightsOn();

        if (holdTime > 0) {
            this.executor.schedule(this::turnAllLightsOff, holdTime, TimeUnit.MILLISECONDS);
        } else {
            this.executor.schedule(this::turnAllLightsOff, 200, TimeUnit.MILLISECONDS);
        }
        this.index++;

        // TODO Testing
        Main.logger.info("Beat Scheduled: " + holdTime);
    }

    private void turnAllLightsOn() {
        //for (LED light : this.controller) {
        //    light.on();
        //}
    }

    private void turnAllLightsOff() {
        //for (LED light : this.controller) {
        //    light.off();
        //}
    }

    private void closeAllLights() {
        //for (LED light : this.controller) {
        //    light.close();
        //}
    }
}
