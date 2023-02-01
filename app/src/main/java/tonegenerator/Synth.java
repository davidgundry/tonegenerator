package tonegenerator;

import com.example.synthesizeralligator.Synthesizer;

enum OscillatorType
{
    Sine,
    Square,
    Saw,
    Triangle,
}

public class Synth extends Synthesizer<SynthCommand> {

    private final int oscillators = 4;
    private final double[] oscFreq = new double[oscillators];
    private final double[] oscAmp = new double[oscillators];
    private final double[] oscOffset = new double[oscillators];
    private final OscillatorType[] oscType = new OscillatorType[oscillators];

    private int effectiveSampleRate;
    private boolean stereo;
    private double totalAmplitude;
    private double freqOfTone = 440; // hz

    public void initialise(int sampleRate, boolean stereo) {
        this.effectiveSampleRate = stereo ? sampleRate * 2 : sampleRate;
        this.resetSound();
        this.stereo = stereo;
    }

    public void command(SynthCommand c)
    {
        if (c.type == SynthCommandType.NewSound)
            resetSound();
        else if (c.type == SynthCommandType.SetOscillatorWaveType)
            this.oscType[c.oscillator] = OscillatorType.values()[(int) c.value];
        else if (c.type == SynthCommandType.SetOscillatorPitch)
            this.oscFreq[c.oscillator] = c.value*(c.oscillator+1);
        else if (c.type == SynthCommandType.SetOscillatorAmplitude) {
            this.oscAmp[c.oscillator] = c.value;
            calculateTotalAmplitude();
        }
    }

    public void generate(long startOffset, int samples, float[] array) {
        for (long i = startOffset; i < startOffset+samples; i++)
        {
            final float twoPiIOverSampleRate = (2 * (float) Math.PI * i)/effectiveSampleRate;
            final float iOverSampleRate = (float) i/effectiveSampleRate;
            float normalised = 0;
            for (int o=0;o<oscillators;o++)
            {
                if (oscType[o] == OscillatorType.Sine) {
                    final double timeForSine = oscOffset[o] + twoPiIOverSampleRate;
                    final double x = timeForSine * freqOfTone * oscFreq[o];
                    normalised += Math.sin(x) * oscAmp[o];
                }
                else if (oscType[o] == OscillatorType.Square) {
                    final double timeForSine = oscOffset[o] + twoPiIOverSampleRate;
                    final double x = timeForSine * freqOfTone * oscFreq[o];
                    normalised += Math.sin(x) > 0 ? oscAmp[o] : -oscAmp[o];
                }
                else if (oscType[o] == OscillatorType.Saw) {
                    final double time = iOverSampleRate + oscOffset[o];
                    final double x = time * freqOfTone * oscFreq[o];
                    final double f = 2 * (x % 1) - 1;
                    normalised += f * oscAmp[o];
                }
                else if (oscType[o] == OscillatorType.Triangle) {
                    final double time = iOverSampleRate + oscOffset[o];
                    final double x = time * freqOfTone * oscFreq[o];
                    final double f = 1 - 2 * Math.abs(1 - ( 2* ((x + 0.25f) % 1)));
                    normalised += f * oscAmp[o];
                }
            }

            normalised /= totalAmplitude;
            array[(int) (i % array.length)] = normalised;
            if (this.stereo) {
                i++;
                array[(int) (i % array.length)] = normalised;
            }
        }
    }

    private void resetSound() {
        totalAmplitude = 0;
        for (int i=0;i<oscillators;i++)
        {
            oscFreq[i] = 0;
            oscOffset[i] = 0;
            oscAmp[i] = 0;
            oscType[i] = OscillatorType.Sine;
        }
    }

    private void calculateTotalAmplitude() {
        totalAmplitude = 0;
        for (int i=0;i<oscillators;i++)
            totalAmplitude += oscAmp[i];
    }
}
