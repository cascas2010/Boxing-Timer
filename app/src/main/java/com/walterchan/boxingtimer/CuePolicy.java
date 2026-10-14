package com.walterchan.boxingtimer;

/** Ensures a stage bell and a countdown cue never play for the same displayed second. */
final class CuePolicy {
    private int lastCueSecond = -1;
    private String lastCuePhase = "";

    void onStageBell(String phase, int displayedSecond) {
        lastCuePhase = phase;
        lastCueSecond = displayedSecond;
    }

    boolean shouldPlayCountdown(boolean running, String phase, int displayedSecond, int reminderSeconds) {
        if (!running || displayedSecond <= 0 || displayedSecond > reminderSeconds) return false;
        if (displayedSecond == lastCueSecond && phase.equals(lastCuePhase)) return false;
        lastCuePhase = phase;
        lastCueSecond = displayedSecond;
        return true;
    }
}
