package com.example.synthesizeralligator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.Serializable;

enum AudioGeneratorState
{
    Created,
    Initialised,
    Ended
}

/**
 * Handles requesting new samples from a Synthesizer and playing them.
 * @param <CommandType> The type of command that the Synthesizer expects
 */
public class AudioGenerator<CommandType extends Serializable> {

    public final int sampleRate;
    public final boolean stereo;
    private final int samplesPerMilli;
    private final int numSamples;
    private final int bufferSize;

    private final byte pcmData[];
    private final float sampleData[];
    private final Synthesizer<CommandType> synth;

    private AudioTrack audioTrack;
    private int samplesGenerated = 0;
    private AudioGeneratorState state;

    public AudioGenerator(Synthesizer synth, int sampleRate, boolean stereo, int bufferSizeInMillis) {
        this.synth = synth;
        this.sampleRate = sampleRate;
        this.stereo = stereo;
        if (stereo) {
            bufferSize = (int) (sampleRate * bufferSizeInMillis/1000f * 2);
            numSamples = bufferSize;
            samplesPerMilli = 2*sampleRate/1000;
        }
        else {
            bufferSize = (int) (sampleRate * bufferSizeInMillis/1000f);
            numSamples = bufferSize;
            samplesPerMilli = sampleRate/1000;
        }
        pcmData = new byte[2 * numSamples];
        sampleData = new float[numSamples];
        state = AudioGeneratorState.Created;
    }

    public void initialise() {
        if (state != AudioGeneratorState.Created)
                throw new IllegalStateException("Can only initialise a newly created AudioGenerator");
        synth.initialise(sampleRate, stereo);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                stereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO ,
                AudioFormat.ENCODING_PCM_16BIT,
                pcmData.length,
                AudioTrack.MODE_STREAM);
        makeNewAudio(bufferSize);
        state = AudioGeneratorState.Initialised;
    }

    public void generate(long millisSinceStart) {
        if (state != AudioGeneratorState.Initialised)
            throw new IllegalStateException("Can only call generate on an initialised but not ended AudioGenerator ");
        long elapsedSamples = millisSinceStart * samplesPerMilli;
        int buffer = (int) (samplesGenerated - elapsedSamples);
        if (buffer < 0)
            bufferUnderrun(-buffer);
        if (buffer < bufferSize)
            makeNewAudio(bufferSize - buffer);
    }

    public void play() {
        if (audioTrack != null)
            audioTrack.play();
    }

    public void pause() {
        if (audioTrack != null)
            audioTrack.pause();
    }

    public void end() {
        if (state != AudioGeneratorState.Initialised)
            throw new IllegalStateException("Can only call end on an initialised AudioGenerator");
        audioTrack.stop();
        audioTrack.release();
        state = AudioGeneratorState.Ended;
    }

    public void command(CommandType c) {
        synth.command(c);
    }

    private void makeNewAudio(int newToGen)
    {
        if (stereo)
            newToGen *= 2;
        synth.generate(samplesGenerated, newToGen, sampleData);
        convertToPCM(samplesGenerated, newToGen);
        playSound(samplesGenerated, newToGen, audioTrack);
        this.samplesGenerated += newToGen;
    }

    private void convertToPCM(long startOffset, int newSamples) {
        int pcmIndex = (int) ((startOffset*2) % pcmData.length);
        int startIndex = (int) (startOffset % sampleData.length);

        for (int i = startIndex ; i < startIndex + newSamples; i++) {
            final short val = (short) ((sampleData[i % sampleData.length] * 32767));
            pcmData[pcmIndex] = (byte) (val & 0x00ff);
            pcmIndex++;
            pcmData[pcmIndex] = (byte) ((val & 0xff00) >>> 8);
            pcmIndex++;
            pcmIndex = pcmIndex % pcmData.length;
        }
    }

    private void playSound(long startOffset, int samples, AudioTrack audioTrack) {
        int sizeInBytes = samples*2;
        long offsetInBytes = startOffset*2;
        int startInArray = (int) (offsetInBytes % pcmData.length);
        if (startInArray + sizeInBytes > pcmData.length) {
            int bytesThatFit = pcmData.length - startInArray;
            audioTrack.write(pcmData, startInArray, bytesThatFit);
            audioTrack.write(pcmData, 0, sizeInBytes - bytesThatFit);
        }
        else
            audioTrack.write(pcmData, startInArray, sizeInBytes);
    }

    private void bufferUnderrun(int samples) {
        Log.d("SynthThread", "underrun of " + samples);
    }


    // Testing indicates this is less efficient than converting ourselves and writing bytes to AudioTrack
    /*private void playSoundFloats(long startOffset, int samples, AudioTrack audioTrack) {
        int startInArray = (int) (startOffset % sampleData.length);
        if (startInArray + samples > sampleData.length) {
            int samplesThatFit = sampleData.length - startInArray;
            audioTrack.write(sampleData, startInArray, samplesThatFit, AudioTrack.WRITE_BLOCKING);
            audioTrack.write(sampleData, 0, Math.min(samples - samplesThatFit, sampleData.length), AudioTrack.WRITE_BLOCKING);
        } else
            audioTrack.write(sampleData, startInArray, samples, AudioTrack.WRITE_BLOCKING);
    }*/
}
