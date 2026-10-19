package io.github.halilozel1903.tvguide.core

/** Where a programme is relative to now. */
public enum class AirStatus {
    /** It has ended. */
    Past,

    /** It is on now. */
    Live,

    /** It has not started yet. */
    Upcoming,
}

/** Status, progress and remaining time of a programme at a given moment. */
public object Airing {

    /** [AirStatus.Live] while [now] is in `[start, end)`. */
    public fun status(programme: TimeSpan, now: Long): AirStatus = when {
        now >= programme.end -> AirStatus.Past
        now >= programme.start -> AirStatus.Live
        else -> AirStatus.Upcoming
    }

    /** How much has been on, from 0 (not started) to 1 (ended). An empty programme is 0 before it and 1 after. */
    public fun progress(programme: TimeSpan, now: Long): Float {
        if (programme.isEmpty) return if (now >= programme.end) 1f else 0f
        return ((now - programme.start).toFloat() / programme.durationMillis).coerceIn(0f, 1f)
    }

    /** Milliseconds until the programme ends, 0 once it has ended. */
    public fun remainingMillis(programme: TimeSpan, now: Long): Long = (programme.end - now).coerceIn(0L, programme.durationMillis)

    /** Milliseconds until the programme starts, 0 once it has started. */
    public fun startsInMillis(programme: TimeSpan, now: Long): Long = (programme.start - now).coerceAtLeast(0L)
}
