package com.walterchan.boxingtimer;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CuePolicyTest {
    @Test public void stageBellSuppressesCountdownForSameDisplayedSecond() {
        CuePolicy policy = new CuePolicy();
        policy.onStageBell(TimerState.PHASE_TRAINING, 1);

        assertFalse(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 1, 10));
        assertFalse(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 1, 10));
    }

    @Test public void laterCountdownSecondPlaysAtMostOnce() {
        CuePolicy policy = new CuePolicy();
        policy.onStageBell(TimerState.PHASE_TRAINING, 10);

        assertTrue(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 9, 10));
        assertFalse(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 9, 10));
        assertTrue(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 8, 10));
    }

    @Test public void transitionBellWinsWhenPhaseChangesAtSameSecond() {
        CuePolicy policy = new CuePolicy();
        assertTrue(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 2, 10));

        policy.onStageBell(TimerState.PHASE_REST, 2);
        assertFalse(policy.shouldPlayCountdown(true, TimerState.PHASE_REST, 2, 10));
        assertTrue(policy.shouldPlayCountdown(true, TimerState.PHASE_REST, 1, 10));
    }

    @Test public void zeroReminderDisablesEveryCountdownCue() {
        CuePolicy policy = new CuePolicy();
        assertFalse(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 10, 0));
        assertFalse(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 1, 0));
        assertFalse(policy.shouldPlayCountdown(true, TimerState.PHASE_TRAINING, 0, 0));
    }
}
