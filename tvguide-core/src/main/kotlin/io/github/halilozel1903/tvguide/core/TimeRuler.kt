package io.github.halilozel1903.tvguide.core

import java.time.Instant
import java.time.ZoneId

/** Tick marks for the time ruler above the grid. */
public object TimeRuler {

    /**
     * Instants in `[span.start, span.end)` that fall on a multiple of [stepMinutes] in local time in [zone]: with a
     * step of 30 that is every :00 and :30 on the wall clock, also in zones with a half hour offset. The step must
     * divide a day (15, 30, 60, ...).
     */
    public fun ticks(span: TimeSpan, stepMinutes: Int, zone: ZoneId): List<Long> {
        require(stepMinutes > 0 && (24 * 60) % stepMinutes == 0) { "stepMinutes must divide a day, was $stepMinutes" }
        val stepMillis = stepMinutes * TimeSpan.MINUTE
        val result = ArrayList<Long>()
        var t = alignUp(span.start, stepMinutes, zone)
        while (t < span.end) {
            result += t
            // Re-align after each step so a daylight saving change does not shift the rest of the ruler.
            t = alignUp(t + stepMillis, stepMinutes, zone)
        }
        return result
    }

    /** The first instant at or after [instant] that is on a multiple of [stepMinutes] in local time. */
    public fun alignUp(instant: Long, stepMinutes: Int, zone: ZoneId): Long {
        val local = Instant.ofEpochMilli(instant).atZone(zone)
        val minuteOfDay = local.hour * 60 + local.minute
        val hasRemainder = local.second != 0 || local.nano != 0
        val remainder = minuteOfDay % stepMinutes
        if (remainder == 0 && !hasRemainder) return instant
        val add = (stepMinutes - remainder).toLong()
        val aligned = local.withSecond(0).withNano(0).plusMinutes(add)
        return aligned.toInstant().toEpochMilli()
    }
}
