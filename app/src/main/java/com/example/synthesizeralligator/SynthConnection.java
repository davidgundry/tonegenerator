package com.example.synthesizeralligator;

import android.app.Activity;
import android.content.Intent;

import java.io.Serializable;

public class SynthConnection {

    Activity context;

    boolean started = false;

    public final String[] sampleRates = {"8000", "16000", "32000", "36000", "44100", "48000" };
    public int sampleRateSelected = 3;
    private final int[] sampleRateNumbers = { 8000, 16000, 32000, 36000, 44100, 48000 };

    public final String[] bufferSizes = {"50ms", "100ms", "200ms", "400ms" };
    public int bufferSizeSelected = 1;
    private final int[] bufferSizeNumbers = { 50, 100, 200, 400 };

    public boolean stereo = false;

    SynthConnection(Activity context)
    {
        this.context = context;
    }

    public void start()
    {
        Intent intent = new Intent(context, SynthService.class);
        intent.setAction(SynthService.START);
        intent.putExtra("sampleRate", sampleRateNumbers[sampleRateSelected]);
        intent.putExtra("bufferSizeInMillis", bufferSizeNumbers[bufferSizeSelected]);
        intent.putExtra("stereo",  stereo);
        intent.putExtra("title",  "Synthesizer Alligator");
        intent.putExtra("artist",  "Main Activity");
        context.startForegroundService(intent);
        started = true;
    }

    public void stop()
    {
        Intent intent = new Intent(context, SynthService.class);
        intent.setAction(SynthService.STOP);
        context.startForegroundService(intent);
        started = false;
    }

    public void command(Serializable command)
    {
        Intent intent = new Intent(context, SynthService.class);
        intent.setAction(SynthService.COMMAND);
        intent.putExtra("command", command);
        context.startForegroundService(intent);
    }
}
