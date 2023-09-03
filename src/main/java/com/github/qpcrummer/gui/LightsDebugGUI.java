package com.github.qpcrummer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LightsDebugGUI extends JFrame {
    private final Box[] boxes;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public LightsDebugGUI() {
        setTitle("Box Color Toggle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel mainPanel = new JPanel(new GridLayout(4, 4));
        boxes = new Box[16];

        for (int i = 0; i < 16; i++) {
            boxes[i] = new Box(i + 1);
            mainPanel.add(boxes[i]);
        }

        add(mainPanel);
        setVisible(true);
    }

    public void blinkBoxes(int[] channels) {
        for (int num : channels) {
            blink(boxes[num]);
        }
    }

    private void blink(Box box) {
        box.toggleColor();
        executorService.schedule(box::toggleColor, 50, TimeUnit.MILLISECONDS);
    }

    private static class Box extends JLabel {
        private Color currentColor;

        public Box(int number) {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setOpaque(true);
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);
            setText(Integer.toString(number));
            currentColor = Color.BLACK;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggleColor();
                }
            });
        }

        public void toggleColor() {
            currentColor = (currentColor == Color.BLACK) ? Color.BLUE : Color.BLACK;
            setBackground(currentColor);
        }
    }
}
