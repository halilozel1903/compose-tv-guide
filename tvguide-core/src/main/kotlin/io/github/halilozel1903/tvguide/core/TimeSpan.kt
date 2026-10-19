package io.github.halilozel1903.tvguide.core

/**
 * A half open stretch of time, `[start, end)`, in epoch milliseconds: a programme, a guide window or the part of a
 * guide that is on screen.
 */
public data class TimeSpan(val start: Long, val end: Long) {
    init {
        require(end >= start) { "end ($end) is before start ($start)" }
    }

    /** Length in milliseconds. */
    val durationMillis: Long get() = end - start

    /** Length in whole minutes, rounded down. */
    val durationMinutes: Long get() = durationMillis / MINUTE

    /** `true` for a span of no length. */
    val isEmpty: Boolean get() = end == start

    /** `true` when [instant] is in `[start, end)`. */
    public operator fun contains(instant: Long): Boolean = instant >= start && instant < end

    /** `true` when the two spans share some time. Touching spans (one ends when the other starts) do not overlap. */
    public fun overlaps(other: TimeSpan): Boolean = start < other.end && other.start < end

    /** The shared part of the two spans, or `null` when they do not overlap. */
    public fun intersect(other: TimeSpan): TimeSpan? =
        if (overlaps(other)) TimeSpan(maxOf(start, other.start), minOf(end, other.end)) else null

    /** [instant] moved into `[start, end]`. */
    public fun clamp(instant: Long): Long = instant.coerceIn(start, end)

    /** The same length starting [millis] later (earlier when negative). */
    public fun shift(millis: Long): TimeSpan = TimeSpan(start + millis, end + millis)

    public companion object {
        public const val MINUTE: Long = 60_000L
        public const val HOUR: Long = 60 * MINUTE

        /** A span of [minutes] minutes starting at [start]. */
        public fun ofMinutes(start: Long, minutes: Long): TimeSpan = TimeSpan(start, start + minutes * MINUTE)
    }
}
