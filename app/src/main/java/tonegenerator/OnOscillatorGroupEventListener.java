package tonegenerator;

public interface OnOscillatorGroupEventListener {
    void onChangePitch(int oscillator, float pitch);
    void onChangeAmplitude(int oscillator, float amplitude);
    void onChangeWave(int oscillator, int id);
    void onRemoveOscillator();
    void onAddOscillator(int id);
}
