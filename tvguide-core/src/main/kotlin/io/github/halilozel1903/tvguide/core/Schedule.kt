package io.github.halilozel1903.tvguide.core

/**
 * Lookups in one channel's schedule: a list of programme spans sorted by start time that do not overlap (gaps are
 * fine). All lookups are binary searches, so a row with hundreds of programmes costs a handful of comparisons.
 */
public object Schedule {

    /** `true` when [spans] are sorted by start and no programme overlaps the next one. */
    public fun isOrdered(spans: List<TimeSpan>): Boolean =
        (1 until spans.size).all { spans[it - 1].end <= spans[it].start }

    /** Index of the programme on at [instant], or -1 when the row has a gap or nothing there. */
    public fun indexAt(spans: List<TimeSpan>, instant: Long): Int {
        var low = 0
        var high = spans.size - 1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val span = spans[mid]
            when {
                instant < span.start -> high = mid - 1
                instant >= span.end -> low = mid + 1
                else -> return mid
            }
        }
        return -1
    }

    /**
     * Indices of the programmes that overlap [visible], as a range; empty when none do. Used to compose only the
     * cells that are on screen.
     */
    public fun visibleRange(spans: List<TimeSpan>, visible: TimeSpan): IntRange {
        // First programme that ends after the visible start.
        var low = 0
        var high = spans.size
        while (low < high) {
            val mid = (low + high) ushr 1
            if (spans[mid].end <= visible.start) low = mid + 1 else high = mid
        }
        val first = low
        var last = first - 1
        var i = first
        while (i < spans.size && spans[i].start < visible.end) {
            last = i
            i++
        }
        return if (last < first) IntRange.EMPTY else first..last
    }

    /**
     * Index of the programme closest to [instant] among those that overlap [within]: the one on at [instant] if
     * there is one, otherwise the nearest by distance (the earlier one on a tie). Empty programmes are skipped.
     * Returns -1 when no programme overlaps [within].
     */
    public fun nearestIndex(spans: List<TimeSpan>, instant: Long, within: TimeSpan): Int {
        val range = visibleRange(spans, within)
        if (range.isEmpty()) return -1
        val target = within.clamp(instant).coerceAtMost(within.end - 1).coerceAtLeast(within.start)
        var best = -1
        var bestDistance = Long.MAX_VALUE
        for (i in range) {
            val span = spans[i]
            if (span.isEmpty) continue
            val distance = when {
                target < span.start -> span.start - target
                target >= span.end -> target - span.end + 1
                else -> 0L
            }
            if (distance < bestDistance) {
                best = i
                bestDistance = distance
                if (distance == 0L) break
            }
        }
        return best
    }
}
