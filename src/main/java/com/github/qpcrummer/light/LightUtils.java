package com.github.qpcrummer.light;

import com.diozero.devices.LED;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class LightUtils {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final int TOTAL_CHANNELS = 12;
    private static final LED[] leds = new LED[TOTAL_CHANNELS];
    public static void readyLights() {
        for (int i = 0; i < TOTAL_CHANNELS; i++) {
            leds[i] = new LED(i);
        }

        allOff();
    }
    public static void blinkLED(int channel) {
        leds[channel].off();
        executor.schedule(() -> leds[channel].on(), 200, TimeUnit.MILLISECONDS);
    }

    public static void blinkLED(int channel, long holdTime) {
        leds[channel].off();
        executor.schedule(() -> leds[channel].on(), holdTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Inverted because our relay board is crong
     */
    public static void allOff() {
        for (LED led : leds) {
            led.on();
        }
    }

    /**
     * Inverted because our relay board is crong
     */
    public static void allOn() {
        for (LED led : leds) {
            led.off();
        }
    }
}
