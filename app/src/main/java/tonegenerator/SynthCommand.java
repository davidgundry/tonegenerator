package tonegenerator;

import java.io.Serializable;

enum SynthCommandType {
    NewSound,
    SetOscillatorPitch,
    SetOscillatorAmplitude,
    SetOscillatorWaveType
}


public class SynthCommand implements Serializable {

    public SynthCommandType type;
    public float value;
    public int oscillator;

    SynthCommand(SynthCommandType type)
    {
        this.type = type;
    }

    SynthCommand(SynthCommandType type, int oscillator, float value)
    {
        this.type = type;
        this.oscillator = oscillator;
        this.value = value;
    }
}
