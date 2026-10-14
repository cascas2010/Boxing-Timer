package com.walterchan.boxingtimer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.walterchan.boxingtimer.ui.ArcadeHomeView;
import com.walterchan.boxingtimer.ui.ArcadeTimerTheme;
import com.walterchan.boxingtimer.ui.ArcadeTimerView;

public final class MainActivity extends Activity {
    private static final String TAG = "BoxingTimer";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 17;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private ArcadeHomeView homeView;
    private ArcadeTimerView timerView;
    private boolean showingTraining;
    private boolean awaitingServiceStart;
    private boolean completionDialogShown;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { renderFromState(); }
    };
    private final Runnable uiTicker = new Runnable() {
        @Override public void run() {
            if (showingTraining) renderFromState();
            uiHandler.postDelayed(this, 200L);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterArcadeFullscreen();
        getWindow().setStatusBarColor(ArcadeTimerTheme.BG);
        getWindow().setNavigationBarColor(ArcadeTimerTheme.BG);
        TimerState state = TimerState.load(this);
        if (state.active) {
            showTraining(null, null);
            if (!startTimerServiceSafely(null)) {
                state.end();
                state.save(this);
                showHome();
                Toast.makeText(this, R.string.timer_restore_failed, Toast.LENGTH_LONG).show();
            }
        } else {
            showHome();
            if (state.completedNaturally) showCompletionDialog();
        }
    }

    @Override protected void onStart() {
        super.onStart();
        enterArcadeFullscreen();
        registerStateReceiver();
        uiHandler.post(uiTicker);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterArcadeFullscreen();
    }

    @Override protected void onStop() {
        uiHandler.removeCallbacks(uiTicker);
        unregisterReceiver(receiver);
        super.onStop();
    }

    @Override public void onBackPressed() {
        if (showingTraining || awaitingServiceStart || TimerState.load(this).active) {
            endTrainingAndReturnHome();
            return;
        }
        super.onBackPressed();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerStateReceiver() {
        IntentFilter filter = new IntentFilter(TimerService.ACTION_STATE);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
    }

    private void showHome() {
        enterArcadeFullscreen();
        showingTraining = false;
        awaitingServiceStart = false;
        timerView = null;
        TimerState saved = TimerState.load(this);
        homeView = new ArcadeHomeView(this,
                TimerState.format(saved.trainingSeconds),
                TimerState.format(saved.restSeconds),
                String.valueOf(saved.roundLimit),
                String.valueOf(saved.reminderSeconds));
        homeView.setOnStartClickListener(v -> beginTraining());
        setContentView(homeView);
        homeView.scrollTop();
    }

    private void beginTraining() {
        int training = TimerState.parseDuration(homeView.trainingText());
        int rest = TimerState.parseDuration(homeView.restText());
        int rounds = parseNumber(homeView.roundsText());
        int reminder = parseReminder(homeView.reminderText());
        if (training < 1 || rest < 1) {
            Toast.makeText(this, R.string.invalid_duration, Toast.LENGTH_LONG).show();
            return;
        }
        if (rounds < 0 || rounds > 99) {
            Toast.makeText(this, R.string.invalid_rounds, Toast.LENGTH_LONG).show();
            return;
        }
        if (reminder < 0 || reminder > 10) {
            Toast.makeText(this, R.string.invalid_reminder, Toast.LENGTH_LONG).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
        Intent intent = new Intent(this, TimerService.class).setAction(TimerService.ACTION_START)
                .putExtra(TimerService.EXTRA_TRAINING, training)
                .putExtra(TimerService.EXTRA_REST, rest)
                .putExtra(TimerService.EXTRA_REMINDER, reminder)
                .putExtra(TimerService.EXTRA_ROUNDS, rounds);
        awaitingServiceStart = true;
        showTraining(training, rounds == 0 ? "∞" : String.valueOf(rounds));
        if (!startTimerServiceSafely(intent)) {
            awaitingServiceStart = false;
            showHome();
            Toast.makeText(this, R.string.timer_start_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void showTraining(Integer initialTrainingSeconds, String initialRoundLimit) {
        enterArcadeFullscreen();
        showingTraining = true;
        homeView = null;
        timerView = new ArcadeTimerView(this);
        timerView.setOnToggleClickListener(v -> sendAction(TimerService.ACTION_TOGGLE));
        timerView.setOnEndClickListener(v -> endTrainingAndReturnHome());
        setContentView(timerView);
        timerView.scrollTop();
        if (initialTrainingSeconds == null) {
            renderFromState();
        } else {
            timerView.render(1, initialRoundLimit == null ? "∞" : initialRoundLimit,
                    TimerState.format(initialTrainingSeconds),
                    getString(R.string.phase_training_running), true, true);
        }
    }

    private void renderFromState() {
        if (!showingTraining || timerView == null) return;
        TimerState state = TimerState.load(this);
        if (!state.active) {
            if (state.completedNaturally) showCompletionDialog();
            else if (!awaitingServiceStart) showHome();
            return;
        }
        awaitingServiceStart = false;
        boolean training = TimerState.PHASE_TRAINING.equals(state.phase);
        String phaseLabel = state.running
                ? getString(training ? R.string.phase_training_running : R.string.phase_rest_running)
                : getString(R.string.paused_status);
        timerView.render(state.currentRound,
                state.roundLimit == 0 ? "∞" : String.valueOf(state.roundLimit),
                TimerState.format(state.remainingSeconds(TimerState.now())),
                phaseLabel,
                state.running,
                training);
    }

    private void showCompletionDialog() {
        if (completionDialogShown || isFinishing()) return;
        completionDialogShown = true;
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(R.string.training_complete)
                .setMessage(R.string.training_complete_message)
                .setCancelable(false)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    TimerState state = TimerState.load(this);
                    state.acknowledgeCompletion();
                    state.save(this);
                    completionDialogShown = false;
                    showHome();
                })
                .show();
    }

    private int parseNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            return -1;
        }
    }

    private int parseReminder(String value) {
        if (value == null) return -1;
        String trimmed = value.trim();
        if (trimmed.matches("\\d{2}:\\d{2}")) {
            int minutes = Integer.parseInt(trimmed.substring(0, 2));
            int seconds = Integer.parseInt(trimmed.substring(3, 5));
            if (minutes != 0 || seconds > 59) return -1;
            return seconds;
        }
        return parseNumber(trimmed);
    }

    private boolean startTimerServiceSafely(Intent explicit) {
        try {
            startForegroundService(explicit == null ? new Intent(this, TimerService.class) : explicit);
            return true;
        } catch (RuntimeException error) {
            Log.e(TAG, "Unable to start timer foreground service", error);
            return false;
        }
    }

    private boolean sendAction(String action) {
        boolean sent = startTimerServiceSafely(new Intent(this, TimerService.class).setAction(action));
        if (!sent) Toast.makeText(this, R.string.timer_action_failed, Toast.LENGTH_LONG).show();
        return sent;
    }

    private void enterArcadeFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void endTrainingAndReturnHome() {
        awaitingServiceStart = false;
        if (!sendAction(TimerService.ACTION_END)) {
            TimerState state = TimerState.load(this);
            state.end();
            state.save(this);
        }
        showHome();
    }
}
