package com.walterchan.boxingtimer;

/** Exact one-strike pattern for each stage boundary. */
final class StageBellPattern {
    static final int STRIKE_COUNT = 1;
    static final long SOUND_DURATION_MS = 1176L;

    private StageBellPattern() {}

    static long[] strikeDelaysMs() {
        long[] delays = new long[STRIKE_COUNT];
        for (int i = 0; i < STRIKE_COUNT; i++) delays[i] = 0L;
        return delays;
    }

    static long groupDurationMs() {
        return SOUND_DURATION_MS;
    }
}
