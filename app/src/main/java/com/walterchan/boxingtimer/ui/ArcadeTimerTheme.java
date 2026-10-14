package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

public final class ArcadeTimerTheme {
    public static final int BG = Color.rgb(2, 7, 24);
    public static final int TEXT = Color.rgb(240, 248, 255);
    public static final int MUTED = Color.rgb(168, 207, 230);
    public static final int GOLD = Color.rgb(255, 216, 61);
    public static final int CYAN = Color.rgb(116, 231, 255);
    public static final int REST_ACCENT = Color.rgb(85, 214, 179);
    public static final int DANGER_TEXT = Color.rgb(255, 190, 174);

    private ArcadeTimerTheme() {}

    public static TextView text(Context context, CharSequence value, int sp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        if (bold) view.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        return view;
    }

    public static Button button(Context context, CharSequence label, boolean primary) {
        Button button = new Button(context);
        button.setText(label);
        button.setTextColor(primary ? Color.rgb(5, 20, 48) : TEXT);
        button.setTextSize(primary ? 24 : 18);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setAllCaps(false);
        button.setMinHeight(dp(context, primary ? 72 : 60));
        button.setShadowLayer(2f, 2f, 2f, Color.BLACK);
        button.setBackgroundResource(primary
                ? com.walterchan.boxingtimer.R.drawable.button_primary
                : com.walterchan.boxingtimer.R.drawable.button_secondary);
        return button;
    }

    public static int timerSp(Context context) {
        boolean compact = context.getResources().getConfiguration().smallestScreenWidthDp < 360;
        int base = compact ? 72 : 88;
        float fontScale = context.getResources().getConfiguration().fontScale;
        if (fontScale <= 1.3f) return base;
        return Math.max(56, Math.round(base * 1.3f / fontScale));
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
