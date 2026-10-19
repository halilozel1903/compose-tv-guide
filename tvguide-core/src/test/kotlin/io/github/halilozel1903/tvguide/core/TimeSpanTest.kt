package io.github.halilozel1903.tvguide.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeSpanTest {
    @Test
    fun containsIsHalfOpen() {
        val s = span("20:00", "21:00")
        assertTrue(at(20) in s)
        assertTrue(at(20, 59) in s)
        assertFalse(at(21) in s)
        assertFalse(at(19, 59) in s)
    }

    @Test
    fun touchingSpansDoNotOverlap() {
        assertFalse(span("20:00", "21:00").overlaps(span("21:00", "22:00")))
        assertTrue(span("20:00", "21:01").overlaps(span("21:00", "22:00")))
    }

    @Test
    fun intersect() {
        assertEquals(span("20:30", "21:00"), span("20:00", "21:00").intersect(span("20:30", "23:00")))
        assertNull(span("20:00", "21:00").intersect(span("22:00", "23:00")))
    }

    @Test
    fun durationAndShift() {
        val s = span("20:00", "21:30")
        assertEquals(90, s.durationMinutes)
        assertEquals(span("21:00", "22:30"), s.shift(TimeSpan.HOUR))
        assertEquals(TimeSpan.ofMinutes(at(20), 90), s)
    }

    @Test
    fun clamp() {
        val s = span("20:00", "21:00")
        assertEquals(at(20), s.clamp(at(19)))
        assertEquals(at(21), s.clamp(at(23)))
        assertEquals(at(20, 15), s.clamp(at(20, 15)))
    }

    @Test
    fun rejectsEndBeforeStart() {
        assertFailsWith<IllegalArgumentException> { TimeSpan(10, 5) }
    }
}
