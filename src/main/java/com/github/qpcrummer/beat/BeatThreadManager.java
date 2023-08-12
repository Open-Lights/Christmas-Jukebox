package com.github.qpcrummer.beat;

import com.github.qpcrummer.music.WAVPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BeatThreadManager {
    private final WAVPlayer player;
    private volatile boolean running = false;
    private Thread thread;
    private CountDownLatch pauseLatch = new CountDownLatch(1);
    private final BeatTranslator translator;
    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
    public BeatThreadManager(WAVPlayer player, BeatTranslator translator) {
        this.player = player;
        this.translator = translator;
    }

    public void startThread() {
        // Check if thread is already running
        if (running) {
            return;
        }

        this.translator.readBeats(this.player.getCurrentSong());

        running = true;
        thread = new Thread(() -> {
            List<ChannelIdentifier> channels = translator.getChannels();
            List<Integer> currentIndices = new ArrayList<>(Collections.nCopies(channels.size(), 0));

            while (running && !channels.isEmpty()) {
                for (int i = 0; i < channels.size(); i++) {
                    ChannelIdentifier channel = channels.get(i);
                    int currentIndex = currentIndices.get(i);

                    if (currentIndex < channel.beats.size()) {
                        long currentBeat = channel.beats.get(currentIndex);
                        long currentPosition = player.getWavClip().getMicrosecondPosition();

                        long difference = currentBeat - currentPosition;

                        if (difference > 0) {
                            try {
                                if (pauseLatch.await(difference, TimeUnit.MICROSECONDS)) {
                                    pauseLatch = new CountDownLatch(1);
                                    continue;
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                System.out.println("Beat Thread Interrupted");
                            }
                        }

                        if (player.getWavClip().getMicrosecondPosition() >= currentBeat) {
                            System.out.println("Beat on channel " + Arrays.toString(channel.channels));
                            currentIndices.set(i, currentIndex + 1); // Move to the next beat for this channel
                        }
                    }
                }
            }

            running = false;
        });

        thread.start();
    }

    /**
     * Pauses the Thread and saves the current spot
     */
    public void pauseThread() {
        running = false;
        pauseLatch.countDown();
    }

    /**
     * Resumes the Thread
     */
    public void resumeThread() {
        running = true;
        pauseLatch.countDown();
    }

    /**
     * Completely stops the Thread and sends it off to the GC
     */
    public void stopThread() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }
}
