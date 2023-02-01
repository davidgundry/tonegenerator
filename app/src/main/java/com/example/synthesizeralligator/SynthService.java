package com.example.synthesizeralligator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import tonegenerator.MainActivity;

/**
 * An Android Service that starts an AudioGenerationThread. Commands passed to this Service
 * in an Intent are passed into the thread.
 */
public class SynthService extends Service {

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_METADATA = "metadata";
    public static final String ACTION_COMMAND = "command";

    public static final String TITLE_EXTRA = "title";
    public static final String ARTIST_EXTRA = "artist";
    public static final String BUFFER_SIZE_IN_MILLIS_EXTRA = "bufferSizeInMillis";
    public static final String STEREO_EXTRA = "stereo";
    public static final String SAMPLE_RATE_EXTRA = "sampleRate";
    public static final String COMMAND_EXTRA = "command";
    public static final String SYNTH_EXTRA = "synth";

    private static final String NOTIFICATION_CHANNEL_ID = "channel1";
    private static final String NOTIFICATION_CHANNEL_NAME = "SynthService Notification";
    private static final String MEDIA_SESSION_TAG = "SynthService";
    private static final int defaultBufferSizeInMillis = 100;
    private static final int defaultSampleRate = 32000;
    private static final boolean defaultStereo = false;
    private static final String defaultTitle = "";
    private static final String defaultArtist = "";

    private AudioGenerationThread thread;
    private MediaSessionCompat mediaSession;
    private MediaMetadataCompat.Builder metadataBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        metadataBuilder = new MediaMetadataCompat.Builder();
        mediaSession = new MediaSessionCompat(getApplicationContext(), MEDIA_SESSION_TAG);
        mediaSession.setActive(true);
        updateNotificationMetadata(defaultTitle,defaultArtist, null);
        this.createNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((intent != null) && (intent.getAction() != null))
            processIntentAction(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        if (thread != null)
            thread.end();
        mediaSession.release();
        super.onDestroy();
    }

    private void processIntentAction(Intent intent) {
        String action = intent.getAction();
        if (action == SynthService.ACTION_START) {
            startThreadFromIntent(intent);
            updateMetadataFromIntent(intent);
        }
        else if (action == SynthService.ACTION_STOP)
            stopSelf();
        else if (action == SynthService.ACTION_PAUSE) {
            if (thread != null)
                thread.pause();
        }
        else if (action == SynthService.ACTION_PLAY) {
            if (thread != null)
                thread.play();
        }
        else if (action == SynthService.ACTION_METADATA)
            updateMetadataFromIntent(intent);
        else if (action == SynthService.ACTION_COMMAND) {
            Serializable command = intent.getSerializableExtra(COMMAND_EXTRA);
            if ((thread != null) && (command != null))
                thread.command(command);
        }
    }

    private void startThreadFromIntent(Intent intent) {
        int sampleRate = intent.getIntExtra(SAMPLE_RATE_EXTRA, defaultSampleRate);
        int bufferSizeInMillis = intent.getIntExtra(BUFFER_SIZE_IN_MILLIS_EXTRA, defaultBufferSizeInMillis);
        boolean stereo = intent.getBooleanExtra(STEREO_EXTRA, defaultStereo);
        String synthClassName = intent.getStringExtra(SYNTH_EXTRA);
        if (thread != null)
            thread.end();
        Synthesizer synthesizer = createSynth(synthClassName);
        if (synthesizer != null)
            thread = this.createAudioGenerationThread(synthesizer, sampleRate, bufferSizeInMillis, stereo);
    }

    private Synthesizer createSynth(String className) {
        try {
            Object o = Class.forName(className).getConstructor().newInstance();
            return (Synthesizer) o;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            Log.d("SynthService", "Failed to create synthesizer with class name " + className + " using default constructor");
            return null;
        }
    }

    private AudioGenerationThread createAudioGenerationThread(Synthesizer synth, int sampleRate, int bufferSizeInMillis, boolean stereo) {
        AudioGenerator myAudioGenerator = new AudioGenerator(synth, sampleRate, stereo, bufferSizeInMillis);
        AudioGenerationThread thread = new AudioGenerationThread(myAudioGenerator, (int) (bufferSizeInMillis*0.5f));
        thread.start();
        return thread;
    }

    private void updateMetadataFromIntent(Intent intent) {
        String title = intent.getStringExtra(TITLE_EXTRA);
        String artist = intent.getStringExtra(ARTIST_EXTRA);
        this.updateNotificationMetadata(title != null ? title : defaultTitle, artist != null ? artist : defaultArtist, null);
    }

    private void updateNotificationMetadata(String title, String artist, Bitmap bitmap) {
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, artist);
        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void createNotification() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        MediaSessionCompat.Token sessionToken = mediaSession.getSessionToken();
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(0)
                .setSilent(true)
                .addAction(createPlayButtonAction())
                .addAction(createPauseButtonAction())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionToken))
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private NotificationCompat.Action createPauseButtonAction() {
        Intent pauseIntent = new Intent(getApplicationContext(), ActionReceiver.class);
        pauseIntent.setAction(SynthService.ACTION_PAUSE);
        PendingIntent pPauseIntent = PendingIntent.getBroadcast(getApplicationContext(),1,pauseIntent,PendingIntent.FLAG_MUTABLE);
        return new NotificationCompat.Action.Builder(R.drawable.pause_button, "Pause", pPauseIntent).build();
    }

    private NotificationCompat.Action createPlayButtonAction() {
        Intent playIntent = new Intent(getApplicationContext(), ActionReceiver.class);
        playIntent.setAction(SynthService.ACTION_PLAY);
        PendingIntent pPlayIntent = PendingIntent.getBroadcast(getApplicationContext(),1,playIntent,PendingIntent.FLAG_MUTABLE);
        return new NotificationCompat.Action.Builder(R.drawable.play_button, "Play", pPlayIntent).build();
    }
}
