package com.github.qpcrummer.audio_computation;

import javax.swing.*;
import java.awt.*;

public class VisualizerInit {
    public static String mode = "player";
    public static AudioVisualizer view = AudioVisualizer.get();;

    public static void init() {
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setMinimumSize(new Dimension(400, 200));

        view.pack();
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }
}
