package com.example.synthesizeralligator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    SynthConnection connection;

    public void startStopButton(View view) {
        Button button = (Button) findViewById(R.id.startStopButton);
        if (connection.started) {
            connection.stop();
            button.setText("Start");
        }
        else {
            Spinner sampleRateSelect = (Spinner) findViewById(R.id.sampleRateSelect);
            connection.sampleRateSelected = (int) sampleRateSelect.getSelectedItemId();
            Spinner bufferSizeSelect = (Spinner) findViewById(R.id.bufferLengthSelect);
            connection.bufferSizeSelected = (int) bufferSizeSelect.getSelectedItemId();
            Switch stereoSelect = (Switch) findViewById(R.id.stereoSelect);
            connection.stereo = stereoSelect.isChecked();
            connection.start();
            button.setText("Stop");
        }
    }

    public void newSoundButton(View view) {
        connection.command(new SynthCommand(SynthCommandType.NewSound));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = new SynthConnection(this);

        Spinner sampleRateSelect = (Spinner) findViewById(R.id.sampleRateSelect);
        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, connection.sampleRates);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleRateSelect.setAdapter(ad);
        sampleRateSelect.setSelection(2);

        Spinner bufferSizeSelect = (Spinner) findViewById(R.id.bufferLengthSelect);
        ArrayAdapter ad2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, connection.bufferSizes);
        ad2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bufferSizeSelect.setAdapter(ad2);
        bufferSizeSelect.setSelection(1);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, SynthService.class));
        super.onDestroy();
    }
}