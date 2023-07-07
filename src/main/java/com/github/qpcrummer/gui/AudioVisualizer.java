package com.github.qpcrummer.gui;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class AudioVisualizer extends JFrame {
    private final VisualizerPanel visualizerPanel;
    private AudioFormat audioFormat;

    public AudioVisualizer(String path) {
        setTitle("Audio Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        visualizerPanel = new VisualizerPanel();
        add(visualizerPanel);

        initializeAudioFormat();

        // Start audio capture and visualization on a separate thread
        Thread captureThread = new Thread(() -> startAudioCapture(path));
        captureThread.start();
    }

    private void initializeAudioFormat() {
        int sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = false;

        audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    private void startAudioCapture(String path) {
        try {
            File audioFile = new File(path);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat fileAudioFormat = audioInputStream.getFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, fileAudioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(fileAudioFormat);
            sourceDataLine.start();

            int bufferSize = sourceDataLine.getBufferSize();
            int numBytesRead;
            byte[] buffer = new byte[bufferSize];

            while ((numBytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                float[] magnitudes = calculateMagnitudes(buffer, numBytesRead); // Custom method to calculate magnitudes

                SwingUtilities.invokeLater(() -> {
                    visualizerPanel.setMagnitudes(magnitudes);
                    visualizerPanel.repaint();
                });

                // Write audio data to system audio output
                sourceDataLine.write(buffer, 0, numBytesRead);
            }

            audioInputStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private float[] calculateMagnitudes(byte[] audioData, int numBytesRead) {
        // Custom method to calculate magnitudes based on audio data
        // You can implement your own logic to determine the magnitudes for the visualizer bars

        int numBars = visualizerPanel.getWidth() / 20; // Adjust the bar width based on the visualizer panel's width
        float[] magnitudes = new float[numBars];

        // Assuming 16-bit stereo audio data
        int bytesPerSample = audioFormat.getSampleSizeInBits() / 8;
        int numSamples = numBytesRead / (bytesPerSample * audioFormat.getChannels());

        // Loop through the audio samples and calculate magnitudes
        for (int i = 0; i < numBars; i++) {
            // Calculate the start and end indices for each bar
            int startIndex = i * numSamples / numBars;
            int endIndex = (i + 1) * numSamples / numBars;

            // Calculate the maximum magnitude in the range of the current bar
            float maxMagnitude = 0;
            for (int j = startIndex; j < endIndex; j++) {
                // Extract the audio sample for the current channel
                int sampleIndex = j * audioFormat.getChannels() * bytesPerSample;
                float sample = 0;
                for (int k = 0; k < audioFormat.getChannels(); k++) {
                    int sampleValue = 0;
                    for (int b = 0; b < bytesPerSample; b++) {
                        int byteValue = audioData[sampleIndex + (k * bytesPerSample) + b] & 0xFF;
                        sampleValue |= byteValue << (8 * (bytesPerSample - 1 - b));
                    }
                    sample += sampleValue / (float) ((1 << (audioFormat.getSampleSizeInBits() - 1)) - 1);
                }

                // Calculate the magnitude of the sample
                float magnitude = Math.abs(sample);

                // Update the maximum magnitude if necessary
                if (magnitude > maxMagnitude) {
                    maxMagnitude = magnitude;
                }
            }

            // Set the bar magnitude to the maximum magnitude of the range
            magnitudes[i] = maxMagnitude;
        }

        return magnitudes;
    }
}

class VisualizerPanel extends JPanel {
    private float[] magnitudes;

    public VisualizerPanel() {
        setBackground(Color.BLACK);
    }

    public void setMagnitudes(float[] magnitudes) {
        this.magnitudes = magnitudes;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.GREEN);

        int barWidth = getWidth() / magnitudes.length;
        int maxHeight = getHeight();

        for (int i = 0; i < magnitudes.length; i++) {
            int barHeight = (int) (maxHeight * magnitudes[i]);
            int x = i * barWidth;
            int y = maxHeight - barHeight;

            g2d.fillRect(x, y, barWidth, barHeight);
        }

        g2d.dispose();
    }
}