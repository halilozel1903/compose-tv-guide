package io.github.halilozel1903.tvguide.core

/**
 * Where a programme cell goes in its row, in pixels from the start of the guide window.
 *
 * @property x left edge of the cell.
 * @property width width of the cell, after the gap between cells. Never negative.
 * @property visible the part of the programme inside the guide window.
 * @property clippedStart the programme started before the window (it began on the previous day, for example).
 * @property clippedEnd the programme ends after the window.
 */
public data class CellGeometry(
    val x: Float,
    val width: Float,
    val visible: TimeSpan,
    val clippedStart: Boolean,
    val clippedEnd: Boolean,
) {
    /** Right edge of the cell. */
    val right: Float get() = x + width
}

/** Places programme cells in a row and keeps their text readable when they are cut by the edge of the screen. */
public object CellLayout {

    /**
     * Places [programme] in a row of a guide showing [window] on [scale]. A programme that sticks out of the window
     * is cut at its edges and flagged with [CellGeometry.clippedStart] or [CellGeometry.clippedEnd]. Returns `null`
     * for a programme completely outside the window or with no length.
     *
     * [gap] is taken off the end of the cell so neighbours do not touch; a cell is never narrower than zero.
     */
    public fun place(programme: TimeSpan, window: TimeSpan, scale: TimeScale, gap: Float = 0f): CellGeometry? {
        val visible = programme.intersect(window) ?: return null
        if (visible.isEmpty) return null
        val x = scale.xOf(visible.start)
        val width = (scale.xOf(visible.end) - x - gap).coerceAtLeast(0f)
        return CellGeometry(
            x = x,
            width = width,
            visible = visible,
            clippedStart = programme.start < window.start,
            clippedEnd = programme.end > window.end,
        )
    }

    /**
     * How far to push the content of a cell to the right so its title stays on screen when the cell starts left of
     * the viewport ("sticky" titles). [viewportStart] is the scroll offset in the same coordinates as [cellX].
     *
     * The content is never pushed so far that less than [minContentWidth] of it would remain inside the cell, and a
     * cell that starts inside the viewport is not moved.
     */
    public fun stickyShift(cellX: Float, cellWidth: Float, viewportStart: Float, minContentWidth: Float): Float {
        val wanted = viewportStart - cellX
        if (wanted <= 0f) return 0f
        val limit = (cellWidth - minContentWidth).coerceAtLeast(0f)
        return wanted.coerceAtMost(limit)
    }
}
