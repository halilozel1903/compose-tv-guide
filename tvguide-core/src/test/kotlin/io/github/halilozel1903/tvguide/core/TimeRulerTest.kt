package io.github.halilozel1903.tvguide.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeRulerTest {
    @Test
    fun halfHourTicksInUtc() {
        val ticks = TimeRuler.ticks(span("18:10", "20:00"), 30, ZoneOffset.UTC)
        assertEquals(listOf(at(18, 30), at(19), at(19, 30)), ticks)
    }

    @Test
    fun startOnATickIsIncluded() {
        assertEquals(listOf(at(18), at(19)), TimeRuler.ticks(span("18:00", "19:30"), 60, ZoneOffset.UTC))
    }

    @Test
    fun hourTicksFollowTheLocalClockInHalfHourZones() {
        val india = ZoneId.of("Asia/Kolkata") // UTC+05:30
        // 18:00 UTC is 23:30 in India, so the first full local hour is 18:30 UTC.
        assertEquals(listOf(at(18, 30), at(19, 30)), TimeRuler.ticks(span("18:00", "20:00"), 60, india))
    }

    @Test
    fun ticksAcrossADaylightSavingChange() {
        val berlin = ZoneId.of("Europe/Berlin")
        val window = GuideDays.window(LocalDate.of(2026, 10, 25), berlin) // 25 hours
        val ticks = TimeRuler.ticks(window, 60, berlin)
        assertEquals(25, ticks.size)
        ticks.forEach { assertEquals(0, LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), berlin).minute) }
    }

    @Test
    fun alignUpDropsSeconds() {
        assertEquals(at(18, 30), TimeRuler.alignUp(at(18, 0) + 1_000, 30, ZoneOffset.UTC))
        assertEquals(at(18, 30), TimeRuler.alignUp(at(18, 30), 30, ZoneOffset.UTC))
    }
}
