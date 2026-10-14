package com.walterchan.boxingtimer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.walterchan.boxingtimer.R;

public final class ArcadeTimerView extends FrameLayout {
    private final TimerSceneView scene;
    private final View pauseButton;
    private final View endButton;

    public ArcadeTimerView(Context context) {
        super(context);
        setBackgroundColor(ArcadeTimerTheme.BG);
        scene = new TimerSceneView(context);
        addView(scene);
        pauseButton = transparentZone(context.getString(R.string.pause));
        endButton = transparentZone(context.getString(R.string.end_training));
        addView(pauseButton);
        addView(endButton);
        verifyLayoutContract();
    }

    public void render(int currentRound, String roundLimitLabel, String remainingLabel, String phaseLabel,
                       boolean running, boolean trainingPhase) {
        scene.render(currentRound, roundLimitLabel, remainingLabel, phaseLabel, running, trainingPhase);
        pauseButton.setContentDescription(running ? getContext().getString(R.string.pause) : getContext().getString(R.string.resume));
    }

    public void setOnToggleClickListener(OnClickListener listener) {
        pauseButton.setOnClickListener(listener);
        scene.setOnClickListener(listener);
        scene.setClickable(true);
    }

    public void setOnEndClickListener(OnClickListener listener) { endButton.setOnClickListener(listener); }
    public void scrollTop() {}

    public void verifyLayoutContract() {
        requireForeground(scene);
        requireForeground(pauseButton);
        requireForeground(endButton);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        scene.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        ArcadeAssetLayout layout = ArcadeAssetLayout.forSize(width, height);
        layout.measure(pauseButton, 226, 132);
        layout.measure(endButton, 230, 132);
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        scene.layout(0, 0, width, height);
        ArcadeAssetLayout layout = ArcadeAssetLayout.forSize(width, height);
        layout.layout(pauseButton, 34, 924, 226, 132);
        layout.layout(endButton, 280, 924, 230, 132);
    }

    private View transparentZone(String description) {
        View view = new View(getContext());
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setClickable(true);
        view.setFocusable(true);
        view.setContentDescription(description);
        return view;
    }

    private void requireForeground(View view) {
        if (view == null || view.getParent() != this || view.getVisibility() != View.VISIBLE) {
            throw new IllegalStateException("Required timer control is missing from arcade foreground");
        }
    }

    @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return true;
    }

    private static final class TimerSceneView extends View {
        private final ArcadeAssetRenderer renderer;
        private int currentRound = 1;
        private String roundLimitLabel = "∞";
        private String remainingLabel = "00:00";
        private String phaseLabel;
        private boolean running = true;
        private boolean trainingPhase = true;

        TimerSceneView(Context context) {
            super(context);
            renderer = new ArcadeAssetRenderer(context);
            phaseLabel = context.getString(R.string.phase_training_running);
            setFocusable(false);
        }

        void render(int currentRound, String roundLimitLabel, String remainingLabel, String phaseLabel,
                    boolean running, boolean trainingPhase) {
            this.currentRound = currentRound;
            this.roundLimitLabel = roundLimitLabel;
            this.remainingLabel = remainingLabel;
            this.phaseLabel = phaseLabel;
            this.running = running;
            this.trainingPhase = trainingPhase;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            renderer.drawTimer(canvas, getWidth(), getHeight(), currentRound, roundLimitLabel,
                    remainingLabel, phaseLabel, running, trainingPhase);
        }
    }
}
