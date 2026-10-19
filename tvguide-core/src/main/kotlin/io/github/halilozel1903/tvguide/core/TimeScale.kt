package io.github.halilozel1903.tvguide.core

import kotlin.math.roundToLong

/**
 * Maps time to horizontal pixels: [origin] (usually the start of the guide window) is at x = 0 and every minute is
 * [pxPerMinute] wide.
 */
public data class TimeScale(val origin: Long, val pxPerMinute: Float) {
    init {
        require(pxPerMinute > 0f) { "pxPerMinute must be positive, was $pxPerMinute" }
    }

    /** The x position of [instant]. Instants before [origin] are negative. */
    public fun xOf(instant: Long): Float = (instant - origin).toFloat() / TimeSpan.MINUTE * pxPerMinute

    /** The width of a stretch of [durationMillis]. */
    public fun widthOf(durationMillis: Long): Float = durationMillis.toFloat() / TimeSpan.MINUTE * pxPerMinute

    /** The instant at x position [x], to the nearest millisecond. */
    public fun instantAt(x: Float): Long = origin + (x.toDouble() / pxPerMinute * TimeSpan.MINUTE).roundToLong()

    /** The time on screen for a viewport that starts at [offset] and is [width] wide. */
    public fun spanOf(offset: Float, width: Float): TimeSpan {
        val start = instantAt(offset)
        return TimeSpan(start, maxOf(start, instantAt(offset + width)))
    }

    /** The full width of [window] on this scale. */
    public fun widthOf(window: TimeSpan): Float = widthOf(window.durationMillis)
}
