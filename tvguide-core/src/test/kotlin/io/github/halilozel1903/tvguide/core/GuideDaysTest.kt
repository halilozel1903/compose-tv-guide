package io.github.halilozel1903.tvguide.core

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class GuideDaysTest {
    private val day = LocalDate.of(2026, 10, 13)

    @Test
    fun midnightWindow() {
        assertEquals(DAY, GuideDays.window(day, ZoneOffset.UTC))
        assertEquals(24 * TimeSpan.HOUR, GuideDays.window(day, ZoneOffset.UTC).durationMillis)
    }

    @Test
    fun broadcastDayStartingAtSix() {
        val window = GuideDays.window(day, ZoneOffset.UTC, startHour = 6)
        assertEquals(TimeSpan(at(6), at(30)), window)
        // 02:00 on the 14th still belongs to the 13th.
        assertEquals(day, GuideDays.dayOf(at(26), ZoneOffset.UTC, startHour = 6))
        assertEquals(day.plusDays(1), GuideDays.dayOf(at(30), ZoneOffset.UTC, startHour = 6))
        assertEquals(day, GuideDays.dayOf(at(0), ZoneOffset.UTC))
    }

    @Test
    fun daylightSavingDays() {
        val berlin = ZoneId.of("Europe/Berlin")
        assertEquals(25 * TimeSpan.HOUR, GuideDays.window(LocalDate.of(2026, 10, 25), berlin).durationMillis)
        assertEquals(23 * TimeSpan.HOUR, GuideDays.window(LocalDate.of(2026, 3, 29), berlin).durationMillis)
    }

    @Test
    fun rangeAndRelation() {
        val days = GuideDays.range(day, before = 1, after = 2)
        assertEquals(listOf(day.minusDays(1), day, day.plusDays(1), day.plusDays(2)), days)
        assertEquals(
            listOf(DayRelation.Yesterday, DayRelation.Today, DayRelation.Tomorrow, DayRelation.Other),
            days.map { GuideDays.relation(it, day) },
        )
    }
}
