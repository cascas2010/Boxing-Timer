package com.walterchan.boxingtimer.ui;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

/** Shared 540x1120 artwork coordinate system for bitmap assets and real interaction zones. */
public final class ArcadeAssetLayout {
    public static final float DESIGN_WIDTH = 540f;
    public static final float DESIGN_HEIGHT = 1120f;

    private final float scale;
    private final float offsetX;
    private final float offsetY;

    private ArcadeAssetLayout(float scale, float offsetX, float offsetY) {
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public static ArcadeAssetLayout forSize(int width, int height) {
        float scale = Math.min(width / DESIGN_WIDTH, height / DESIGN_HEIGHT);
        return new ArcadeAssetLayout(scale,
                (width - DESIGN_WIDTH * scale) * .5f,
                (height - DESIGN_HEIGHT * scale) * .5f);
    }

    public int saveAndApply(Canvas canvas) {
        int save = canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        return save;
    }

    public void measure(View view, float width, float height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(Math.round(width * scale), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(Math.round(height * scale), View.MeasureSpec.EXACTLY));
    }

    public void layout(View view, float x, float y, float width, float height) {
        int left = Math.round(offsetX + x * scale);
        int top = Math.round(offsetY + y * scale);
        view.layout(left, top, left + Math.round(width * scale), top + Math.round(height * scale));
    }

    public float textPx(float designPx) { return designPx * scale; }

    public RectF screenRect(float x, float y, float width, float height) {
        return new RectF(offsetX + x * scale, offsetY + y * scale,
                offsetX + (x + width) * scale, offsetY + (y + height) * scale);
    }
}
