package com.example.synthesizeralligator;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A thread that regularly tells an AudioGenerator to generate sound. Handles thread safe communication for the AudioGenerator.
 * @param <CommandType> The type of command that the AudioGenerator expects.
 */
public class AudioGenerationThread<CommandType> extends Thread {

    private AudioGenerator audioGenerator;
    private boolean playing = true;
    private boolean shouldPause = false;
    private boolean shouldPlay = false;
    private boolean shouldEnd = false;
    private final int sleepTime;
    private long startTime;

    private ConcurrentLinkedQueue<CommandType> commands = new ConcurrentLinkedQueue<CommandType>();

    public AudioGenerationThread(AudioGenerator audioGenerator, int sleepTimeInMillis)
    {
        this.audioGenerator = audioGenerator;
        this.sleepTime = sleepTimeInMillis;
    }

    public void run() {
        audioGenerator.initialise();
        audioGenerator.play();
        startTime = System.currentTimeMillis();
        while(!shouldEnd)
        {
            updateState();
            sendCommands();
            if (playing) {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                audioGenerator.generate(elapsedMillis);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        audioGenerator.end();
    }

    public void end() {
        shouldEnd = true;
    }

    public void pause() {
        shouldPause = true;
    }

    public void play() {
        shouldPlay = true;
    }

    public void command(CommandType c) {
        commands.add(c);
    }

    private void updateState() {
        if (shouldPause) {
            audioGenerator.pause();
            shouldPause = false;
            playing = false;
        }
        if (shouldPlay) {
            audioGenerator.play();
            shouldPlay = false;
            playing = true;
        }
    }

    private void sendCommands()
    {
        CommandType c = commands.poll();
        while (c != null)
        {
            audioGenerator.command(c);
            c = commands.poll();
        }
    }
}
