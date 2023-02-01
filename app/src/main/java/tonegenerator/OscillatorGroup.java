package tonegenerator;

import android.app.Activity;
import android.view.View;
import com.example.synthesizeralligator.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class OscillatorGroup {

    private static float deleteRadius = 100;

    OnOscillatorGroupEventListener eventListener;

    private Activity activity;
    private OscillatorCircle[] oscillators;
    int currentOscillators = 0;

    private FloatingActionButton addButton;
    private FloatingActionButton delButton;

    public OnOscillatorGroupEventListener getEventListener() { return eventListener; }
    public void setEventListener(OnOscillatorGroupEventListener eventListener) { this.eventListener = eventListener;}

    public int getTotalOscillators() { return this.oscillators.length; }

    OscillatorGroup(Activity activity)
    {
        this.activity = activity;
    }

    void setupOscillators()
    {
        addButton = activity.findViewById(R.id.addButton);
        delButton = activity.findViewById(R.id.delButton);
        addButton.setOnClickListener((View v) -> add());

        OscillatorCircle o0 = (OscillatorCircle) activity.findViewById(R.id.oscCircle0);
        OscillatorCircle o1 = (OscillatorCircle) activity.findViewById(R.id.oscCircle1);
        OscillatorCircle o2 = (OscillatorCircle) activity.findViewById(R.id.oscCircle2);
        OscillatorCircle o3 = (OscillatorCircle) activity.findViewById(R.id.oscCircle3);
        oscillators = new OscillatorCircle[]{o0, o1, o2, o3};
        for (int i=0;i<oscillators.length;i++)
            setEventsOnOscillatorCircle(oscillators[i], i);
    }

    void randomise()
    {
        int oldCurrentOscillators = currentOscillators;
        currentOscillators = (int) (Math.random()*(oscillators.length)) + 1;
        for (int i=0;i<oscillators.length;i++) {
            boolean visible = i < currentOscillators;
            setOscillatorVisibility(i);
            setPitch(i, (float) Math.random());
            setAmplitude(i, visible ? (float) Math.random() : 0);
            setWaveType(i, (int) (Math.random() * 4));
        }
        updateAddButtonVisibility();
        if (oldCurrentOscillators > currentOscillators) {
            if (eventListener != null)
                eventListener.onRemoveOscillator();
        }
        else if (oldCurrentOscillators < currentOscillators) {
            if (eventListener != null)
                eventListener.onAddOscillator(currentOscillators-1);
        }
    }

    void reset()
    {
        boolean sendEmptyEvent = (currentOscillators > 0);
        currentOscillators = 0;
        for (int i=0;i<oscillators.length;i++) {
            setOscillatorVisibility(i);
            setPitch(i, 0.5f);
            setAmplitude(i, 0);
            setWaveType(i, 0);
        }
        if ((sendEmptyEvent) && (eventListener != null))
            eventListener.onRemoveOscillator();
        updateAddButtonVisibility();
    }

    private void add()
    {
        if (this.currentOscillators < oscillators.length) {
            this.currentOscillators++;
            setOscillatorVisibility(currentOscillators - 1);
            setAmplitude(currentOscillators - 1, 0.5f);
            setPitch(currentOscillators - 1, 0.5f);
            setWaveType(currentOscillators - 1, 0);
            if (eventListener != null)
                eventListener.onAddOscillator(currentOscillators-1);
        }
    }

    private void hideInnerOscillator()
    {
        if (this.currentOscillators > 0) {
            this.currentOscillators--;
            setOscillatorVisibility(currentOscillators);
            setAmplitude(currentOscillators, 0);
            setPitch(currentOscillators, 0.5f);
            if (eventListener != null)
                eventListener.onRemoveOscillator();
        }
        updateAddButtonVisibility();
        delButton.setVisibility(View.INVISIBLE);
    }

    private void updateAddButtonVisibility()
    {
        int visibility = (currentOscillators < oscillators.length) ? View.VISIBLE : View.INVISIBLE;
        addButton.setVisibility(visibility);
    }

    private void setOscillatorVisibility(int id) {
        oscillators[id].setHidden(id >= currentOscillators);
    }

    float angleToPitch(float angle) { return (angle+180)/360; }
    float pitchToAngle(float pitch) { return (pitch*360)-180; }
    float widthToAmplitude(float width, float maxWidth) { return (width-1)/(maxWidth-1); }
    float amplitudeToWidth(float amplitude, float maxWidth) { return (amplitude*(maxWidth-1))+1; }

    private void setPitch(int oscillator, float value) {
        oscillators[oscillator].setAngle(pitchToAngle(value));
        if (eventListener != null)
            eventListener.onChangePitch(oscillator, value);
    }

    float getPitch(int oscillator) { return angleToPitch(oscillators[oscillator].getAngle()); }

    void setAmplitude(int oscillator, float value) {
        oscillators[oscillator].setStrokeWidth(amplitudeToWidth(value, oscillators[oscillator].getMaxWidth()));
        if (eventListener != null)
            eventListener.onChangeAmplitude(oscillator, value);
    }

    float getAmplitude(int oscillator)
    {
        if (oscillator < currentOscillators)
            return widthToAmplitude(oscillators[oscillator].getWidth(),oscillators[oscillator].getMaxWidth());
        return 0;
    }

    void setWaveType(int oscillator, int value) {
        oscillators[oscillator].setCurrentColor(value);
        if (eventListener != null)
            eventListener.onChangeWave(oscillator, value);
    }

    int getWaveType(int oscillator) {
        return oscillators[oscillator].getCurrentColor();
    }

    private void setEventsOnOscillatorCircle(OscillatorCircle o, int id)
    {
        o.setEventListener(new OnOscillatorCircleEventListener() {
            @Override
            public void onChangeAngle(double angle) {
                if (eventListener != null)
                    eventListener.onChangePitch(id, angleToPitch((float) angle));
            }

            @Override
            public void onChangeWidth(float width) {
                if (eventListener != null)
                    eventListener.onChangeAmplitude(id, widthToAmplitude(width, o.getMaxWidth()));
            }

            @Override
            public void onChangeColor(int color) {
                if (eventListener != null)
                    eventListener.onChangeWave(id, color);
            }

            @Override
            public void onDrag() {
                addButton.setVisibility(View.INVISIBLE);
                if (id == currentOscillators-1)
                    delButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRelease(float radius) {
                updateAddButtonVisibility();
                delButton.setVisibility(View.INVISIBLE);
                if (id == currentOscillators-1)
                    if (radius < deleteRadius)
                        hideInnerOscillator();
            }
        });
    }
}
