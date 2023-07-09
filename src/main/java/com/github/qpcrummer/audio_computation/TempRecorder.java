package com.github.qpcrummer.audio_computation;

import javax.sound.sampled.*;

public class TempRecorder {

    public static final int BUFFER_SIZE = 1024;
    private TargetDataLine audioLine;

    public static volatile boolean running;

    AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void start(){
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException("The system does not support the specified format.");
            }

            audioLine = AudioSystem.getTargetDataLine(format);
            audioLine.open(format);
            audioLine.start();

            running = true;

            final int normalBytes = TempPlayer.normalBytesFromBits(format.getSampleSizeInBits());

            float[] samples = new float[BUFFER_SIZE * format.getChannels()];
            long[] transfer = new long[samples.length];
            byte[] bytes = new byte[samples.length * normalBytes];

            int bread = BUFFER_SIZE * format.getChannels();

            while (running) {
                audioLine.read(bytes, 0, bytes.length);
                TempPlayer.unpack(bytes, transfer, samples, bread, format);
                TempPlayer.hamming(samples, bread, format);
                AudioVisualizer.drawSpectrum2(samples);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            audioLine.flush();
            audioLine.drain();
            audioLine.close();
            System.out.println("STOPPED");
        }
    }

    public void stop() {
        running = false;
    }

}