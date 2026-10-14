package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.walterchan.boxingtimer.R;

public final class ArcadeHomeView extends FrameLayout {
    private final HomeSceneView scene;
    private final EditText trainingInput;
    private final EditText restInput;
    private final EditText roundsInput;
    private final EditText reminderInput;
    private final View[] arrowZones = new View[8];
    private final View startButton;

    public ArcadeHomeView(Context context, String training, String rest, String rounds, String reminder) {
        super(context);
        setBackgroundColor(ArcadeTimerTheme.BG);
        scene = new HomeSceneView(context);
        addView(scene);
        trainingInput = createValueInput(training, InputType.TYPE_CLASS_TEXT, 5);
        restInput = createValueInput(rest, InputType.TYPE_CLASS_TEXT, 5);
        roundsInput = createValueInput(rounds, InputType.TYPE_CLASS_NUMBER, 2);
        reminderInput = createValueInput(formatReminder(reminder), InputType.TYPE_CLASS_TEXT, 5);
        addView(trainingInput);
        addView(restInput);
        addView(roundsInput);
        addView(reminderInput);
        addArrowZones();
        startButton = transparentZone(context.getString(R.string.start_training));
        addView(startButton);
        verifyLayoutContract();
    }

    public String trainingText() { return trainingInput.getText().toString().trim(); }
    public String restText() { return restInput.getText().toString().trim(); }
    public String roundsText() { return roundsInput.getText().toString().trim(); }
    public String reminderText() { return reminderInput.getText().toString().trim(); }
    public void setOnStartClickListener(OnClickListener listener) { startButton.setOnClickListener(listener); }
    public void scrollTop() {}

    public void verifyLayoutContract() {
        requireForeground(trainingInput);
        requireForeground(restInput);
        requireForeground(roundsInput);
        requireForeground(reminderInput);
        requireForeground(startButton);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        scene.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        ArcadeAssetLayout layout = ArcadeAssetLayout.forSize(width, height);
        layout.measure(trainingInput, 190, 70);
        layout.measure(restInput, 190, 70);
        layout.measure(roundsInput, 190, 70);
        layout.measure(reminderInput, 190, 70);
        for (View zone : arrowZones) layout.measure(zone, 68, 40);
        layout.measure(startButton, 400, 105);
        float valueTextPx = layout.textPx(32);
        applyTextSize(trainingInput, valueTextPx);
        applyTextSize(restInput, valueTextPx);
        applyTextSize(roundsInput, valueTextPx);
        applyTextSize(reminderInput, valueTextPx);
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        scene.layout(0, 0, width, height);
        ArcadeAssetLayout layout = ArcadeAssetLayout.forSize(width, height);
        layoutValue(layout, trainingInput, 0);
        layoutValue(layout, restInput, 1);
        layoutValue(layout, roundsInput, 2);
        layoutValue(layout, reminderInput, 3);
        for (int row = 0; row < 4; row++) {
            float rowTop = 398 + row * 98;
            layout.layout(arrowZones[row * 2], 423, rowTop, 68, 40);
            layout.layout(arrowZones[row * 2 + 1], 423, rowTop + 41, 68, 40);
        }
        layout.layout(startButton, 70, 811, 400, 105);
    }

    private EditText createValueInput(String value, int type, int maxLength) {
        EditText input = new EditText(getContext());
        input.setText(value);
        input.setSingleLine(true);
        input.setGravity(Gravity.CENTER);
        input.setInputType(type);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        input.setTextColor(Color.rgb(255, 243, 106));
        input.setTypeface(Typeface.create("sans-serif-monospace", Typeface.BOLD));
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setPadding(0, 0, 0, 0);
        input.setIncludeFontPadding(false);
        input.setSelectAllOnFocus(false);
        input.setShadowLayer(1f, 2f, 2f, Color.BLACK);
        input.setLetterSpacing(.04f);
        return input;
    }

    private void addArrowZones() {
        EditText[] inputs = {trainingInput, restInput, roundsInput, reminderInput};
        boolean[] duration = {true, true, false, true};
        int[] steps = {5, 5, 1, 1};
        int[] min = {1, 1, 0, 0};
        int[] max = {3599, 3599, 99, 10};
        for (int row = 0; row < 4; row++) {
            final int index = row;
            arrowZones[row * 2] = transparentZone("增加");
            arrowZones[row * 2].setOnClickListener(v -> stepValue(inputs[index], duration[index], steps[index], min[index], max[index]));
            addView(arrowZones[row * 2]);
            arrowZones[row * 2 + 1] = transparentZone("减少");
            arrowZones[row * 2 + 1].setOnClickListener(v -> stepValue(inputs[index], duration[index], -steps[index], min[index], max[index]));
            addView(arrowZones[row * 2 + 1]);
        }
    }

    private View transparentZone(String description) {
        View view = new View(getContext());
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setClickable(true);
        view.setFocusable(true);
        view.setContentDescription(description);
        return view;
    }

    private void stepValue(EditText input, boolean duration, int delta, int min, int max) {
        int value = duration ? parseDuration(input.getText().toString()) : parseNumber(input.getText().toString());
        if (value < min) value = min;
        int next = Math.max(min, Math.min(max, value + delta));
        input.setText(duration ? formatDuration(next) : String.valueOf(next));
        input.setSelection(input.getText().length());
    }

    private int parseDuration(String value) {
        String[] parts = value.trim().split(":");
        if (parts.length != 2) return 0;
        try {
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private int parseNumber(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private String formatDuration(int seconds) {
        return String.format(java.util.Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private String formatReminder(String value) {
        if (value != null && value.contains(":")) return value;
        return formatDuration(parseNumber(value == null ? "0" : value));
    }

    private void layoutValue(ArcadeAssetLayout layout, View child, int row) {
        layout.layout(child, 226, 399 + row * 98, 190, 70);
    }

    private void applyTextSize(EditText input, float pixels) {
        input.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixels);
    }

    private void requireForeground(View view) {
        if (view == null || view.getParent() != this || view.getVisibility() != View.VISIBLE) {
            throw new IllegalStateException("Required home control is missing from arcade foreground");
        }
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return true;
    }

    private static final class HomeSceneView extends View {
        private final ArcadeAssetRenderer renderer;

        HomeSceneView(Context context) {
            super(context);
            renderer = new ArcadeAssetRenderer(context);
            setClickable(false);
            setFocusable(false);
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            renderer.drawHome(canvas, getWidth(), getHeight());
        }
    }
}
