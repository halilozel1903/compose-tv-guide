package io.github.halilozel1903.tvguide

import io.github.halilozel1903.tvguide.core.GuideNavigator
import io.github.halilozel1903.tvguide.core.Schedule
import io.github.halilozel1903.tvguide.core.TimeScale
import io.github.halilozel1903.tvguide.core.TimeSpan

/** A programme cell: the channel row and the programme's key. */
internal data class GuideCellId(val row: Int, val key: Any)

/**
 * Everything the guide knows about its data and geometry for one composition. Rows are read lazily: a channel's
 * programmes are fetched, sorted when needed and turned into spans only when that row is shown or navigated through.
 */
internal class GuideModel<C, P>(
    val channels: List<C>,
    private val programmesOf: (C) -> List<P>,
    val programmeKey: (P) -> Any,
    private val programmeTime: (P) -> TimeSpan,
    val window: TimeSpan,
    val scale: TimeScale,
    val rowHeight: Float,
    val rowStride: Float,
    val cellGap: Float,
) {
    private val programmeCache = arrayOfNulls<List<P>>(channels.size)
    private val spanCache = arrayOfNulls<List<TimeSpan>>(channels.size)

    val rowCount: Int get() = channels.size

    val navigator: GuideNavigator = GuideNavigator(channels.size, window) { spans(it) }

    val contentWidth: Float = scale.widthOf(window)

    val contentHeight: Float = if (channels.isEmpty()) 0f else channels.size * rowStride - (rowStride - rowHeight)

    fun programmes(row: Int): List<P> {
        programmeCache[row]?.let { return it }
        val list = programmesOf(channels[row])
        val sorted = if (Schedule.isOrdered(list.map(programmeTime))) list else list.sortedBy { programmeTime(it).start }
        programmeCache[row] = sorted
        return sorted
    }

    fun spans(row: Int): List<TimeSpan> {
        spanCache[row]?.let { return it }
        val spans = programmes(row).map(programmeTime)
        spanCache[row] = spans
        return spans
    }

    fun keyAt(row: Int, index: Int): Any? = programmes(row).getOrNull(index)?.let(programmeKey)

    fun indexOf(row: Int, key: Any?): Int = if (key == null) -1 else programmes(row).indexOfFirst { programmeKey(it) == key }

    fun rowTop(row: Int): Float = row * rowStride
}
