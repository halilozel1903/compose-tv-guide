package io.github.halilozel1903.tvguide.core

/** Short English duration text for cells and details panels, such as "1 h 30 min". */
public object DurationText {

    /**
     * Formats [millis] in whole minutes, rounded up so a programme with 20 seconds left reads "1 min":
     * "45 min", "2 h", "1 h 5 min". Zero and negative durations read "0 min".
     */
    public fun format(millis: Long, hourUnit: String = "h", minuteUnit: String = "min"): String {
        if (millis <= 0L) return "0 $minuteUnit"
        val minutes = (millis + TimeSpan.MINUTE - 1) / TimeSpan.MINUTE
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0L -> "$m $minuteUnit"
            m == 0L -> "$h $hourUnit"
            else -> "$h $hourUnit $m $minuteUnit"
        }
    }
}
