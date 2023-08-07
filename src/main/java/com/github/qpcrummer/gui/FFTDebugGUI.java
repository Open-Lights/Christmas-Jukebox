package com.github.qpcrummer.gui;

import com.github.qpcrummer.gui_addons.Complex;
import com.github.qpcrummer.gui_addons.ImageViewer;
import com.github.qpcrummer.computation.FFT;
import com.github.qpcrummer.computation.FFTTracker;

import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class FFTDebugGUI extends JFrame {
    private final JPanel viewingPanel = new JPanel();
    private ImageViewer imageViewer;
    private final BufferedImage image = new BufferedImage(300, 128, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage sideStrip = new BufferedImage(10, 128, BufferedImage.TYPE_INT_RGB);
    private final int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
    private final int[] sidePixels = ((DataBufferInt)sideStrip.getRaster().getDataBuffer()).getData();
    private final int[] colorMap = new int[128];
    private final int[] colors = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,  0x1aa3ff, 0x001f33, 0x001f33, 0x001f33};
    private double[] frequencyBins;
    private final double[] magnitudes = new double[128];

    private final Thread thread;
    private final FFTTracker tracker = new FFTTracker();
    public FFTDebugGUI(Thread thread) {
        this.thread = thread;
        init();
        this.pack();
        this.setVisible(true);
    }

    /**
     * Sets up the DebugGUI
     */
    private void init() {
        viewingPanel.setLayout(new BorderLayout());
        this.imageViewer = new ImageViewer(image);
        viewingPanel.add(imageViewer, BorderLayout.CENTER);

        makeColorMap();

        ImageViewer side = new ImageViewer(sideStrip);
        viewingPanel.add(side, BorderLayout.EAST);

        this.add(viewingPanel);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                FFTTracker.running = false;
            }
        });
        this.setMinimumSize(new Dimension(320, 128));
        this.setMaximumSize(new Dimension(320, 128));
    }

    /**
     * Starts the FFT tracking
     * @param inputStream InputStream for currently playing Song
     */
    public void startTracking(AudioInputStream inputStream) {
        this.tracker.start(inputStream,this);
    }

    /**
     * Stops the FFT tracking
     */
    public void stopTracking() {

    }

    /**
     * Resumes the FFT tracking at the specified frame
     * @param currentFrame Resumed frame of the WAV file
     */
    public void resumeTracking(long currentFrame) {

    }

    // Technical stuff

    /**
     * Draws the Spectrograph on the DebugGUI
     * @param samples audio samples from the WAV file
     */
    public void drawSpectrograph(float[] samples) {
        Complex[] data = new Complex[samples.length];
        for (int i = 0; i < samples.length; i++){
            data[i] = new Complex(samples[i], 0);
        }
        Complex[] niz = FFT.fft(data);

        double max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
        for (int i = 0; i < magnitudes.length; i++){

            int startIndex = (int)frequencyBins[i];
            int endIndex = (int)frequencyBins[i+2];

            int amount = (endIndex - startIndex) / 2;
            int amountFull = endIndex - startIndex;

            double maxAmp = Integer.MIN_VALUE;

            for (int j = startIndex; j < endIndex; j++){
                //double amp = Math.sqrt(niz[j].re() * niz[j].re() + niz[j].im() * niz[j].im());

                double amp = 0;
                if (j <= startIndex + amount) {
                    amp = (((j - (startIndex - 1)) * 1.0) / (amount + 1)) * Math.sqrt(niz[j].re() * niz[j].re() + niz[j].im() * niz[j].im());
                }
                else {
                    amp = (((amountFull - (j - startIndex)) * 1.0) / amount) * Math.sqrt(niz[j].re() * niz[j].re() + niz[j].im() * niz[j].im());
                }

                if (amp > maxAmp) maxAmp = amp;
            }
            if (maxAmp == 0) magnitudes[i] = 0;
            else magnitudes[i] = 20 * Math.log10(maxAmp);

            if (magnitudes[i] > max) max = magnitudes[i];
            if (magnitudes[i] < min) min = magnitudes[i];

        }

        if (min < 0){
            min = Math.abs(min);
            for (int i = 0; i < magnitudes.length; i++){
                magnitudes[i] += min;
            }
        }

        double scale = 1.27;
        shiftSpectrograph();
        for (int i = magnitudes.length - 1; i >= 0; i--){
            int x = 127 - Math.abs((int)(magnitudes[127-i] * scale));
            if (x < 0) x = 0;
            pixels[300 * i + 300 - 1] = colorMap[x];
        }

        this.imageViewer = new ImageViewer(image);
        this.repaint();
    }

    /**
     * Shifts the entire Spectrograph over by 1 pixel
     */
    private void shiftSpectrograph() {
        for (int i = 0; i < 128; i++){
            for (int j = 1; j < 300; j++){
                pixels[i * 300 + j - 1] = pixels[i * 300 + j];
            }
        }
    }

    /**
     * Calculates FFT Bins (spectrum samples)
     */
    private void calculateBins() {
        double maxFreq = 22050;
        double division = (double) FFTTracker.DEF_BUFFER_SAMPLE_SZ /2;
        double time = division /maxFreq;
        double minFreq = 1/time;

        frequencyBins = new double[130];
        frequencyBins[0] = minFreq;
        frequencyBins[frequencyBins.length-1] = maxFreq;

        minFreq = melTransform(minFreq);
        maxFreq = melTransform(maxFreq);

        double amount = (maxFreq - minFreq)/(129);

        for (int i = 1; i < frequencyBins.length-1; i++){
            frequencyBins[i] = iMelTransform(minFreq + i * amount);
        }

        System.out.println(Arrays.toString(frequencyBins));
        int index = 0;
        for (int i = 1; i <= division; i++){
            double freq = i / time;
            if (freq >= frequencyBins[index]){
                frequencyBins[index++] = i-1;
            }
            if (index==(130)) break;
        }
        frequencyBins[frequencyBins.length-1] = division;
        System.out.println(Arrays.toString(frequencyBins));
    }

    /**
     * Creates colors for Spectrograph
     */
    private void makeColorMap(){
        int amount = 128 / (colors.length-1) + 1;
        int counter = 0;

        int color1 = 0, color2 = 0;
        for (int i = 0; i < 128; i++){
            if (i % amount == 0 && counter < colors.length-1){
                color1 = colors[counter];
                color2 = colors[counter + 1];
                counter++;
            }

            double x = (i % amount)/(double)amount;
            int color = preLerp(color1, color2, x);

            for (int j = 0; j < sideStrip.getWidth(); j++){
                sidePixels[i*sideStrip.getWidth() + j] = color;
            }
            colorMap[i] = color;
        }
    }

    /**
     * Prepare for Linear Interpolation
     * @param color1 First color value
     * @param color2 Second color value
     * @param x Multiplication factor
     * @return int after Linear Interpolation
     */
    private int preLerp(int color1, int color2, double x) {
        int r = lerp(((color1 & 0xFF0000) >> 16), ((color2 & 0xFF0000) >> 16), x);
        int g = lerp(((color1 & 0x00FF00) >> 8), ((color2 & 0x00FF00) >> 8), x);
        int b = lerp((color1 & 0x0000FF), (color2 & 0x0000FF), x);
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Linear Interpolation
     */
    private int lerp(int a, int b, double x) {
        return (int)(a + (b - a) * x);
    }

    private double melTransform(double freq){
        return 1125 * Math.log(1 + freq/(float)700);
    }

    private double iMelTransform(double freq){
        return 700 * (Math.pow(Math.E, freq/(float)1125) - 1);
    }
}
