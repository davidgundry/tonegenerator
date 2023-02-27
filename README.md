# Tone Generator 

An Android demo app that allows the user to design a synthesized tone by controlling the pitch and waveforms of four oscillators. Each oscilator's base pitch is defined by the [harmonic series](https://en.wikipedia.org/wiki/Harmonic_series_(music)).

Four waveforms can be used: sine, square, saw, and triangle.

## As an Android Service Demo

As a demo of how Android services can be used, look at the `SynthConnection` and `SynthService` classes for an example of how to perform inter-service communication.

* `SynthConnection` handles creating and sending messages to the service.
* `SynthService` handles creates a thread for audio generation, handles unpacking messages from Intents to pass into the thread, and creates the notification required for a foreground service to run.
* Additionally `ActionReciever` is a BroadcastReciever. It recieves broadcast messages from the notification controls and passes them on to the service.

## As an Audio Generation on Android Demo

As a demo of how to generate audio on Android, look at the `AudioGenerator` class. This does not create the samples itself, but it handles creating and supplying data to an `AudioTrack`.

* It periodically requests a window of samples from a `Synthesizer` (window size depends on how much time has passed, and how large a buffer to maintain)
* It converts these samples to PCM to supply to the `AudioTrack` as this seems to be more efficient than supplying the samples as floats.

## Development

The launches a foreground service so it can continue to generate audio when the user navigates to a different app. This service generates and outputs procedural audio. It is run in a separate thread.

The code is designed to be reusable. To make a new synthesizer app you need to
* Replace the `MainActivity` class to supply the desired front-end controls
* Replace the `Synthesizer` class to respond appropriately to the `generate` command. 


A front end (such as the Tone Generator UI demoed here) creates a `SynthConnnection`, supplying the classname of the synthesizer to create. A `SynthConnection` provides the methods `start()`, `stop()` and `command()`. This last method supplies a command object to the synthesizer The commands can be any class that extends `Serializable`.

The synthesizer must extend a generic `Synthesizer<SynthCommand extends Serializable>` abstract class, implementing three methods:

    public abstract void initialise(int sampleRate, boolean stereo)
    public abstract void command(SynthCommand command)
    public abstract void generate(long startOffset, int samples, float[] array)

