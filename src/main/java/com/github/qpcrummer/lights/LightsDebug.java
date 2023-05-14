package com.github.qpcrummer.lights;

import com.github.qpcrummer.lights.utils.Flash;

import javax.swing.*;
import java.awt.*;

public class LightsDebug {
    public static JFrame frame = new JFrame("Light Debug");
    public static JPanel panel = new JPanel();
    public static JButton flashing_lights_test = new JButton("Flashing Test");

    public static void initLightGUI() {
        frame.add(panel);

        panel.add(flashing_lights_test);

        flashing_lights_test.addActionListener(e -> {
            flashing_lights_test.setVisible(false);
            new Flash();
        });
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(600, 400));
        frame.pack();
        frame.setVisible(true);
    }
}
