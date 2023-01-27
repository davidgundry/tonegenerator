package com.example.synthesizeralligator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent synthIntent = new Intent(context, SynthService.class);
        synthIntent.setAction(intent.getAction());
        context.startService(synthIntent);
    }
}