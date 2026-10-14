package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import com.walterchan.boxingtimer.R;

final class ArcadeScenePainter {
    private final Paint paint = new Paint();
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final Context context;

    ArcadeScenePainter(Context context) {
        this.context = context;
        paint.setAntiAlias(false);
    }

    void drawHome(Canvas canvas, int width, int height) {
        drawArena(canvas, width, height, true);
        drawHomeTitle(canvas, width, height);
        drawHomePanel(canvas, width, height);
        drawStartButton(canvas, width, height);
        drawFooterRing(canvas, width, height, true);
    }

    void drawTimer(Canvas canvas, int width, int height, int currentRound, String roundLimit,
                   String remaining, String phaseLabel, boolean running, boolean trainingPhase) {
        drawArena(canvas, width, height, false);
        drawLaurelRound(canvas, width, height, currentRound, roundLimit);
        drawTimerPanel(canvas, width, height, remaining, phaseLabel, running, trainingPhase);
        drawBackBoxer(canvas, width, height);
        drawFooterRing(canvas, width, height, false);
        drawActionButton(canvas, width * .07f, height * .84f, width * .46f, height * .93f,
                running ? context.getString(R.string.pause) : context.getString(R.string.resume),
                Color.rgb(0, 116, 216), Color.rgb(0, 168, 255));
        drawActionButton(canvas, width * .54f, height * .84f, width * .93f, height * .93f,
                context.getString(R.string.end_training), Color.rgb(215, 62, 54), Color.rgb(255, 106, 88));
    }

