package com.example.synthesizeralligator;

import java.io.Serializable;

/**
 * Responsible for generating sound samples
 * @param <SynthCommand>
 */
public abstract class Synthesizer<SynthCommand extends Serializable> {

    /**
     *
     */
    public Synthesizer() {}

    public abstract void initialise(int sampleRate, boolean stereo);

    /**
     * Sends the synthesizer a command object
     * @param command
     */
    public abstract void command(SynthCommand command);

    /**
     * Generate PCM audio sample data
     * @param startOffset the number of samples that have been generated already
     * @param samples the number of samples to generate
     * @param array the array to store output values in
     */
    public abstract void generate(long startOffset, int samples, float[] array);
}
