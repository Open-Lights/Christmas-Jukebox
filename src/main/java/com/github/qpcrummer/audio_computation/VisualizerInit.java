package com.github.qpcrummer.audio_computation;

import javax.swing.*;

public class VisualizerInit {
    public static String mode = "";

    public static void init(String[] args) {
        if (args.length != 1 || (!args[0].equals("mic") && !args[0].equals("player"))) {
            System.out.println("Error - invalid arguments. Please pass argument: mic or player");
            return;
        }
        mode = args[0];

        SwingUtilities.invokeLater(VisualizerInit::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AudioVisualizer view = AudioVisualizer.get();
        frame.getContentPane().add(view);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
