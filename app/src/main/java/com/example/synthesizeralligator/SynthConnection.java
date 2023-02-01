package com.example.synthesizeralligator;

import android.app.Activity;
import android.content.Intent;

import java.io.Serializable;

/**
 * Provides a helper for an Activity to create and control a SynthService with a synthesizer of a
 * provided class name.
 */
public class SynthConnection<CommandType extends Serializable> {

    public final String[] sampleRates = {"8000", "16000", "32000", "36000", "44100", "48000" };
    private final int[] sampleRateNumbers = { 8000, 16000, 32000, 36000, 44100, 48000 };
    public final String[] bufferSizes = {"50ms", "100ms", "200ms", "400ms" };
    private final int[] bufferSizeNumbers = { 50, 100, 200, 400 };

    private Activity context;
    private boolean started = false;
    private boolean stereo = false;
    private int sampleRateSelected = 3;
    private int bufferSizeSelected = 1;
    private String synthClassName;

    public boolean isStereo() { return stereo;}
    public void setStereo(boolean stereo) { this.stereo = stereo;}
    public boolean isStarted() { return this.started; }
    public int getSampleRateSelected() { return sampleRateSelected;}
    public void setSampleRateSelected(int sampleRateSelected) {this.sampleRateSelected = Math.max(Math.min(sampleRateSelected, sampleRates.length-1), 0);}
    public int getBufferSizeSelected() {return bufferSizeSelected;}
    public void setBufferSizeSelected(int bufferSizeSelected) {this.bufferSizeSelected = Math.max(Math.min(bufferSizeSelected, bufferSizes.length-1), 0); }

    public SynthConnection(Activity context, String synthClassName)
    {
        this.context = context;
        this.synthClassName = synthClassName;
    }

    public void start()
    {
        Intent intent = new Intent(context, SynthService.class);
        intent.setAction(SynthService.ACTION_START);
        intent.putExtra(SynthService.SAMPLE_RATE_EXTRA, sampleRateNumbers[sampleRateSelected]);
        intent.putExtra(SynthService.BUFFER_SIZE_IN_MILLIS_EXTRA, bufferSizeNumbers[bufferSizeSelected]);
        intent.putExtra(SynthService.STEREO_EXTRA,  stereo);
        intent.putExtra(SynthService.TITLE_EXTRA,  "Synthesizer Alligator");
        intent.putExtra(SynthService.ARTIST_EXTRA,  "Main Activity");
        intent.putExtra(SynthService.SYNTH_EXTRA,  synthClassName);
        context.startForegroundService(intent);
        started = true;
    }

    public void stop()
    {
        Intent intent = new Intent(context, SynthService.class);
        intent.setAction(SynthService.ACTION_STOP);
        context.startForegroundService(intent);
        started = false;
    }

    public void command(CommandType command)
    {
        Intent intent = new Intent(context, SynthService.class);
        intent.setAction(SynthService.ACTION_COMMAND);
        intent.putExtra(SynthService.COMMAND_EXTRA, command);
        context.startForegroundService(intent);
    }
}
