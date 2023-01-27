package com.example.synthesizeralligator;

enum OscillatorType
{
    Sine,
    Square,
    Saw,
    Triangle,
}

public class Synth implements Synthesizer<SynthCommand>{

    private int sampleRate;
    private int effectiveSampleRate;
    private boolean stereo;

    private final int oscillators = 10;
    private final double[] oscFreq = new double[oscillators];
    private final double[] oscAmp = new double[oscillators];
    private final double[] oscOffset = new double[oscillators];
    private final OscillatorType[] oscType = new OscillatorType[oscillators];
    private double totalAmp;

    private double freqOfTone = 440; // hz

    public void initialise(int sampleRate, boolean stereo) {
        this.sampleRate = sampleRate;
        this.effectiveSampleRate = stereo ? sampleRate * 2 : sampleRate;
        this.newSound();
        this.stereo = stereo;
    }

    public void command(SynthCommand c)
    {
        if (c.type == SynthCommandType.NewSound)
            newSound();
    }

    public void generate(long startOffset, int samples, float[] array) {
        for (long i = startOffset; i < startOffset+samples; i++)
        {
            float twoPiIOverSampleRate = (2 * (float) Math.PI * i)/effectiveSampleRate;
            float iOverSampleRate = (float) i/effectiveSampleRate;
            float normalised = 0;
            for (int o=0;o<oscillators;o++)
            {
                if (oscType[o] == OscillatorType.Sine)
                {
                    final double timeForSine = oscOffset[o] + twoPiIOverSampleRate;
                    final double x = timeForSine * oscFreq[o];
                    normalised += Math.sin(x) * oscAmp[o];
                }
                else if (oscType[o] == OscillatorType.Square)
                {
                    final double timeForSine = oscOffset[o] + twoPiIOverSampleRate;
                    final double x = timeForSine * oscFreq[o];
                    normalised += Math.sin(x) > 0 ? oscAmp[o] : -oscAmp[o];
                }
                else if (oscType[o] == OscillatorType.Saw)
                {
                    final double time = iOverSampleRate + oscOffset[o];
                    final double x = time * oscFreq[o];
                    final double f = 2 * (x % 1) - 1;
                    normalised += f * oscAmp[o];
                }
                else if (oscType[o] == OscillatorType.Triangle) // Seems to be half the frequency
                {
                    final double time = iOverSampleRate + oscOffset[o];
                    final double x = time * oscFreq[o];
                    final double f = 1 - 2 * Math.abs(1 - ( 2* ((x/2 + 1/4) % 1)));
                    normalised += f * oscAmp[o];
                }
            }

            normalised /= totalAmp;
            array[(int) (i % array.length)] = normalised;
            if (this.stereo)
            {
                i++;
                array[(int) (i % array.length)] = normalised;
            }
        }
    }

    private void newSound() {
        totalAmp = 0;
        for (int i=0;i<oscillators;i++)
        {
            oscFreq[i] = i == 0 ? freqOfTone : (freqOfTone * (i+1) * (0.9 + Math.random()/5));
            oscOffset[i] = Math.random();
            oscAmp[i] = Math.random();
            int type = (int) (Math.random() * 4);
            if (type == 0)
                oscType[i] = OscillatorType.Sine;
            else if (type == 1)
                oscType[i] = OscillatorType.Square;
            else if (type == 2)
                oscType[i] = OscillatorType.Saw;
            else if (type == 3)
                oscType[i] = OscillatorType.Triangle;
            totalAmp += oscAmp[i];
        }
    }

}
