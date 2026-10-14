package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.walterchan.boxingtimer.R;

/** Draws the v1.3.9 screens from independent pixel-art bitmaps plus dynamic text. */
public final class ArcadeAssetRenderer {
    private final Paint bitmapPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint digitPaint = new Paint();
    private final Path segmentPath = new Path();

    private final Bitmap homeBackground;
    private final Bitmap timerBackground;
    private final Bitmap settingsPanel;
    private final Bitmap startButton;
    private final Bitmap ringFooter;
    private final Bitmap roundPanel;
    private final Bitmap timerPanel;
    private final Bitmap timerStatusPanel;
    private final Bitmap boxerBack;
    private final Bitmap timerRing;
    private final Bitmap timerControlsDock;
    private final Bitmap phaseTraining;
    private final Bitmap phaseRest;
    private final Bitmap phasePaused;
    private final Bitmap labelPause;
    private final Bitmap labelResume;
    private final Bitmap labelEnd;
    private final Rect pauseLabelAlphaBounds;
    private final Rect resumeLabelAlphaBounds;
    private final Rect endLabelAlphaBounds;

    public ArcadeAssetRenderer(Context context) {
        bitmapPaint.setAntiAlias(false);
        bitmapPaint.setFilterBitmap(false);
        bitmapPaint.setDither(false);
        textPaint.setAntiAlias(false);
        textPaint.setSubpixelText(false);
        textPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        digitPaint.setAntiAlias(false);
        digitPaint.setDither(false);

        homeBackground = load(context, R.drawable.ui_home_background);
        timerBackground = load(context, R.drawable.ui_timer_background);
        settingsPanel = load(context, R.drawable.ui_home_settings_panel);
        startButton = load(context, R.drawable.ui_home_start_button);
        ringFooter = load(context, R.drawable.ui_ring_footer);
        roundPanel = load(context, R.drawable.ui_timer_round_panel);
        timerPanel = load(context, R.drawable.ui_timer_panel);
        timerStatusPanel = load(context, R.drawable.ui_timer_status_panel);
        boxerBack = load(context, R.drawable.ui_boxer_back);
        timerRing = load(context, R.drawable.ui_timer_ring);
        timerControlsDock = load(context, R.drawable.ui_timer_controls_dock);
        phaseTraining = load(context, R.drawable.ui_phase_training);
        phaseRest = load(context, R.drawable.ui_phase_rest);
        phasePaused = load(context, R.drawable.ui_phase_paused);
        labelPause = load(context, R.drawable.ui_label_pause);
        labelResume = load(context, R.drawable.ui_label_resume);
        labelEnd = load(context, R.drawable.ui_label_end);
        pauseLabelAlphaBounds = findAlphaBounds(labelPause);
        resumeLabelAlphaBounds = findAlphaBounds(labelResume);
        endLabelAlphaBounds = findAlphaBounds(labelEnd);
    }

    public void drawHome(Canvas canvas, int width, int height) {
        ArcadeAssetLayout layout = ArcadeAssetLayout.forSize(width, height);
        int save = layout.saveAndApply(canvas);
        draw(canvas, homeBackground, 0, 0, 540, 1120);
        draw(canvas, settingsPanel, 24, 374, 493, 427);
        draw(canvas, startButton, 36, 806, 468, 122);
        draw(canvas, ringFooter, 7, 926, 525, 194);
        canvas.restoreToCount(save);
    }

