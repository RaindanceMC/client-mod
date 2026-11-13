package de.raindancer.hyades.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MotionTest {

    @Test
    void easingStartsFastEndsSlowAndStaysInsideItsEnds() {
        assertThat(Motion.easeOut(0)).isZero();
        assertThat(Motion.easeOut(1)).isEqualTo(1);
        assertThat(Motion.easeOut(0.5f)).isGreaterThan(0.5f);
        assertThat(Motion.easeOut(-3)).isZero();
        assertThat(Motion.easeOut(7)).isEqualTo(1);
    }

    @Test
    void fadingScalesOnlyTheAlpha() {
        assertThat(Colours.fade(0xFF6EC1EA, 1)).isEqualTo(0xFF6EC1EA);
        assertThat(Colours.fade(0xFF6EC1EA, 0.5f)).isEqualTo(0x806EC1EA);
        assertThat(Colours.fade(0x806EC1EA, 0.5f)).isEqualTo(0x406EC1EA);
        assertThat(Colours.fade(0xFF6EC1EA, 0)).isEqualTo(0x006EC1EA);
    }
}
