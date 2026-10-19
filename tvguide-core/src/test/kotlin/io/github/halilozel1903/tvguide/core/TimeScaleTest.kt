package io.github.halilozel1903.tvguide.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TimeScaleTest {
    private val scale = TimeScale(origin = at(0), pxPerMinute = 4f)

    @Test
    fun timeToPixel() {
        assertEquals(0f, scale.xOf(at(0)))
        assertEquals(240f, scale.xOf(at(1)))
        assertEquals(4f * 90, scale.xOf(at(1, 30)))
        assertEquals(-120f, scale.xOf(at(0) - 30 * TimeSpan.MINUTE))
    }

    @Test
    fun pixelToTimeRoundTrips() {
        for (minute in listOf(0, 1, 59, 61, 600, 1439)) {
            val t = at(0, minute)
            assertEquals(t, scale.instantAt(scale.xOf(t)))
        }
        assertEquals(at(0, 0) + 15_000, scale.instantAt(1f))
    }

    @Test
    fun widths() {
        assertEquals(120f, scale.widthOf(30 * TimeSpan.MINUTE))
        assertEquals(4f * 1440, scale.widthOf(DAY))
    }

    @Test
    fun spanOfViewport() {
        assertEquals(span("01:00", "03:00"), scale.spanOf(offset = 240f, width = 480f))
    }

    @Test
    fun rejectsZeroScale() {
        assertFailsWith<IllegalArgumentException> { TimeScale(0, 0f) }
    }
}