    public void drawTimer(Canvas canvas, int width, int height, int currentRound, String roundLimit,
                          String remaining, String phaseLabel, boolean running, boolean trainingPhase) {
        ArcadeAssetLayout layout = ArcadeAssetLayout.forSize(width, height);
        int save = layout.saveAndApply(canvas);
        draw(canvas, timerBackground, 0, 0, 540, 1120);
        draw(canvas, roundPanel, 60, 250, 420, 112);
        drawRoundLabel(canvas, currentRound, roundLimit);
        draw(canvas, timerPanel, 26, 355, 488, 302);
        drawDigitalTime(canvas, remaining, 270, 470, 120);
        draw(canvas, timerRing, 0, 730, 540, 280);
        draw(canvas, boxerBack, 0, 638, 540, 309);
        draw(canvas, timerStatusPanel, 42, 545, 456, 140);
        Bitmap phaseBitmap = !running ? phasePaused : (trainingPhase ? phaseTraining : phaseRest);
        draw(canvas, phaseBitmap, 140, 580, 260, 70);
        draw(canvas, timerControlsDock, 0, 924, 540, 196);
        if (running) {
            drawAlphaCentered(canvas, labelPause, pauseLabelAlphaBounds, 136, 1009);
        } else {
            drawAlphaCentered(canvas, labelResume, resumeLabelAlphaBounds, 136, 1009);
        }
        drawAlphaCentered(canvas, labelEnd, endLabelAlphaBounds, 393, 1009);
        canvas.restoreToCount(save);
    }

    private void drawRoundLabel(Canvas canvas, int currentRound, String roundLimit) {
        String left = "第 ";
        String current = String.valueOf(currentRound);
        String right = " / " + roundLimit + " 回合";
        textPaint.setTextSize(35);
        textPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        float total = textPaint.measureText(left) + textPaint.measureText(current) + textPaint.measureText(right);
        float x = 270 - total * .5f;
        drawOutlinedTextLeft(canvas, left, x, 323, 35, Color.WHITE, Color.rgb(1, 18, 42), 3);
        x += textPaint.measureText(left);
        drawOutlinedTextLeft(canvas, current, x, 323, 35, Color.rgb(255, 234, 55), Color.rgb(1, 18, 42), 3);
        x += textPaint.measureText(current);
        drawOutlinedTextLeft(canvas, right, x, 323, 35, Color.WHITE, Color.rgb(1, 18, 42), 3);
    }

