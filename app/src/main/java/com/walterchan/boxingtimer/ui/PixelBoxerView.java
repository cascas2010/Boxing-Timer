package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public final class PixelBoxerView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PixelBoxerView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = ArcadeTimerTheme.dp(getContext(), 170);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        drawCrowd(canvas, w, h);
        drawBoxer(canvas, w, h);
        drawRopes(canvas, w, h);
    }

    private void drawCrowd(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.FILL);
        for (int y = 0; y < h; y += 11) {
            for (int x = 0; x < w; x += 12) {
                int selector = (x * 13 + y * 7) % 5;
                paint.setColor(selector == 0 ? Color.rgb(255, 92, 76)
                        : selector == 1 ? Color.rgb(65, 185, 255) : Color.rgb(14, 62, 126));
                canvas.drawRect(x, y, x + 4, y + 4, paint);
            }
        }
    }

    private void drawBoxer(Canvas canvas, float w, float h) {
        float cx = w * .5f;
        float base = h * .84f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(244, 172, 100));
        canvas.drawCircle(cx, base - 92, 18, paint);
        canvas.drawRect(cx - 30, base - 78, cx + 30, base - 20, paint);
        paint.setColor(Color.rgb(255, 103, 77));
        canvas.drawCircle(cx - 78, base - 90, 20, paint);
        canvas.drawCircle(cx + 78, base - 90, 20, paint);
        paint.setStrokeWidth(13f);
        paint.setColor(Color.rgb(244, 172, 100));
        canvas.drawLine(cx - 25, base - 70, cx - 66, base - 88, paint);
        canvas.drawLine(cx + 25, base - 70, cx + 66, base - 88, paint);
        paint.setColor(Color.rgb(210, 237, 255));
        canvas.drawRect(cx - 36, base - 20, cx + 36, base, paint);
        paint.setColor(Color.rgb(27, 82, 169));
        for (int i = -2; i <= 2; i++) {
            canvas.drawRect(cx + i * 14 - 3, base - 20, cx + i * 14 + 3, base, paint);
        }
        paint.setColor(Color.rgb(8, 20, 44));
        canvas.drawRect(cx - 18, base - 110, cx + 18, base - 96, paint);
    }

    private void drawRopes(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        int[] colors = {Color.rgb(238, 54, 62), Color.WHITE, Color.rgb(47, 145, 255)};
        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawLine(0, h * .72f + i * 16, w, h * .72f + i * 16, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(31, 142, 243));
        canvas.drawRect(12, h * .55f, 32, h, paint);
        canvas.drawRect(w - 32, h * .55f, w - 12, h, paint);
    }
}
