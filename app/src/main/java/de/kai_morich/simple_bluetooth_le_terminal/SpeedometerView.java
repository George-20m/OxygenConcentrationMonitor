package de.kai_morich.simple_bluetooth_le_terminal;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
public class SpeedometerView extends View {
    private Paint trackPaint, needlePaint, centerPaint;
    private float value = 0f;
    private RectF arcRect = new RectF();
    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(18f);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(Color.WHITE);
        needlePaint.setStrokeWidth(6f);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.WHITE);
    }
    public void setValue(float val) {
        this.value = Math.max(0, Math.min(100, val));
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h * 0.85f;
        float radius = Math.min(w, h * 2f) * 0.42f;
        float left = cx - radius;
        float top = cy - radius;
        arcRect.set(left, top, left + radius * 2, top + radius * 2);
        trackPaint.setColor(0x44FFFFFF);
        canvas.drawArc(arcRect, 180, 180, false, trackPaint);
        float sweep = (value / 100f) * 180f;
        int color;
        if (value < 80) color = 0xFFFF3333;
        else if (value < 90) color = 0xFFFFAA00;
        else color = 0xFF44DD88;
        trackPaint.setColor(color);
        canvas.drawArc(arcRect, 180, sweep, false, trackPaint);
        double angle = Math.toRadians(180 + sweep);
        float nx = cx + (float)(radius * 0.78 * Math.cos(angle));
        float ny = cy + (float)(radius * 0.78 * Math.sin(angle));
        canvas.drawLine(cx, cy, nx, ny, needlePaint);
        canvas.drawCircle(cx, cy, 10f, centerPaint);
    }
}
