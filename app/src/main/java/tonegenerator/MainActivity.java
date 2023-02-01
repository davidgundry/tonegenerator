package tonegenerator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.synthesizeralligator.R;
import com.example.synthesizeralligator.SynthConnection;
import com.example.synthesizeralligator.SynthService;

public class MainActivity extends AppCompatActivity {

    SynthConnection connection;
    OscillatorGroup oscillators;
    SettingsHelper settingsHelper;

    public void newSoundButton(View view) {
        oscillators.randomise();
    }

    public void clearButton(View view) {
        oscillators.reset();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connection = new SynthConnection(this, "tonegenerator.Synth");
       // createSettingsHelper();
        createOscillatorGroup();
    }

    private void createSettingsHelper()
    {
        settingsHelper = new SettingsHelper(this, connection);
        settingsHelper.setup();
    }

    private void createOscillatorGroup()
    {
        oscillators = new OscillatorGroup(this);
        oscillators.setupOscillators();
        oscillators.eventListener = new OnOscillatorGroupEventListener() {
            @Override
            public void onChangePitch(int oscillator, float pitch) {
                connection.command(new SynthCommand(SynthCommandType.SetOscillatorPitch, oscillator, pitch));
            }

            @Override
            public void onChangeAmplitude(int oscillator, float amplitude) {
                connection.command(new SynthCommand(SynthCommandType.SetOscillatorAmplitude, oscillator, amplitude));
            }

            @Override
            public void onChangeWave(int oscillator, int id) {
                connection.command(new SynthCommand(SynthCommandType.SetOscillatorWaveType, oscillator, id));
            }

            @Override
            public void onRemoveOscillator() {
                if ((oscillators.currentOscillators) == 0 && (connection.isStarted()))
                    connection.stop();
            }

            @Override
            public void onAddOscillator(int id) {
                if (!connection.isStarted()) {
                    connection.start();
                    resendSound();
                }
            }
        };
        oscillators.reset();
    }

    private void resendSound() {
        for (int i=0;i<oscillators.getTotalOscillators();i++) {
            connection.command(new SynthCommand(SynthCommandType.SetOscillatorPitch, i, oscillators.getPitch(i)));
            connection.command(new SynthCommand(SynthCommandType.SetOscillatorAmplitude, i, oscillators.getAmplitude(i)));
            connection.command(new SynthCommand(SynthCommandType.SetOscillatorWaveType, i, oscillators.getWaveType(i)));
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, SynthService.class));
        super.onDestroy();
    }
}