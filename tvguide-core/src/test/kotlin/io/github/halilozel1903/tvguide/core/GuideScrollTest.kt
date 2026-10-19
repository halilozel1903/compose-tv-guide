package io.github.halilozel1903.tvguide.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GuideScrollTest {
    private val content = 5000f
    private val viewport = 1000f

    @Test
    fun alreadyVisibleDoesNotScroll() {
        assertEquals(300f, GuideScroll.keepVisible(400f, 700f, offset = 300f, viewport, content, padding = 50f))
    }

    @Test
    fun scrollsRightJustEnough() {
        // End 1400 + padding 50 must be <= offset + 1000.
        assertEquals(450f, GuideScroll.keepVisible(1200f, 1400f, offset = 300f, viewport, content, padding = 50f))
    }

    @Test
    fun scrollsLeftJustEnough() {
        assertEquals(150f, GuideScroll.keepVisible(200f, 400f, offset = 300f, viewport, content, padding = 50f))
    }

    @Test
    fun neverPastTheContent() {
        assertEquals(0f, GuideScroll.keepVisible(10f, 100f, offset = 300f, viewport, content, padding = 50f))
        assertEquals(4000f, GuideScroll.keepVisible(4900f, 5000f, offset = 300f, viewport, content, padding = 50f))
    }

    @Test
    fun wideItemsNeedHalfTheViewport() {
        // A 3000 px programme showing 1000..1500 at offset 500 (450 px of the 900 inside the padding): enough.
        assertEquals(500f, GuideScroll.keepVisible(1000f, 4000f, offset = 500f, viewport, content, padding = 50f))
        // Starting at 1300: only 150 px show, so scroll until 450 do.
        assertEquals(800f, GuideScroll.keepVisible(1300f, 4300f, offset = 500f, viewport, content, padding = 50f))
        // Ending at 700 while scrolled to 500 (150 px shown): scroll left until 450 px of its end show.
        assertEquals(200f, GuideScroll.keepVisible(-2000f, 700f, offset = 500f, viewport, content, padding = 50f))
    }

    @Test
    fun align() {
        assertEquals(1750f, GuideScroll.align(2000f, viewport, content, fraction = 0.25f))
        assertEquals(0f, GuideScroll.align(100f, viewport, content, fraction = 0.25f))
        assertEquals(4000f, GuideScroll.align(4990f, viewport, content, fraction = 0f))
    }

    @Test
    fun clampWhenContentIsSmallerThanTheViewport() {
        assertEquals(0f, GuideScroll.clamp(200f, contentSize = 500f, viewportSize = viewport))
        assertEquals(0f, GuideScroll.maxOffset(500f, viewport))
    }

    @Test
    fun visibleRows() {
        // Rows 100 px apart, viewport 350 px.
        assertEquals(0..3, GuideScroll.visibleRows(0f, 350f, 100f, rowCount = 20))
        assertEquals(1..4, GuideScroll.visibleRows(150f, 350f, 100f, rowCount = 20))
        assertEquals(0..5, GuideScroll.visibleRows(150f, 350f, 100f, rowCount = 20, overscan = 1))
        assertEquals(18..19, GuideScroll.visibleRows(1850f, 350f, 100f, rowCount = 20))
        assertTrue(GuideScroll.visibleRows(0f, 350f, 100f, rowCount = 0).isEmpty())
        assertTrue(GuideScroll.visibleRows(0f, 0f, 100f, rowCount = 5).isEmpty())
    }
}
