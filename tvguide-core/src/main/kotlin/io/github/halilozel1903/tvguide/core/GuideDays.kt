package io.github.halilozel1903.tvguide.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** How a guide day relates to today, for labels such as "Today" and "Tomorrow". */
public enum class DayRelation { Yesterday, Today, Tomorrow, Other }

/**
 * Broadcast days. A guide day can start at an hour other than midnight ([startHour]), as many broadcasters do at
 * 06:00: the programme at 02:00 then belongs to the previous day.
 */
public object GuideDays {

    /** The window of [date] in [zone]: from [startHour] on that date to [startHour] the next day (23 or 25 hours on a daylight saving change). */
    public fun window(date: LocalDate, zone: ZoneId, startHour: Int = 0): TimeSpan {
        require(startHour in 0..23) { "startHour must be 0..23, was $startHour" }
        val time = LocalTime.of(startHour, 0)
        val start = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atTime(time).atZone(zone).toInstant().toEpochMilli()
        return TimeSpan(start, end)
    }

    /** The guide day [instant] belongs to. */
    public fun dayOf(instant: Long, zone: ZoneId, startHour: Int = 0): LocalDate {
        val local = Instant.ofEpochMilli(instant).atZone(zone)
        return if (local.hour < startHour) local.toLocalDate().minusDays(1) else local.toLocalDate()
    }

    /** [before] days before [today], [today] and [after] days after it, in order. */
    public fun range(today: LocalDate, before: Int, after: Int): List<LocalDate> {
        require(before >= 0 && after >= 0) { "before and after must not be negative" }
        return (-before..after).map { today.plusDays(it.toLong()) }
    }

    /** Whether [date] is yesterday, today, tomorrow or another day relative to [today]. */
    public fun relation(date: LocalDate, today: LocalDate): DayRelation = when (date) {
        today -> DayRelation.Today
        today.plusDays(1) -> DayRelation.Tomorrow
        today.minusDays(1) -> DayRelation.Yesterday
        else -> DayRelation.Other
    }
}
