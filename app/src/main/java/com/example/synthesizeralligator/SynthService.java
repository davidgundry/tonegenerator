package com.example.synthesizeralligator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.Serializable;

public class SynthService extends Service {

    public static final String COMMAND = "command";
    public static final String NEWSOUND = "new sound";
    public static final String PLAY = "play";
    public static final String PAUSE = "pause";
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String METADATA = "metadata";

    AudioGenerationThread thread;
    MediaSessionCompat session;
    PlaybackStateCompat.Builder builder;
    MediaMetadataCompat.Builder metadataBuilder;

    public SynthService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        metadataBuilder = new MediaMetadataCompat.Builder();
        this.startMediaSession();
        this.createNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null)
            processAction(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void processAction(Intent intent)
    {
        switch (intent.getAction())
        {
            case SynthService.START:
                if (thread != null)
                    thread.end();
                int sampleRate = intent.getIntExtra("sampleRate", 8000);
                int bufferSizeInMillis = intent.getIntExtra("bufferSizeInMillis", 100);
                boolean stereo  = intent.getBooleanExtra("stereo", false);
                thread = this.createAudioGenerationThread(new Synth(), sampleRate, bufferSizeInMillis, stereo);
                updateMetaDataFromIntent(intent);
                break;
            case SynthService.STOP:
                if (thread != null)
                    thread.end();
                stopSelf();
                break;
            case SynthService.PAUSE:
                Log.d("Action", "pause");
                if (thread != null)
                    thread.pause();
                break;
            case SynthService.PLAY:
                Log.d("Action", "play");
                if (thread != null)
                    thread.play();
                break;
            case SynthService.NEWSOUND:
                Log.d("Action", "new sound");
                if (thread != null)
                    thread.command(new SynthCommand(SynthCommandType.NewSound));
                break;
            case SynthService.COMMAND:
                Log.d("Action", "command");
                Object command = intent.getSerializableExtra("command");
                if (thread != null)
                    thread.command(command);
                break;
            case SynthService.METADATA:
                Log.d("Action", "metadata");
                updateMetaDataFromIntent(intent);
                break;
        }
    }

    private void updateMetaDataFromIntent(Intent intent)
    {
        String title = intent.getStringExtra("title");
        String artist = intent.getStringExtra("artist");
        this.updateMetadata(title != null ? title : "SynthesizerAlligator", artist != null ? artist : "Procedural Generation", null);
    }

    private void createNotification()
    {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        MediaSessionCompat.Token sessionToken = session.getSessionToken();
        Intent newSoundIntent = new Intent(getApplicationContext(), ActionReceiver.class);
        newSoundIntent.setAction(SynthService.NEWSOUND);
        PendingIntent pNewSoundIntent = PendingIntent.getBroadcast(getApplicationContext(),1,newSoundIntent,PendingIntent.FLAG_MUTABLE);

        Intent pauseIntent = new Intent(getApplicationContext(), ActionReceiver.class);
        pauseIntent.setAction(SynthService.PAUSE);
        PendingIntent pPauseIntent = PendingIntent.getBroadcast(getApplicationContext(),1,pauseIntent,PendingIntent.FLAG_MUTABLE);

        Intent playIntent = new Intent(getApplicationContext(), ActionReceiver.class);
        playIntent.setAction(SynthService.PLAY);
        PendingIntent pPlayIntent = PendingIntent.getBroadcast(getApplicationContext(),1,playIntent,PendingIntent.FLAG_MUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "ChannelID1")
                //.setContentTitle("Synthesizer")
                //.setContentText("Making sweet grooves")
                //.setTicker("Making sweet grooves")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(0)
                .setSilent(true)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.new_sound_button, "New Sound", pNewSoundIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.pause_button, "Pause", pPauseIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.play_button, "Play", pPlayIntent).build())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(sessionToken))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void startMediaSession()
    {
        session = new MediaSessionCompat(getApplicationContext(), "SynthService");
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                Log.d("SynthService", "Play");
                super.onPlay();
            }

            @Override
            public void onPause() {
                Log.d("SynthService", "Pause");
                super.onPause();
            }

            @Override
            public void onStop() {
                Log.d("SynthService", "Stop");
                super.onStop();
            }
        });
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

        builder = new PlaybackStateCompat.Builder();
        updatePlaybackState();

        session.setActive(true);
        updateMetadata("SynthesizerAlligator","Procedural Generation", null);
    }


    private void updatePlaybackState()
    {
        builder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        builder.setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        session.setPlaybackState(builder.build());
    }

    private void updateMetadata(String title, String artist, Bitmap bitmap) {
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, artist);
        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
        session.setMetadata(metadataBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("ChannelID1", "SynthService Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        thread.end();
        session.release();
        super.onDestroy();
    }

    private AudioGenerationThread createAudioGenerationThread(Synthesizer synth, int sampleRate, int bufferSizeInMillis, boolean stereo)
    {
        AudioGenerator myAudioGenerator = new AudioGenerator(synth, sampleRate, stereo, bufferSizeInMillis);
        AudioGenerationThread thread = new AudioGenerationThread(myAudioGenerator, (int) (bufferSizeInMillis*0.5f));
        thread.start();
        return thread;
    }
}