package tonegenerator;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;

import com.example.synthesizeralligator.R;
import com.example.synthesizeralligator.SynthConnection;

public class SettingsHelper {

    private Activity activity;
    private SynthConnection connection;

    SettingsHelper(Activity activity, SynthConnection connection)
    {
        this.activity = activity;
        this.connection = connection;
    }

    void setup()
    {
        Spinner sampleRateSelect = (Spinner) activity.findViewById(R.id.sampleRateSelect);
        ArrayAdapter ad = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, connection.sampleRates);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleRateSelect.setAdapter(ad);
        sampleRateSelect.setSelection(connection.getSampleRateSelected());

        Spinner bufferSizeSelect = (Spinner) activity.findViewById(R.id.bufferLengthSelect);
        ArrayAdapter ad2 = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, connection.bufferSizes);
        ad2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bufferSizeSelect.setAdapter(ad2);
        bufferSizeSelect.setSelection(connection.getBufferSizeSelected());

        Button startStopButton = activity.findViewById(R.id.startStopButton);
        startStopButton.setOnClickListener((View view) -> {
            if (connection.isStarted())
                stopSynth();
            else
                startSynth();
        });
    }

    private void startSynth()
    {
        Button button = (Button) activity.findViewById(R.id.startStopButton);
        Spinner sampleRateSelect = (Spinner) activity.findViewById(R.id.sampleRateSelect);
        Spinner bufferSizeSelect = (Spinner) activity.findViewById(R.id.bufferLengthSelect);
        SwitchCompat stereoSelect = (SwitchCompat) activity.findViewById(R.id.stereoSelect);
        connection.setSampleRateSelected((int) sampleRateSelect.getSelectedItemId());
        connection.setBufferSizeSelected((int) bufferSizeSelect.getSelectedItemId());
        connection.setStereo(stereoSelect.isChecked());
        connection.start();
        button.setText("Stop");
    }

    private void stopSynth()
    {
        Button button = (Button) activity.findViewById(R.id.startStopButton);
        connection.stop();
        button.setText("Start");
    }
}
