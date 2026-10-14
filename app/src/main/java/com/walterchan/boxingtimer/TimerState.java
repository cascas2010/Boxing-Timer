package com.walterchan.boxingtimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

final class TimerState {
    static final String PREFS = "boxing_timer_state";
    static final String PHASE_TRAINING = "training";
    static final String PHASE_REST = "rest";

    boolean active;
    boolean running;
    String phase = PHASE_TRAINING;
    long deadlineMs;
    long pausedRemainingMs;
    int trainingSeconds = 180;
    int restSeconds = 60;
    int reminderSeconds = 10;
    int roundLimit;
    int currentRound = 1;
    boolean completedNaturally;

    static TimerState load(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        TimerState s = new TimerState();
        s.active = p.getBoolean("active", false);
        s.running = p.getBoolean("running", false);
        s.phase = p.getString("phase", PHASE_TRAINING);
        s.deadlineMs = p.getLong("deadline", 0L);
        s.pausedRemainingMs = p.getLong("remaining", s.trainingSeconds * 1000L);
        s.trainingSeconds = p.getInt("training_seconds", 180);
        s.restSeconds = p.getInt("rest_seconds", 60);
        s.reminderSeconds = p.getInt("reminder_seconds", 10);
        s.roundLimit = p.getInt("round_limit", 0);
        s.currentRound = p.getInt("current_round", 1);
        s.completedNaturally = p.getBoolean("completed_naturally", false);
        return s;
    }

    void save(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean("active", active)
                .putBoolean("running", running)
                .putString("phase", phase)
                .putLong("deadline", deadlineMs)
                .putLong("remaining", pausedRemainingMs)
                .putInt("training_seconds", trainingSeconds)
                .putInt("rest_seconds", restSeconds)
                .putInt("reminder_seconds", reminderSeconds)
                .putInt("round_limit", roundLimit)
                .putInt("current_round", currentRound)
                .putBoolean("completed_naturally", completedNaturally)
                .apply();
    }

    long remainingMs(long now) {
        return running ? Math.max(0L, deadlineMs - now) : Math.max(0L, pausedRemainingMs);
    }

    int remainingSeconds(long now) {
        long millis = remainingMs(now);
        return (int) Math.max(0L, (millis + 999L) / 1000L);
    }

    void begin(int training, int rest, int reminder, int rounds, long now) {
        active = true;
        running = true;
        phase = PHASE_TRAINING;
        trainingSeconds = training;
        restSeconds = rest;
        reminderSeconds = reminder;
        roundLimit = rounds;
        currentRound = 1;
        completedNaturally = false;
        pausedRemainingMs = training * 1000L;
        deadlineMs = now + pausedRemainingMs;
    }

    void toggle(long now) {
        if (!active) return;
        if (running) {
            pausedRemainingMs = Math.max(1L, deadlineMs - now);
            running = false;
        } else {
            deadlineMs = now + Math.max(1L, pausedRemainingMs);
            running = true;
        }
    }

    int advance(long now) {
        int transitions = 0;
        while (active && running && now >= deadlineMs) {
            if (PHASE_TRAINING.equals(phase)) {
                phase = PHASE_REST;
                deadlineMs += restSeconds * 1000L;
            } else if (roundLimit > 0 && currentRound >= roundLimit) {
                active = false;
                running = false;
                completedNaturally = true;
                deadlineMs = 0L;
                pausedRemainingMs = 0L;
            } else {
                currentRound++;
                phase = PHASE_TRAINING;
                deadlineMs += trainingSeconds * 1000L;
            }
            transitions++;
            if (transitions > 10000) {
                deadlineMs = now + (PHASE_TRAINING.equals(phase) ? trainingSeconds : restSeconds) * 1000L;
                break;
            }
        }
        return transitions;
    }

    void end() {
        active = false;
        running = false;
        deadlineMs = 0L;
        pausedRemainingMs = 0L;
        completedNaturally = false;
    }

    void acknowledgeCompletion() {
        completedNaturally = false;
    }

    static String format(int seconds) {
        return String.format(java.util.Locale.CHINA, "%02d:%02d", seconds / 60, seconds % 60);
    }

    static int parseDuration(String text) {
        if (text == null || !text.matches("\\d{2}:\\d{2}")) return -1;
        int minutes = Integer.parseInt(text.substring(0, 2));
        int seconds = Integer.parseInt(text.substring(3, 5));
        if (minutes > 59 || seconds > 59) return -1;
        int total = minutes * 60 + seconds;
        return total >= 1 && total <= 3599 ? total : -1;
    }

    static long now() { return SystemClock.elapsedRealtime(); }
}
