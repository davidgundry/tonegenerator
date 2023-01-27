package com.example.synthesizeralligator;

import java.io.Serializable;

enum SynthCommandType {
    NewSound
}


public class SynthCommand implements Serializable {

    public SynthCommandType type;

    SynthCommand(SynthCommandType type)
    {
        this.type = type;
    }
}
