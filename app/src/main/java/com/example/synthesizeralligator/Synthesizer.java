package com.example.synthesizeralligator;

/**
 * Responsible for generating sound samples
 * @param <SynthCommand>
 */
public interface Synthesizer<SynthCommand> {

    void initialise(int sampleRate, boolean stereo);

    /**
     * Sends the synthesizer a command object
     * @param command
     */
    void command(SynthCommand command);

    /**
     * Generate PCM audio sample data
     * @param startOffset the number of samples that have been generated already
     * @param samples the number of samples to generate
     * @param array the array to store output values in
     */
    void generate(long startOffset, int samples, float[] array);
}
