package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** An explicit choice on the TV wins; otherwise Apple TV Mode follows the selected theme. */
public class AppleTvModeTest {
    @Test
    public void savedOnChoiceOverridesThemeOff() {
        assertTrue(Config.resolveAppleTvMode(true, true, false));
    }

    @Test
    public void savedOffChoiceOverridesThemeOn() {
        assertFalse(Config.resolveAppleTvMode(true, false, true));
    }

    @Test
    public void noSavedChoiceFollowsTheme() {
        assertTrue(Config.resolveAppleTvMode(false, false, true));
        assertFalse(Config.resolveAppleTvMode(false, true, false));
    }
}
