package com.github.qpcrummer.lights.utils;

import com.diozero.devices.LED;

public class Flash {

    public Flash() {
        for (int i = 0; i < 16; i++) {
            try (LED led = new LED(i)) {
                led.onOffLoop(1, 1, 10, false, null);
            }
        }
    }

}
