package io.github.halilozel1903.tvguide.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleTest {
    // A gap from 21:00 to 21:30.
    private val row = listOf(
        span("18:00", "19:00"),
        span("19:00", "19:30"),
        span("19:30", "21:00"),
        span("21:30", "23:00"),
        span("23:00", "24:00"),
    )

    @Test
    fun ordered() {
        assertTrue(Schedule.isOrdered(row))
        assertTrue(Schedule.isOrdered(emptyList()))
        assertFalse(Schedule.isOrdered(listOf(span("19:00", "20:00"), span("18:00", "19:00"))))
        assertFalse(Schedule.isOrdered(listOf(span("18:00", "19:30"), span("19:00", "20:00"))))
    }

    @Test
    fun indexAt() {
        assertEquals(0, Schedule.indexAt(row, at(18)))
        assertEquals(1, Schedule.indexAt(row, at(19)))
        assertEquals(2, Schedule.indexAt(row, at(20, 59)))
        assertEquals(-1, Schedule.indexAt(row, at(21, 10)))
        assertEquals(4, Schedule.indexAt(row, at(23, 59)))
        assertEquals(-1, Schedule.indexAt(row, at(17)))
        assertEquals(-1, Schedule.indexAt(emptyList(), at(17)))
    }

    @Test
    fun visibleRange() {
        assertEquals(1..2, Schedule.visibleRange(row, span("19:10", "20:00")))
        assertEquals(2..3, Schedule.visibleRange(row, span("20:00", "22:00")))
        // A programme ending exactly at the visible start is not visible.
        assertEquals(1..1, Schedule.visibleRange(row, span("19:00", "19:30")))
        assertEquals(0..4, Schedule.visibleRange(row, DAY))
        assertTrue(Schedule.visibleRange(row, span("21:05", "21:20")).isEmpty())
        assertTrue(Schedule.visibleRange(row, span("06:00", "07:00")).isEmpty())
        assertTrue(Schedule.visibleRange(emptyList(), DAY).isEmpty())
    }

    @Test
    fun nearestIndex() {
        assertEquals(2, Schedule.nearestIndex(row, at(20), DAY))
        // In the gap: 21:10 is 10 min after the end of #2 and 20 min before #3.
        assertEquals(2, Schedule.nearestIndex(row, at(21, 10), DAY))
        assertEquals(3, Schedule.nearestIndex(row, at(21, 25), DAY))
        // Before everything.
        assertEquals(0, Schedule.nearestIndex(row, at(9), DAY))
        // Limited to a window: #0 is outside, so the nearest inside is #1.
        assertEquals(1, Schedule.nearestIndex(row, at(18, 30), span("19:00", "24:00")))
        assertEquals(-1, Schedule.nearestIndex(row, at(9), span("06:00", "12:00")))
    }
}
