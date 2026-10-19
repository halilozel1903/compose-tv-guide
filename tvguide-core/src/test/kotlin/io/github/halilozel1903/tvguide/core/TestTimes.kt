package io.github.halilozel1903.tvguide.core

import java.time.LocalDateTime
import java.time.ZoneOffset

/** 2026-10-13 00:00 UTC, the start of the test day. */
internal val DAY_START: Long = LocalDateTime.of(2026, 10, 13, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()

/** The instant [hour]:[minute] on the test day (UTC). */
internal fun at(hour: Int, minute: Int = 0): Long = DAY_START + hour * TimeSpan.HOUR + minute * TimeSpan.MINUTE

/** A span from [from] to [to], each "HH:MM" on the test day; "24:00" is the end of the day. */
internal fun span(from: String, to: String): TimeSpan = TimeSpan(parse(from), parse(to))

private fun parse(text: String): Long {
    val (h, m) = text.split(":").map { it.toInt() }
    return at(h, m)
}

internal val DAY: TimeSpan = TimeSpan(at(0), at(24))
