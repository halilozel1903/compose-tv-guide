package io.github.halilozel1903.tvguide.core

import kotlin.test.Test
import kotlin.test.assertEquals

class AiringTest {
    private val show = span("20:00", "21:00")

    @Test
    fun status() {
        assertEquals(AirStatus.Upcoming, Airing.status(show, at(19, 59)))
        assertEquals(AirStatus.Live, Airing.status(show, at(20)))
        assertEquals(AirStatus.Live, Airing.status(show, at(20, 59)))
        assertEquals(AirStatus.Past, Airing.status(show, at(21)))
    }

    @Test
    fun progress() {
        assertEquals(0f, Airing.progress(show, at(19)))
        assertEquals(0.25f, Airing.progress(show, at(20, 15)))
        assertEquals(1f, Airing.progress(show, at(22)))
        assertEquals(0f, Airing.progress(span("20:00", "20:00"), at(19)))
        assertEquals(1f, Airing.progress(span("20:00", "20:00"), at(20)))
    }

    @Test
    fun remainingAndStartsIn() {
        assertEquals(45 * TimeSpan.MINUTE, Airing.remainingMillis(show, at(20, 15)))
        assertEquals(60 * TimeSpan.MINUTE, Airing.remainingMillis(show, at(18)))
        assertEquals(0L, Airing.remainingMillis(show, at(22)))
        assertEquals(90 * TimeSpan.MINUTE, Airing.startsInMillis(show, at(18, 30)))
        assertEquals(0L, Airing.startsInMillis(show, at(20, 30)))
    }

    @Test
    fun durationText() {
        assertEquals("45 min", DurationText.format(45 * TimeSpan.MINUTE))
        assertEquals("2 h", DurationText.format(120 * TimeSpan.MINUTE))
        assertEquals("1 h 5 min", DurationText.format(65 * TimeSpan.MINUTE))
        assertEquals("1 min", DurationText.format(20_000))
        assertEquals("0 min", DurationText.format(-5))
        assertEquals("1 Std 30 Min", DurationText.format(90 * TimeSpan.MINUTE, hourUnit = "Std", minuteUnit = "Min"))
    }
}
