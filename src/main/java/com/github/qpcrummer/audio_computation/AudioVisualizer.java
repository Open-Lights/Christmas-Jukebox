package com.github.qpcrummer.audio_computation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class AudioVisualizer extends JFrame {

    private static AudioVisualizer instance;

    public static final int height = 128, width = 300;

    public static BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
    public static int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

    public static BufferedImage sideStrip = new BufferedImage(10, height,BufferedImage.TYPE_INT_RGB);
    public static int[] sidePixels = ((DataBufferInt)sideStrip.getRaster().getDataBuffer()).getData();

    public static int[] colorMap = new int[height];

    //int colors[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xf442dc, 0xFF0000, 0xffe900, 0x00e1ff, 0x2600ff, 0};
    int[] colorsMic = {0xFFFFFF, 0xf442dc, 0xFF0000, 0xffe900, 0xffe900, 0x00e1ff, 0x2600ff, 0};
    //int colorsPlayer[] = {0xFFFFFF, 0xFFFFFF, 0};
    //int colorsPlayer[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,  0x008ae6, 0x001f33, 0x001f33, 0x001f33};
    //int colorsPlayer[] = {0xFFFFFF, 0xf442dc, 0xFF0000, 0xffe900, 0xffe900, 0x00e1ff, 0x2600ff, 0};
    //int colorsPlayer[] = {0xffffff, 0xffffff,0xffffff,0xff0066,0x3366cc};
    int[] colorsPlayer = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,  0x1aa3ff, 0x001f33, 0x001f33, 0x001f33};

    int[] colors;
    //int colors[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFF0000, 0xffe900, 0x00e1ff, 0x2600ff, 0};

    //int colors[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xffe900, 0xffe900, 0x00e1ff, 0x2600ff, 0};

    public static double[] frequencyBins;

    public static double[] magnitudes = new double[height];

    public static TempRecorder recorder;

    public static ImageViewer iv;

    public static String song = "Vivaldi.wav";
    public static String path = "audioFiles\\";

    public static AudioVisualizer get(){
        if (instance == null)
            new AudioVisualizer();
        return instance;
    }

    private AudioVisualizer(){
        instance = this;
        initWindow();
        calculateBins();
        if (VisualizerInit.mode.equals("player"))
            startPlaying();
        else
            startRecording();
    }

    private void startRecording(){
        recorder = new TempRecorder();

        Thread recordThread = new Thread(() -> {
            System.out.println("started recording...");
            recorder.start();
        });
        recordThread.start();
    }

    private void startPlaying(){
        TempPlayer player = new TempPlayer();
        Thread thread = new Thread(player::start);

        thread.start();
    }

    public static void drawSpectrum2(float[] samples){
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
        shiftImageLeft(1);
        for (int i = magnitudes.length - 1; i >= 0; i--){
            int x = 127 - Math.abs((int)(magnitudes[127-i] * scale));
            if (x < 0) x = 0;
            pixels[width * i + width - 1] = colorMap[x];
        }

        iv = new ImageViewer(image);
    }

    private static void shiftImageLeft(int pixelAmount){
        for (int i = 0; i < height; i++){
            for (int j = pixelAmount; j < width; j++){
                pixels[i * width + j - pixelAmount] = pixels[i * width + j];
            }
        }
    }

    private void calculateBins(){
        double maxFreq = 22050;
        double time = ((double) TempPlayer.DEF_BUFFER_SAMPLE_SZ /2)/maxFreq;
        double minFreq = 1/time;

        frequencyBins = new double[height + 2];
        frequencyBins[0] = minFreq;
        frequencyBins[frequencyBins.length-1] = maxFreq;

        minFreq = melTransform(minFreq);
        maxFreq = melTransform(maxFreq);

        double amount = (maxFreq - minFreq)/(height + 1);

        for (int i = 1; i < frequencyBins.length-1; i++){
            frequencyBins[i] = iMelTransform(minFreq + i * amount);
        }

        System.out.println(Arrays.toString(frequencyBins));
        int index = 0;
        for (int i = 1; i <= TempPlayer.DEF_BUFFER_SAMPLE_SZ/2; i++){
            double freq = i / time;
            if (freq >= frequencyBins[index]){
                frequencyBins[index++] = i-1;
            }
            if (index==(height+2)) break;
        }
        frequencyBins[frequencyBins.length-1] = (double) TempPlayer.DEF_BUFFER_SAMPLE_SZ /2;
        System.out.println(Arrays.toString(frequencyBins));
    }

    private void initWindow(){
        JPanel bp = new JPanel();
        iv = new ImageViewer(image);
        //iv.setPreserveRatio(true);
        //iv.setFitWidth(width * 2);
        //iv.setFitHeight(height * 2);
        bp.setLayout(new BorderLayout());
        bp.add(iv, BorderLayout.CENTER);

        if (VisualizerInit.mode.equals("player"))
            colors = colorsPlayer;
        else
            colors = colorsMic;

        makeColorMap();

        ImageViewer pm = new ImageViewer(sideStrip);
        //pm.setPreserveRatio(true);
        //pm.setFitWidth(10 * 2);
        //pm.setFitHeight(height * 2);
        bp.add(pm, BorderLayout.EAST);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                TempPlayer.running = false;
                TempRecorder.running = false;
            }
        });

        this.add(bp);
        if (VisualizerInit.mode.equals("player"))
            this.setTitle("Now playing: " + song);
        else
            this.setTitle("Recording...");
        this.setVisible(true);
    }

    void makeColorMap(){
        int amount = height / (colors.length-1) + 1;
        int counter = 0;

        int color1 = 0, color2 = 0;
        for (int i = 0; i < height; i++){
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

    public static int preLerp(int color1, int color2, double x){
        int r = lerp(((color1 & 0xFF0000) >> 16), ((color2 & 0xFF0000) >> 16), x);
        int g = lerp(((color1 & 0x00FF00) >> 8), ((color2 & 0x00FF00) >> 8), x);
        int b = lerp((color1 & 0x0000FF), (color2 & 0x0000FF), x);
        return (r << 16) | (g << 8) | b;
    }

    public static int lerp(int a, int b, double x){
        return (int)(a + (b - a) * x);
    }

    private double melTransform(double freq){
        return 1125 * Math.log(1 + freq/(float)700);
    }

    private double iMelTransform(double freq){
        return 700 * (Math.pow(Math.E, freq/(float)1125) - 1);
    }

}
