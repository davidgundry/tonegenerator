package tonegenerator;

public interface OnOscillatorCircleEventListener {
    void onChangeAngle(double angle);
    void onChangeWidth(float width);
    void onChangeColor(int id);
    void onDrag();
    void onRelease(float radius);
}