package com.github.qpcrummer.lights.utils;

import com.github.qpcrummer.lights.LightsDebug;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;

public class Flash {
    public Flash() {
        int total_pins = 8;
        for (int i = 0; i < total_pins; i++) {

            // Setup WiringPi
            Gpio.wiringPiSetup();
            GpioUtil.export(i, GpioUtil.DIRECTION_OUT);

            // Turn on the GPIO pin
            Gpio.digitalWrite(i, Gpio.HIGH);

            // Pause for 4s
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Turn off the GPIO pin
            Gpio.digitalWrite(i, Gpio.LOW);
        }

        for (int i = 0; i < total_pins; i++) {
            Gpio.digitalWrite(i, Gpio.HIGH);
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < total_pins; i++) {
            Gpio.digitalWrite(i, Gpio.LOW);
        }
        LightsDebug.flashing_lights_test.setVisible(true);
    }

}
