package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import com.walterchan.boxingtimer.R;

public final class ArcadeHomeHeroView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Bitmap glove;

    public ArcadeHomeHeroView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        glove = BitmapFactory.decodeResource(getResources(), R.drawable.pixel_glove_logo);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = ArcadeTimerTheme.dp(getContext(), 250);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        drawArenaTop(canvas, w, h);
        drawPoster(canvas, w * .05f, h * .08f, w * .22f, h * .58f, true);
        drawPoster(canvas, w * .78f, h * .08f, w * .95f, h * .58f, false);
        drawCrown(canvas, w, h);
        drawTitle(canvas, w, h);
        drawGloves(canvas, w, h);
    }

    private void drawArenaTop(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(3, 18, 55));
        canvas.drawRect(0, 0, w, h, paint);
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 24; col++) {
                int tone = (row * 5 + col * 3) % 4;
                paint.setColor(tone == 0 ? Color.rgb(12, 92, 185)
                        : tone == 1 ? Color.rgb(16, 62, 142) : Color.rgb(4, 31, 86));
                float x = col * w / 24f;
                float y = row * h * .045f;
                canvas.drawRect(x, y, x + w / 28f, y + h * .028f, paint);
            }
        }
        drawBeam(canvas, w * .23f, h * .08f, w * .35f, h * .72f);
        drawBeam(canvas, w * .77f, h * .08f, w * .65f, h * .72f);
        drawStar(canvas, w * .28f, h * .22f, h * .08f, Color.rgb(196, 244, 255));
        drawStar(canvas, w * .72f, h * .22f, h * .08f, Color.rgb(196, 244, 255));
        paint.setColor(Color.rgb(255, 230, 76));
        for (int i = 0; i < 18; i++) {
            float x = i * w / 17f;
            canvas.drawCircle(x, h * .68f + (i % 2) * 5, 3.5f, paint);
        }
        paint.setColor(Color.rgb(255, 238, 71));
        for (int i = 0; i < 12; i++) {
            canvas.drawRect(i * w / 12f, h * .6f, i * w / 12f + w / 24f, h * .66f, paint);
            paint.setColor(i % 2 == 0 ? Color.rgb(5, 19, 50) : Color.rgb(255, 238, 71));
        }
    }

    private void drawPoster(Canvas canvas, float left, float top, float right, float bottom, boolean blue) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(blue ? Color.rgb(179, 219, 255) : Color.rgb(255, 106, 89));
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setColor(blue ? Color.rgb(38, 96, 180) : Color.rgb(24, 52, 93));
        float cx = (left + right) / 2f;
        float cy = top + (bottom - top) * .38f;
        canvas.drawCircle(cx, cy, (right - left) * .16f, paint);
        canvas.drawRect(cx - 5, cy + 12, cx + 9, bottom - 20, paint);
        canvas.drawCircle(cx + (blue ? -18 : 18), cy + 28, 9, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ArcadeTimerTheme.dp(getContext(), 3));
        paint.setColor(Color.rgb(79, 181, 255));
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawCrown(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 226, 67));
        float cx = w * .5f;
        float y = h * .27f;
        canvas.drawRect(cx - 26, y + 18, cx + 26, y + 28, paint);
        float[] points = {cx - 25, y + 18, cx - 18, y, cx - 6, y + 16,
                cx, y - 8, cx + 6, y + 16, cx + 18, y, cx + 25, y + 18};
        for (int i = 0; i < points.length - 2; i += 2) {
            canvas.drawLine(points[i], points[i + 1], points[i + 2], points[i + 3], paint);
        }
    }

    private void drawTitle(Canvas canvas, float w, float h) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.min(ArcadeTimerTheme.dp(getContext(), 54), w / 7.1f));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ArcadeTimerTheme.dp(getContext(), 5));
        paint.setColor(Color.rgb(146, 35, 55));
        canvas.drawText(getContext().getString(R.string.app_name), w * .5f, h * .55f, paint);
        paint.setStrokeWidth(ArcadeTimerTheme.dp(getContext(), 2));
        paint.setColor(Color.rgb(3, 18, 55));
        canvas.drawText(getContext().getString(R.string.app_name), w * .5f, h * .55f - 4, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ArcadeTimerTheme.GOLD);
        canvas.drawText(getContext().getString(R.string.app_name), w * .5f, h * .55f - 4, paint);
    }

    private void drawGloves(Canvas canvas, float w, float h) {
        if (glove == null) return;
        float size = h * .22f;
        rect.set(w * .5f - size * .55f, h * .61f, w * .5f + size * .55f, h * .61f + size);
        canvas.drawBitmap(glove, null, rect, paint);
        paint.setColor(Color.rgb(255, 247, 145));
        paint.setStrokeWidth(3f);
        canvas.drawLine(w * .5f, h * .58f, w * .5f, h * .63f, paint);
    }

    private void drawBeam(Canvas canvas, float topX, float topY, float bottomX, float bottomY) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(76, 75, 173, 255));
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(topX - 7, topY);
        path.lineTo(topX + 7, topY);
        path.lineTo(bottomX + 48, bottomY);
        path.lineTo(bottomX - 48, bottomY);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius, int color) {
        paint.setColor(color);
        paint.setStrokeWidth(ArcadeTimerTheme.dp(getContext(), 4));
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paint);
        canvas.drawLine(cx, cy - radius, cx, cy + radius, paint);
        paint.setStrokeWidth(ArcadeTimerTheme.dp(getContext(), 2));
        canvas.drawLine(cx - radius * .65f, cy - radius * .65f, cx + radius * .65f, cy + radius * .65f, paint);
        canvas.drawLine(cx - radius * .65f, cy + radius * .65f, cx + radius * .65f, cy - radius * .65f, paint);
    }
}
