package com.github.qpcrummer.gui_addons;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer extends JPanel {
    private final BufferedImage image;

    public ImageViewer(BufferedImage image) {
        this.image = image;
        // Set the preferred size of the component based on the image dimensions
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the image onto the component
        g.drawImage(image, 0, 0, null);
    }
}
