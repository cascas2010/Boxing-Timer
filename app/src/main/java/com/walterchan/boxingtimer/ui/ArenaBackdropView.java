package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public final class ArenaBackdropView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public ArenaBackdropView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth(), height = getHeight();
        canvas.drawColor(Color.rgb(2, 9, 31));
        drawSpotlight(canvas, width * .18f, 0f, width * .38f, height * .72f);
        drawSpotlight(canvas, width * .82f, 0f, width * .62f, height * .72f);
        drawLights(canvas, width);
        drawCrowd(canvas, width, height);
        drawRing(canvas, width, height);
    }

    private void drawLights(Canvas canvas, float width) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(224, 249, 255));
        for (float x : new float[]{width * .18f, width * .5f, width * .82f}) {
            canvas.drawRect(x - 9, 2, x + 9, 8, paint);
            canvas.drawRect(x - 4, -4, x + 4, 16, paint);
        }
    }

    private void drawCrowd(Canvas canvas, float width, float height) {
        paint.setStyle(Paint.Style.FILL);
        for (int y = (int) (height * .47f); y < height - 38; y += 10) {
            for (int x = 5; x < width; x += 11) {
                int selector = (x * 7 + y * 11) % 5;
                paint.setColor(selector == 0 ? Color.rgb(255, 89, 73)
                        : selector == 1 ? Color.rgb(58, 184, 255) : Color.rgb(21, 66, 129));
                canvas.drawRect(x, y, x + 4, y + 4, paint);
            }
        }
    }

    private void drawRing(Canvas canvas, float width, float height) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        int[] ropeColors = {Color.rgb(235, 56, 59), Color.WHITE, Color.rgb(31, 137, 255)};
        for (int i = 0; i < ropeColors.length; i++) {
            paint.setColor(ropeColors[i]);
            float y = height - 38 + i * 13;
            canvas.drawLine(0, y, width, y, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(8, 72, 158));
        canvas.drawRect(5, height - 58, 17, height, paint);
        canvas.drawRect(width - 17, height - 58, width - 5, height, paint);
        paint.setColor(Color.rgb(46, 185, 255));
        canvas.drawRect(8, height - 55, 12, height - 3, paint);
        canvas.drawRect(width - 12, height - 55, width - 8, height - 3, paint);
    }

    private void drawSpotlight(Canvas canvas, float topX, float topY, float bottomX, float bottomY) {
        path.reset();
        path.moveTo(topX - 10, topY);
        path.lineTo(topX + 10, topY);
        path.lineTo(bottomX + 54, bottomY);
        path.lineTo(bottomX - 54, bottomY);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(74, 65, 166, 255));
        canvas.drawPath(path, paint);
    }
}