    private void drawArena(Canvas canvas, float w, float h, boolean home) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(2, 7, 24));
        canvas.drawRect(0, 0, w, h, paint);

        paint.setColor(Color.rgb(5, 28, 76));
        canvas.drawRect(0, 0, w, h * (home ? .18f : .16f), paint);
        paint.setColor(Color.rgb(0, 18, 55));
        canvas.drawRect(0, h * .025f, w, h * .055f, paint);
        paint.setColor(Color.rgb(34, 130, 238));
        for (int i = 0; i < 13; i++) {
            float x = i * w / 12f;
            canvas.drawRect(x - w * .006f, h * .026f, x + w * .006f, h * .055f, paint);
        }
        paint.setColor(Color.rgb(136, 204, 255));
        canvas.drawRect(0, h * .057f, w, h * .064f, paint);

        for (int row = 0; row < 14; row++) {
            for (int col = 0; col < 30; col++) {
                int tone = Math.abs(row * 9 + col * 5) % 5;
                paint.setColor(tone == 0 ? Color.rgb(18, 96, 184)
                        : tone == 1 ? Color.rgb(9, 56, 132)
                        : tone == 2 ? Color.rgb(4, 29, 82) : Color.rgb(3, 18, 55));
                float x = col * w / 30f;
                float y = h * .065f + row * h * .014f;
                canvas.drawRect(x, y, x + w * .026f, y + h * .01f, paint);
            }
        }

        drawPoster(canvas, w * .03f, h * .035f, w * .19f, h * (home ? .235f : .245f), true);
        drawPoster(canvas, w * .81f, h * .035f, w * .97f, h * (home ? .235f : .245f), false);
        drawBeam(canvas, w * .25f, h * .04f, w * .34f, h * (home ? .37f : .57f));
        drawBeam(canvas, w * .75f, h * .04f, w * .66f, h * (home ? .37f : .57f));
        drawStar(canvas, w * .27f, h * .066f, h * .029f, Color.rgb(205, 247, 255));
        drawStar(canvas, w * .73f, h * .066f, h * .029f, Color.rgb(205, 247, 255));
        if (!home) drawStar(canvas, w * .5f, h * .105f, h * .035f, Color.rgb(255, 224, 89));

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 25; i++) {
            paint.setColor(i % 2 == 0 ? Color.rgb(255, 190, 73) : Color.rgb(255, 88, 78));
            canvas.drawRect(i * w / 24f - w * .008f, h * (home ? .283f : .17f) + (i % 2) * 4,
                    i * w / 24f + w * .008f, h * (home ? .287f : .174f) + (i % 2) * 4, paint);
        }
        drawCrowd(canvas, w, h, home ? h * .27f : h * .18f, h * .83f);
    }

    private void drawHomeTitle(Canvas canvas, float w, float h) {
        drawCrown(canvas, w * .5f, h * .097f, w * .11f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(w * .145f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * .022f);
        paint.setColor(Color.rgb(5, 18, 47));
        canvas.drawText(context.getString(R.string.app_name), w * .505f, h * .192f, paint);
        paint.setStrokeWidth(w * .014f);
        paint.setColor(Color.rgb(142, 35, 55));
        canvas.drawText(context.getString(R.string.app_name), w * .518f, h * .202f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 226, 69));
        canvas.drawText(context.getString(R.string.app_name), w * .5f, h * .19f, paint);
        paint.setColor(Color.rgb(255, 245, 128));
        canvas.drawRect(w * .14f, h * .203f, w * .21f, h * .209f, paint);
        canvas.drawRect(w * .73f, h * .204f, w * .80f, h * .210f, paint);
        drawGloves(canvas, w * .5f, h * .267f, w * .22f);
    }

    private void drawHomePanel(Canvas canvas, float w, float h) {
        float left = w * .07f, top = h * .345f, right = w * .93f, bottom = h * .705f;
        drawMetalPanel(canvas, left, top, right, bottom);
        String[] labels = {
                context.getString(R.string.training_duration),
                context.getString(R.string.rest_duration),
                context.getString(R.string.round_count),
                context.getString(R.string.last_reminder)
        };
        for (int i = 0; i < labels.length; i++) {
            float rowTop = h * (.38f + .09f * i);
            drawParameterRow(canvas, w, rowTop, h * .074f, labels[i]);
        }
    }

    private void drawStartButton(Canvas canvas, float w, float h) {
        rect.set(w * .13f, h * .73f, w * .87f, h * .81f);
        drawPixelButtonBase(canvas, rect, Color.rgb(255, 228, 77), Color.rgb(255, 242, 125), Color.rgb(173, 106, 0));
        drawOutlinedText(canvas, context.getString(R.string.start_training), w * .5f, h * .785f,
                w * .084f, Color.rgb(5, 20, 48), Color.rgb(255, 245, 128), 0f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 232, 67));
        drawStar(canvas, w * .095f, h * .77f, w * .025f, Color.rgb(255, 232, 67));
        drawStar(canvas, w * .905f, h * .77f, w * .025f, Color.rgb(255, 232, 67));
    }

    private void drawParameterRow(Canvas canvas, float w, float y, float rowH, String label) {
        float left = w * .10f, right = w * .90f;
        rect.set(left, y, right, y + rowH);
        drawInset(canvas, rect, Color.rgb(9, 37, 70), Color.rgb(91, 159, 207), Color.rgb(5, 15, 35));
        paint.setColor(Color.rgb(21, 78, 126));
        canvas.drawRect(left + w * .012f, y + rowH * .10f, right - w * .012f, y + rowH * .18f, paint);
        paint.setColor(Color.rgb(1, 9, 24));
        canvas.drawRect(left + w * .012f, y + rowH * .82f, right - w * .012f, y + rowH * .90f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(w * .052f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawText(label, w * .13f, y + rowH * .64f, paint);
        rect.set(w * .43f, y + rowH * .15f, w * .80f, y + rowH * .85f);
        drawInset(canvas, rect, Color.rgb(2, 10, 20), Color.rgb(44, 175, 241), Color.rgb(0, 4, 12));
        drawPanelScanlines(canvas, rect.left, rect.top, rect.right, rect.bottom, rowH * .08f);
        drawArrowBox(canvas, w * .82f, y + rowH * .12f, w * .91f, y + rowH * .47f, true);
        drawArrowBox(canvas, w * .82f, y + rowH * .53f, w * .91f, y + rowH * .88f, false);
    }

    private void drawLaurelRound(Canvas canvas, float w, float h, int currentRound, String roundLimit) {
        for (int side = -1; side <= 1; side += 2) {
            paint.setColor(Color.rgb(230, 212, 67));
            paint.setStyle(Paint.Style.FILL);
            float cx = side < 0 ? w * .21f : w * .79f;
            for (int i = 0; i < 8; i++) {
                float y = h * (.205f + i * .012f);
                canvas.drawOval(cx + side * i * w * .006f - w * .018f, y, cx + side * i * w * .006f + w * .018f, y + h * .012f, paint);
            }
        }
        rect.set(w * .18f, h * .20f, w * .82f, h * .28f);
        drawMetalPanel(canvas, rect.left, rect.top, rect.right, rect.bottom);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(w * .055f);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("第  " + currentRound + " / " + roundLimit + "  回合", w * .5f, h * .253f, paint);
        paint.setColor(Color.rgb(255, 238, 64));
        canvas.drawText(String.valueOf(currentRound), w * .40f, h * .253f, paint);
    }

    private void drawTimerPanel(Canvas canvas, float w, float h, String remaining, String phaseLabel,
                                boolean running, boolean trainingPhase) {
        rect.set(w * .05f, h * .30f, w * .95f, h * .58f);
        drawMetalPanel(canvas, rect.left, rect.top, rect.right, rect.bottom);
        rect.set(w * .075f, h * .325f, w * .925f, h * .485f);
        drawInset(canvas, rect, Color.rgb(1, 9, 19), Color.rgb(60, 224, 255), Color.rgb(0, 4, 12));
        drawPanelScanlines(canvas, rect.left, rect.top, rect.right, rect.bottom, h * .004f);
        drawDigitalTime(canvas, remaining, w * .5f, h * .462f, w * .268f);
        paint.setColor(Color.rgb(116, 231, 255));
        canvas.drawRect(w * .20f, h * .525f, w * .34f, h * .531f, paint);
        canvas.drawRect(w * .66f, h * .525f, w * .80f, h * .531f, paint);
        int phaseColor = running ? (trainingPhase ? Color.rgb(116, 231, 255) : Color.rgb(85, 214, 179)) : Color.WHITE;
        drawOutlinedText(canvas, phaseLabel, w * .5f, h * .56f, w * .072f, phaseColor, Color.rgb(4, 19, 44), 0f);
    }

    private void drawPanelScanlines(Canvas canvas, float left, float top, float right, float bottom, float step) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(3, 18, 38));
        float safeStep = Math.max(4f, step);
        for (float y = top + safeStep; y < bottom; y += safeStep * 2f) {
            canvas.drawRect(left + 4f, y, right - 4f, y + Math.max(2f, safeStep * .35f), paint);
        }
    }

    private void drawDigitalTime(Canvas canvas, String text, float cx, float baseline, float size) {
        float digitW = size * .54f;
        float digitH = size * .92f;
        float gap = size * .10f;
        float colonW = size * .20f;
        float total = digitW * 4f + colonW + gap * 4f;
        float x = cx - total * .5f;
        float top = baseline - digitH;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':') {
                drawDigitalColon(canvas, x + colonW * .5f, top, digitH, size * .08f);
                x += colonW + gap;
            } else if (c >= '0' && c <= '9') {
                drawSevenSegment(canvas, c - '0', x, top, digitW, digitH);
                x += digitW + gap;
            }
        }
    }

    private void drawSevenSegment(Canvas canvas, int digit, float left, float top, float width, float height) {
        final boolean[][] segments = {
                {true, true, true, true, true, true, false},
                {false, true, true, false, false, false, false},
                {true, true, false, true, true, false, true},
                {true, true, true, true, false, false, true},
                {false, true, true, false, false, true, true},
                {true, false, true, true, false, true, true},
                {true, false, true, true, true, true, true},
                {true, true, true, false, false, false, false},
                {true, true, true, true, true, true, true},
                {true, true, true, true, false, true, true}
        };
        float t = width * .15f;
        int on = Color.rgb(255, 242, 56);
        int shadow = Color.rgb(1, 4, 10);
        paint.setStyle(Paint.Style.FILL);
        drawSegment(canvas, left + width * .10f + t * .20f, top + t * .15f, width * .80f - t * .40f, t, segments[digit][0], on, shadow);
        drawSegment(canvas, left + width - t, top + height * .08f + t * .40f, t, height * .34f, segments[digit][1], on, shadow);
        drawSegment(canvas, left + width - t, top + height * .54f, t, height * .34f, segments[digit][2], on, shadow);
        drawSegment(canvas, left + width * .10f + t * .20f, top + height - t * 1.15f, width * .80f - t * .40f, t, segments[digit][3], on, shadow);
        drawSegment(canvas, left, top + height * .54f, t, height * .34f, segments[digit][4], on, shadow);
        drawSegment(canvas, left, top + height * .08f + t * .40f, t, height * .34f, segments[digit][5], on, shadow);
        drawSegment(canvas, left + width * .10f + t * .20f, top + height * .47f, width * .80f - t * .40f, t, segments[digit][6], on, shadow);
    }

    private void drawSegment(Canvas canvas, float left, float top, float width, float height, boolean active, int on, int shadow) {
        paint.setColor(shadow);
        canvas.drawRect(left + height * .24f, top + height * .24f, left + width + height * .24f, top + height + height * .24f, paint);
        paint.setColor(active ? on : Color.rgb(17, 31, 38));
        canvas.drawRect(left, top, left + width, top + height, paint);
        if (active) {
            paint.setColor(Color.rgb(255, 255, 132));
            canvas.drawRect(left + height * .18f, top + height * .12f, left + width * .42f, top + height * .28f, paint);
        }
    }

    private void drawDigitalColon(Canvas canvas, float cx, float top, float height, float size) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 242, 56));
        canvas.drawRect(cx - size, top + height * .30f, cx + size, top + height * .30f + size * 2f, paint);
        canvas.drawRect(cx - size, top + height * .64f, cx + size, top + height * .64f + size * 2f, paint);
    }

    private void drawBackBoxer(Canvas canvas, float w, float h) {
        float cx = w * .5f;
        float base = h * .79f;
        drawCrowd(canvas, w, h, h * .55f, h * .82f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(244, 172, 100));
        canvas.drawCircle(cx, base - h * .12f, w * .045f, paint);
        canvas.drawRect(cx - w * .07f, base - h * .105f, cx + w * .07f, base - h * .025f, paint);
        paint.setColor(Color.rgb(202, 109, 63));
        canvas.drawRect(cx - w * .012f, base - h * .10f, cx + w * .012f, base - h * .035f, paint);
        canvas.drawRect(cx - w * .052f, base - h * .082f, cx - w * .022f, base - h * .072f, paint);
        canvas.drawRect(cx + w * .022f, base - h * .082f, cx + w * .052f, base - h * .072f, paint);
        paint.setColor(Color.rgb(255, 199, 128));
        canvas.drawRect(cx - w * .060f, base - h * .100f, cx - w * .032f, base - h * .090f, paint);
        canvas.drawRect(cx + w * .032f, base - h * .100f, cx + w * .060f, base - h * .090f, paint);
        paint.setColor(Color.rgb(244, 172, 100));
        paint.setStrokeWidth(w * .035f);
        canvas.drawLine(cx - w * .055f, base - h * .095f, cx - w * .19f, base - h * .155f, paint);
        canvas.drawLine(cx + w * .055f, base - h * .095f, cx + w * .19f, base - h * .155f, paint);
        paint.setColor(Color.rgb(255, 92, 76));
        canvas.drawCircle(cx - w * .22f, base - h * .17f, w * .055f, paint);
        canvas.drawCircle(cx + w * .22f, base - h * .17f, w * .055f, paint);
        paint.setColor(Color.rgb(220, 238, 255));
        canvas.drawRect(cx - w * .085f, base - h * .025f, cx + w * .085f, base + h * .02f, paint);
        paint.setColor(Color.rgb(24, 82, 170));
        for (int i = -2; i <= 2; i++) {
            canvas.drawRect(cx + i * w * .035f - w * .008f, base - h * .025f,
                    cx + i * w * .035f + w * .008f, base + h * .02f, paint);
        }
        paint.setColor(Color.rgb(5, 18, 40));
        canvas.drawRect(cx - w * .04f, base - h * .155f, cx + w * .04f, base - h * .135f, paint);
    }

    private void drawFooterRing(Canvas canvas, float w, float h, boolean home) {
        float top = home ? h * .82f : h * .74f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(57, 169, 255));
        canvas.drawRect(0, top, w, h, paint);
        paint.setColor(Color.rgb(123, 206, 255));
        float tile = w / 14f;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 14; x++) {
                if ((x + y) % 2 == 0) {
                    canvas.drawRect(x * tile, top + y * tile * .38f, (x + 1) * tile, top + (y + 1) * tile * .38f, paint);
                }
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * .012f);
        int[] colors = {Color.rgb(237, 55, 62), Color.WHITE, Color.rgb(47, 145, 255)};
        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            float y = top - h * .035f + i * h * .015f;
            canvas.drawLine(0, y, w, y, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(41, 145, 234));
        canvas.drawRect(w * .03f, top - h * .06f, w * .08f, top + h * .06f, paint);
        canvas.drawRect(w * .92f, top - h * .06f, w * .97f, top + h * .06f, paint);
        paint.setColor(Color.rgb(255, 92, 76));
        canvas.drawRect(w * .00f, top - h * .045f, w * .035f, top + h * .08f, paint);
        canvas.drawRect(w * .965f, top - h * .045f, w, top + h * .08f, paint);
        if (home) {
            drawSmallFighter(canvas, w * .30f, h * .92f, w * .16f, true);
            drawSmallFighter(canvas, w * .70f, h * .92f, w * .16f, false);
            drawStar(canvas, w * .5f, h * .91f, w * .035f, Color.rgb(255, 226, 69));
        }
    }

    private void drawSmallFighter(Canvas canvas, float cx, float cy, float size, boolean red) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(red ? Color.rgb(235, 70, 67) : Color.rgb(24, 82, 180));
        canvas.drawCircle(cx, cy - size * .45f, size * .12f, paint);
        canvas.drawRect(cx - size * .16f, cy - size * .35f, cx + size * .14f, cy - size * .05f, paint);
        canvas.drawCircle(cx + (red ? size * .28f : -size * .28f), cy - size * .33f, size * .11f, paint);
        paint.setStrokeWidth(size * .08f);
        canvas.drawLine(cx, cy - size * .26f, cx + (red ? size * .22f : -size * .22f), cy - size * .32f, paint);
    }

    private void drawPoster(Canvas canvas, float left, float top, float right, float bottom, boolean redSide) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(redSide ? Color.rgb(199, 89, 84) : Color.rgb(170, 214, 255));
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setColor(Color.rgb(17, 53, 111));
        float cx = (left + right) * .5f;
        float bodyTop = top + (bottom - top) * .28f;
        canvas.drawCircle(cx, bodyTop, (right - left) * .14f, paint);
        canvas.drawRect(cx - (right - left) * .10f, bodyTop + (bottom - top) * .08f,
                cx + (right - left) * .08f, bottom - (bottom - top) * .16f, paint);
        canvas.drawCircle(cx + (redSide ? (right - left) * .22f : -(right - left) * .22f),
                bodyTop + (bottom - top) * .18f, (right - left) * .11f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(4f, (right - left) * .04f));
        paint.setColor(Color.rgb(108, 188, 255));
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawMetalPanel(Canvas canvas, float left, float top, float right, float bottom) {
        rect.set(left, top + (bottom - top) * .025f, right, bottom + (bottom - top) * .035f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(2, 10, 24));
        canvas.drawRect(rect, paint);
        rect.set(left, top, right, bottom);
        drawInset(canvas, rect, Color.rgb(14, 48, 84), Color.rgb(132, 200, 244), Color.rgb(5, 17, 39));
        paint.setColor(Color.rgb(34, 96, 143));
        canvas.drawRect(left + 10, top + 10, right - 10, top + 16, paint);
        canvas.drawRect(left + 10, bottom - 16, right - 10, bottom - 10, paint);
    }

    private void drawInset(Canvas canvas, RectF r, int fill, int light, int dark) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawRect(r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(3f, r.width() * .008f));
        paint.setColor(dark);
        canvas.drawRect(r, paint);
        paint.setStrokeWidth(Math.max(2f, r.width() * .005f));
        paint.setColor(light);
        canvas.drawLine(r.left, r.top, r.right, r.top, paint);
        canvas.drawLine(r.left, r.top, r.left, r.bottom, paint);
        paint.setColor(Color.rgb(0, 4, 12));
        canvas.drawLine(r.right, r.top, r.right, r.bottom, paint);
        canvas.drawLine(r.left, r.bottom, r.right, r.bottom, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawPixelButtonBase(Canvas canvas, RectF r, int fill, int light, int shadow) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(2, 8, 18));
        canvas.drawRect(r.left + 8, r.top + 8, r.right + 8, r.bottom + 8, paint);
        paint.setColor(fill);
        canvas.drawRect(r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7f);
        paint.setColor(Color.rgb(4, 17, 41));
        canvas.drawRect(r, paint);
        paint.setStrokeWidth(4f);
        paint.setColor(light);
        canvas.drawLine(r.left + 8, r.top + 8, r.right - 8, r.top + 8, paint);
        canvas.drawLine(r.left + 8, r.top + 8, r.left + 8, r.bottom - 8, paint);
        paint.setColor(shadow);
        canvas.drawLine(r.left + 8, r.bottom - 8, r.right - 8, r.bottom - 8, paint);
        canvas.drawLine(r.right - 8, r.top + 8, r.right - 8, r.bottom - 8, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawActionButton(Canvas canvas, float left, float top, float right, float bottom, String label, int fill, int light) {
        rect.set(left, top, right, bottom);
        drawPixelButtonBase(canvas, rect, fill, light, Color.rgb(6, 16, 35));
        drawOutlinedText(canvas, label, (left + right) * .5f, top + (bottom - top) * .64f,
                (right - left) * .18f, Color.WHITE, Color.rgb(4, 17, 41), 0f);
    }

    private void drawArrowBox(Canvas canvas, float left, float top, float right, float bottom, boolean up) {
        rect.set(left, top, right, bottom);
        drawInset(canvas, rect, Color.rgb(30, 122, 225), Color.rgb(103, 207, 255), Color.rgb(1, 16, 45));
        paint.setColor(Color.WHITE);
        path.reset();
        float cx = (left + right) * .5f;
        float cy = (top + bottom) * .5f;
        float s = (right - left) * .22f;
        if (up) {
            path.moveTo(cx, cy - s);
            path.lineTo(cx - s, cy + s);
            path.lineTo(cx + s, cy + s);
        } else {
            path.moveTo(cx, cy + s);
            path.lineTo(cx - s, cy - s);
            path.lineTo(cx + s, cy - s);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawBeam(Canvas canvas, float topX, float topY, float bottomX, float bottomY) {
        path.reset();
        path.moveTo(topX - 10, topY);
        path.lineTo(topX + 10, topY);
        path.lineTo(bottomX + 64, bottomY);
        path.lineTo(bottomX - 64, bottomY);
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(76, 75, 173, 255));
        canvas.drawPath(path, paint);
    }

    private void drawCrowd(Canvas canvas, float w, float h, float top, float bottom) {
        paint.setStyle(Paint.Style.FILL);
        float dot = Math.max(3f, w * .008f);
        for (float y = top; y < bottom; y += dot * 3f) {
            for (float x = dot; x < w; x += dot * 3.1f) {
                int selector = ((int) x * 7 + (int) y * 11) % 7;
                paint.setColor(selector == 0 ? Color.rgb(255, 92, 76)
                        : selector == 1 ? Color.rgb(65, 185, 255)
                        : selector == 2 ? Color.rgb(31, 117, 216) : Color.rgb(14, 62, 126));
                canvas.drawRect(x, y, x + dot, y + dot, paint);
            }
        }
    }

    private void drawGloves(Canvas canvas, float cx, float cy, float size) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 86, 76));
        canvas.drawRect(cx - size * .44f, cy - size * .08f, cx - size * .10f, cy + size * .24f, paint);
        canvas.drawRect(cx + size * .10f, cy - size * .08f, cx + size * .44f, cy + size * .24f, paint);
        canvas.drawCircle(cx - size * .24f, cy - size * .16f, size * .18f, paint);
        canvas.drawCircle(cx + size * .24f, cy - size * .16f, size * .18f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawRect(cx - size * .35f, cy - size * .05f, cx - size * .29f, cy + size * .08f, paint);
        canvas.drawRect(cx + size * .29f, cy - size * .05f, cx + size * .35f, cy + size * .08f, paint);
        paint.setColor(Color.rgb(5, 17, 41));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size * .035f);
        canvas.drawRect(cx - size * .45f, cy - size * .18f, cx - size * .08f, cy + size * .25f, paint);
        canvas.drawRect(cx + size * .08f, cy - size * .18f, cx + size * .45f, cy + size * .25f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawCrown(Canvas canvas, float cx, float y, float size) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 226, 67));
        canvas.drawRect(cx - size * .35f, y + size * .25f, cx + size * .35f, y + size * .37f, paint);
        path.reset();
        path.moveTo(cx - size * .35f, y + size * .25f);
        path.lineTo(cx - size * .22f, y);
        path.lineTo(cx - size * .08f, y + size * .21f);
        path.lineTo(cx, y - size * .10f);
        path.lineTo(cx + size * .08f, y + size * .21f);
        path.lineTo(cx + size * .22f, y);
        path.lineTo(cx + size * .35f, y + size * .25f);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(3f, radius * .16f));
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paint);
        canvas.drawLine(cx, cy - radius, cx, cy + radius, paint);
        paint.setStrokeWidth(Math.max(2f, radius * .08f));
        canvas.drawLine(cx - radius * .65f, cy - radius * .65f, cx + radius * .65f, cy + radius * .65f, paint);
        canvas.drawLine(cx - radius * .65f, cy + radius * .65f, cx + radius * .65f, cy - radius * .65f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawOutlinedText(Canvas canvas, String text, float x, float baseline, float textSize,
                                  int fill, int stroke, float skew) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSkewX(skew);
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(4f, textSize * .09f));
        paint.setColor(stroke);
        canvas.drawText(text, x, baseline, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawText(text, x, baseline, paint);
        paint.setTextSkewX(0f);
    }
}
