package com.walterchan.boxingtimer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimerStateTest {
    @Test public void durationValidationAcceptsFullRange() {
        assertEquals(1, TimerState.parseDuration("00:01"));
        assertEquals(3599, TimerState.parseDuration("59:59"));
        assertEquals(-1, TimerState.parseDuration("00:00"));
        assertEquals(-1, TimerState.parseDuration("60:00"));
        assertEquals(-1, TimerState.parseDuration("1:00"));
    }

    @Test public void pauseAndResumeKeepRemainingTime() {
        TimerState state = new TimerState();
        state.begin(180, 60, 10, 0, 1000L);
        state.toggle(31000L);
        assertFalse(state.running);
        assertEquals(150000L, state.pausedRemainingMs);
        state.toggle(50000L);
        assertTrue(state.running);
        assertEquals(200000L, state.deadlineMs);
    }

    @Test public void delayedTickCatchesUpAcrossSeveralPhases() {
        TimerState state = new TimerState();
        state.begin(3, 2, 1, 0, 1000L);
        assertEquals(3, state.advance(9000L));
        assertEquals(TimerState.PHASE_REST, state.phase);
        assertEquals(11000L, state.deadlineMs);
        assertEquals(2, state.remainingSeconds(9000L));
    }

    @Test public void endingStopsTimerAndKeepsLatestSettings() {
        TimerState state = new TimerState();
        state.begin(10, 5, 0, 8, 1000L);
        state.end();

        assertFalse(state.active);
        assertFalse(state.running);
        assertEquals(10, state.trainingSeconds);
        assertEquals(5, state.restSeconds);
        assertEquals(0, state.reminderSeconds);
        assertEquals(8, state.roundLimit);
    }

    @Test public void finiteTrainingCompletesAfterLastRoundsRest() {
        TimerState state = new TimerState();
        state.begin(3, 2, 1, 2, 1000L);

        assertEquals(1, state.advance(4000L));
        assertEquals(TimerState.PHASE_REST, state.phase);
        assertEquals(1, state.currentRound);
        assertEquals(1, state.advance(6000L));
        assertEquals(TimerState.PHASE_TRAINING, state.phase);
        assertEquals(2, state.currentRound);
        assertEquals(1, state.advance(9000L));
        assertEquals(TimerState.PHASE_REST, state.phase);
        assertEquals(1, state.advance(11000L));
        assertFalse(state.active);
        assertTrue(state.completedNaturally);
    }

    @Test public void zeroRoundsKeepsLooping() {
        TimerState state = new TimerState();
        state.begin(1, 1, 0, 0, 0L);

        assertEquals(5, state.advance(5000L));
        assertTrue(state.active);
        assertEquals(3, state.currentRound);
    }
}
