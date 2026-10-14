package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public final class PixelRingFooterView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PixelRingFooterView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = ArcadeTimerTheme.dp(getContext(), 118);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(70, 169, 238));
        canvas.drawRect(0, h * .3f, w, h, paint);
        for (int y = (int) (h * .34f); y < h; y += 16) {
            for (int x = 0; x < w; x += 16) {
                paint.setColor(((x + y) / 16) % 2 == 0 ? Color.rgb(152, 218, 255) : Color.rgb(36, 126, 210));
                canvas.drawRect(x, y, x + 8, y + 8, paint);
            }
        }
        drawRopes(canvas, w, h);
        drawFighter(canvas, w * .26f, h * .72f, true);
        drawFighter(canvas, w * .74f, h * .72f, false);
        drawHitSpark(canvas, w * .5f, h * .65f);
    }

    private void drawRopes(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        int[] colors = {Color.rgb(241, 54, 64), Color.WHITE, Color.rgb(35, 129, 247)};
        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawLine(0, h * .22f + i * 13, w, h * .22f + i * 13, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(244, 76, 74));
        canvas.drawRect(2, h * .02f, 16, h * .55f, paint);
        canvas.drawRect(w - 16, h * .02f, w - 2, h * .55f, paint);
        paint.setColor(Color.rgb(35, 150, 255));
        canvas.drawRect(16, h * .02f, 23, h * .55f, paint);
        canvas.drawRect(w - 23, h * .02f, w - 16, h * .55f, paint);
    }

    private void drawFighter(Canvas canvas, float cx, float cy, boolean red) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(red ? Color.rgb(228, 76, 69) : Color.rgb(37, 112, 212));
        float direction = red ? 1f : -1f;
        canvas.drawCircle(cx, cy - 26, 10, paint);
        canvas.drawRect(cx - 8, cy - 18, cx + 8, cy + 16, paint);
        canvas.drawCircle(cx + direction * 24, cy - 12, 9, paint);
        paint.setStrokeWidth(7f);
        canvas.drawLine(cx + direction * 5, cy - 8, cx + direction * 22, cy - 12, paint);
        paint.setStrokeWidth(6f);
        canvas.drawLine(cx - 5, cy + 14, cx - direction * 15, cy + 34, paint);
        canvas.drawLine(cx + 5, cy + 14, cx + direction * 14, cy + 34, paint);
        paint.setColor(Color.rgb(255, 232, 174));
        canvas.drawCircle(cx + direction * 24, cy - 12, 4, paint);
    }

    private void drawHitSpark(Canvas canvas, float cx, float cy) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(ArcadeTimerTheme.GOLD);
        canvas.drawLine(cx - 26, cy, cx + 26, cy, paint);
        canvas.drawLine(cx, cy - 26, cx, cy + 26, paint);
        paint.setStrokeWidth(3f);
        canvas.drawLine(cx - 18, cy - 18, cx + 18, cy + 18, paint);
        canvas.drawLine(cx - 18, cy + 18, cx + 18, cy - 18, paint);
    }
}
