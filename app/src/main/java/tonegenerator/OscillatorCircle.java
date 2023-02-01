package tonegenerator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.synthesizeralligator.R;

enum Direction
{
    UNSET,
    SET,
}

public class OscillatorCircle extends View {

    OnOscillatorCircleEventListener eventListener;

    public void setEventListener(OnOscillatorCircleEventListener eventListener) {
        this.eventListener = eventListener;
    }

    private int[] colors = { Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE };
    private int currentColor = 0;
    private float radius;
    private float angle = 0;
    private float startPressTime;
    private float strokeWidth;
    private float maxWidth;
    private float gapAngle = 20;
    private final Paint paint = new Paint();

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        invalidate();
    }

    private boolean hidden = false;

    private Direction direction = Direction.UNSET;

    public OscillatorCircle(Context context) {
        this(context, null, 0);
    }

    public OscillatorCircle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OscillatorCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OscillatorCircle, defStyle, 0);
            strokeWidth = a.getFloat(R.styleable.OscillatorCircle_lineWidth, 1);
            maxWidth = a.getFloat(R.styleable.OscillatorCircle_maxLineWidth, 10);
            radius = a.getFloat(R.styleable.OscillatorCircle_radius, 100);
        }
        else {
            strokeWidth = 1;
            maxWidth = 10;
        }

        paint.setColor(colors[currentColor]);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.STROKE);
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int val) {
        currentColor = val;
        paint.setColor(colors[currentColor]);
        invalidate();
    }

    public float getRadius() {
        return radius;
    }

    public float getAngle() {
        return angle;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        paint.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (hidden)
            return false;
        if(event.getActionMasked() == MotionEvent.ACTION_MOVE){
            if (eventListener != null)
                eventListener.onDrag();

            if (event.getHistorySize() >= 1) {
                final float originX = getX() + getWidth() / 2;
                final float originY = getY() + getHeight() / 2;

                final float oldX = event.getHistoricalX(event.getHistorySize() - 1) - originX;
                final float oldY = event.getHistoricalY(event.getHistorySize() - 1) - originY;
                final double oldTheta = Math.atan(oldY/oldX);// + oldX < 0 ? Math.PI: 0;
                final double oldRadius = Math.sqrt(oldX*oldX + oldY*oldY);

                final float newX = event.getX() - originX;
                final float newY = event.getY() - originY;
                final double newTheta = Math.atan(newY/newX);// + newX < 0 ? Math.PI: 0;
                final double newRadius = Math.sqrt(newX*newX + newY*newY);

                final double deltaTheta = Math.max(Math.min(newTheta - oldTheta, Math.PI/16), -Math.PI/16);
                final double deltaRadius = newRadius - oldRadius;

                final double arcLength = oldRadius * deltaTheta;

                if ((Math.abs(arcLength) > 0.1) || Math.abs(deltaRadius) > 0.1) // TODO: compare against first position instead
                    direction = Direction.SET;

                angle += 10 * (float) (deltaTheta * (180/Math.PI));
                if (angle > 180-gapAngle/2)
                    angle = 180-gapAngle/2;
                if (angle < -180+gapAngle/2)
                    angle = -180+gapAngle/2;
                invalidate();
                if (eventListener != null)
                    eventListener.onChangeAngle(angle);

                strokeWidth += deltaRadius;
                strokeWidth = Math.min(Math.max(strokeWidth, 1),maxWidth);
                paint.setStrokeWidth(strokeWidth);
                if (eventListener != null)
                    eventListener.onChangeWidth(strokeWidth);
                invalidate();
            }
            return true;
        }
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
            final float originX = getX() + getWidth() / 2;
            final float originY = getY() + getHeight() / 2;
            final float x = event.getX();
            final float y = event.getY();
            final double dist = Math.sqrt((x-originX)*(x-originX) + (y-originY)*(y-originY));
            if ((dist < radius+Math.max(strokeWidth,10)+10) && (dist > radius-Math.max(strokeWidth,10)-10)) {
                startPressTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (direction == Direction.UNSET) {
                currentColor = (currentColor + 1) % colors.length;
                paint.setColor(colors[currentColor]);
                if (eventListener != null)
                    eventListener.onChangeColor(currentColor);
                invalidate();
            }
            final float originX = getX() + getWidth() / 2;
            final float originY = getY() + getHeight() / 2;
            final float x = event.getX() - originX;
            final float y = event.getY() - originY;
            final double radius = Math.sqrt(x * x + y * y);
            eventListener.onRelease((float) radius);

            direction = Direction.UNSET;
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.hidden) {
            float width = this.getRight() - this.getLeft();
            float height = this.getBottom() - this.getTop();
            float left = (this.getLeft() + width / 2) - radius;
            float top = (this.getTop() + height / 2) - radius;
            canvas.drawArc(left, top, left + radius * 2, top + radius * 2, -90 + (gapAngle / 2) + angle, 360 - gapAngle, false, paint);
        }
    }
}