    private void drawDigitalTime(Canvas canvas, String value, float centerX, float centerY, float height) {
        String text = value == null || value.length() != 5 ? "00:00" : value;
        float digitWidth = height * .50f;
        float colonWidth = height * .18f;
        float gap = height * .055f;
        float total = digitWidth * 4 + colonWidth + gap * 4;
        float x = centerX - total * .5f;
        float top = centerY - height * .5f;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':') {
                drawColon(canvas, x, top, colonWidth, height);
                x += colonWidth + gap;
            } else {
                int digit = c >= '0' && c <= '9' ? c - '0' : 0;
                drawDigit(canvas, digit, x, top, digitWidth, height);
                x += digitWidth + gap;
            }
        }
    }

    private void drawDigit(Canvas canvas, int digit, float left, float top, float width, float height) {
        final boolean[][] map = {
                {true, true, true, true, true, true, false}, {false, true, true, false, false, false, false},
                {true, true, false, true, true, false, true}, {true, true, true, true, false, false, true},
                {false, true, true, false, false, true, true}, {true, false, true, true, false, true, true},
                {true, false, true, true, true, true, true}, {true, true, true, false, false, false, false},
                {true, true, true, true, true, true, true}, {true, true, true, true, false, true, true}
        };
        float t = width * .17f;
        drawSegment(canvas, left + t * .45f, top, width - t * .90f, t, map[digit][0], true);
        drawSegment(canvas, left + width - t, top + t * .55f, t, height * .38f, map[digit][1], false);
        drawSegment(canvas, left + width - t, top + height * .53f, t, height * .38f, map[digit][2], false);
        drawSegment(canvas, left + t * .45f, top + height - t, width - t * .90f, t, map[digit][3], true);
        drawSegment(canvas, left, top + height * .53f, t, height * .38f, map[digit][4], false);
        drawSegment(canvas, left, top + t * .55f, t, height * .38f, map[digit][5], false);
        drawSegment(canvas, left + t * .45f, top + height * .46f, width - t * .90f, t, map[digit][6], true);
    }

    private void drawSegment(Canvas canvas, float left, float top, float width, float height,
                             boolean active, boolean horizontal) {
        float cut = Math.min(width, height) * .36f;
        segmentPath.reset();
        if (horizontal) {
            segmentPath.moveTo(left + cut, top);
            segmentPath.lineTo(left + width - cut, top);
            segmentPath.lineTo(left + width, top + height * .5f);
            segmentPath.lineTo(left + width - cut, top + height);
            segmentPath.lineTo(left + cut, top + height);
            segmentPath.lineTo(left, top + height * .5f);
        } else {
            segmentPath.moveTo(left + width * .5f, top);
            segmentPath.lineTo(left + width, top + cut);
            segmentPath.lineTo(left + width, top + height - cut);
            segmentPath.lineTo(left + width * .5f, top + height);
            segmentPath.lineTo(left, top + height - cut);
            segmentPath.lineTo(left, top + cut);
        }
        segmentPath.close();
        digitPaint.setColor(active ? Color.rgb(255, 241, 50) : Color.rgb(13, 30, 35));
        canvas.drawPath(segmentPath, digitPaint);
        if (active) {
            digitPaint.setColor(Color.rgb(255, 255, 151));
            canvas.drawRect(left + width * .18f, top + height * .12f,
                    left + width * .54f, top + height * .28f, digitPaint);
        }
    }

    private void drawColon(Canvas canvas, float left, float top, float width, float height) {
        digitPaint.setColor(Color.rgb(255, 241, 50));
        float size = width * .48f;
        canvas.drawRect(left + width * .25f, top + height * .28f,
                left + width * .25f + size, top + height * .28f + size, digitPaint);
        canvas.drawRect(left + width * .25f, top + height * .66f,
                left + width * .25f + size, top + height * .66f + size, digitPaint);
    }

    private void drawOutlinedText(Canvas canvas, String text, float centerX, float baseline, float size,
                                  int fill, int outline, float stroke) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawText(canvas, text, centerX, baseline, size, fill, outline, stroke);
    }

    private void drawOutlinedTextLeft(Canvas canvas, String text, float left, float baseline, float size,
                                      int fill, int outline, float stroke) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        drawText(canvas, text, left, baseline, size, fill, outline, stroke);
    }

    private void drawText(Canvas canvas, String text, float x, float baseline, float size,
                          int fill, int outline, float stroke) {
        textPaint.setTextSize(size);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(stroke);
        textPaint.setColor(outline);
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(fill);
        canvas.drawText(text, x, baseline, textPaint);
    }

    private void draw(Canvas canvas, Bitmap bitmap, float x, float y, float width, float height) {
        if (bitmap == null) return;
        canvas.drawBitmap(bitmap, null, new RectF(x, y, x + width, y + height), bitmapPaint);
    }

    private void drawAlphaCentered(Canvas canvas, Bitmap bitmap, Rect alphaBounds,
                                   float targetCenterX, float targetCenterY) {
        float alphaCenterX = (alphaBounds.left + alphaBounds.right) * .5f;
        float alphaCenterY = (alphaBounds.top + alphaBounds.bottom) * .5f;
        draw(canvas, bitmap, targetCenterX - alphaCenterX, targetCenterY - alphaCenterY,
                bitmap.getWidth(), bitmap.getHeight());
    }

    private static Rect findAlphaBounds(Bitmap bitmap) {
        int left = bitmap.getWidth();
        int top = bitmap.getHeight();
        int right = -1;
        int bottom = -1;
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                if ((bitmap.getPixel(x, y) >>> 24) != 0) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }
        return right >= left && bottom >= top
                ? new Rect(left, top, right + 1, bottom + 1)
                : new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    private static Bitmap load(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
    }
}
