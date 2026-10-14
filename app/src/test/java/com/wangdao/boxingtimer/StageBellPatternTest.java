package com.walterchan.boxingtimer;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StageBellPatternTest {
    @Test public void stageBoundarySchedulesExactlyOneFullStrike() {
        assertEquals(1, StageBellPattern.STRIKE_COUNT);
        assertArrayEquals(new long[]{0L}, StageBellPattern.strikeDelaysMs());
        assertEquals(1176L, StageBellPattern.groupDurationMs());
    }
}
