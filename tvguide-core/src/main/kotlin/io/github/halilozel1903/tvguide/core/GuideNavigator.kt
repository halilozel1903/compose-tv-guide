package io.github.halilozel1903.tvguide.core

/** A D-pad direction. */
public enum class GuideDirection { Left, Right, Up, Down }

/** A programme cell in the guide: the channel [row] and the programme's [index] in that channel's schedule. */
public data class GuidePosition(val row: Int, val index: Int)

/** The result of a D-pad press in the guide. */
public sealed interface GuideMove {
    /**
     * Focus moves to [position]. [anchor] is the time to remember for the next vertical move, so going up and down
     * stays in the same column of time even through short and long programmes.
     */
    public data class Moved(val position: GuidePosition, val anchor: Long) : GuideMove

    /** There is nothing further in [direction]: the guide's edge. Focus may leave the guide here. */
    public data class Edge(val direction: GuideDirection) : GuideMove
}

/**
 * Focus neighbour search for an EPG grid.
 *
 * - **Left and right** go to the previous and next programme in the same channel that is inside [window]. The anchor
 *   becomes the start of the new programme, or the start of the visible time when the programme began off screen.
 * - **Up and down** go to the nearest channel above or below that has something in [window] and pick the programme
 *   on at the anchor time, or the nearest one when there is a gap. The anchor is kept, so a column of time survives
 *   several vertical moves.
 *
 * [spans] returns a channel's programmes sorted by start time (see [Schedule]).
 */
public class GuideNavigator(
    private val rowCount: Int,
    private val window: TimeSpan,
    private val spans: (row: Int) -> List<TimeSpan>,
) {

    /**
     * Moves from [from] in [direction]. [anchor] is the remembered time column (see [GuideMove.Moved.anchor]) and
     * [visible] the time on screen, used to anchor programmes that start before it.
     */
    public fun move(
        from: GuidePosition,
        direction: GuideDirection,
        anchor: Long,
        visible: TimeSpan = window,
    ): GuideMove {
        if (from.row !in 0 until rowCount) {
            return positionAt(0, anchor)?.let { GuideMove.Moved(it, anchor) } ?: GuideMove.Edge(direction)
        }
        return when (direction) {
            GuideDirection.Left, GuideDirection.Right -> horizontal(from, direction, anchor, visible)
            GuideDirection.Up, GuideDirection.Down -> vertical(from, direction, anchor)
        }
    }

    /** The programme in [row] at [instant] (or the nearest one inside the window), or `null` for an empty row. */
    public fun positionAt(row: Int, instant: Long): GuidePosition? {
        if (row !in 0 until rowCount) return null
        val index = Schedule.nearestIndex(spans(row), instant, window)
        return if (index < 0) null else GuidePosition(row, index)
    }

    /**
     * The first row at or after [row] in the direction of [step] (+1 or -1) that has a programme inside the window
     * near [instant], or `null`.
     */
    public fun positionNear(row: Int, instant: Long, step: Int = 1): GuidePosition? {
        require(step == 1 || step == -1) { "step must be 1 or -1" }
        var r = row
        while (r in 0 until rowCount) {
            positionAt(r, instant)?.let { return it }
            r += step
        }
        return null
    }

    /** The anchor for a programme that has just been focused: its start, or the visible start if it began earlier. */
    public fun anchorFor(programme: TimeSpan, visible: TimeSpan = window): Long {
        val start = maxOf(programme.start, visible.start, window.start)
        return start.coerceAtMost(maxOf(programme.start, programme.end - 1))
    }

    private fun horizontal(from: GuidePosition, direction: GuideDirection, anchor: Long, visible: TimeSpan): GuideMove {
        val row = spans(from.row)
        if (from.index !in row.indices) {
            return positionAt(from.row, anchor)?.let { GuideMove.Moved(it, anchor) } ?: GuideMove.Edge(direction)
        }
        val step = if (direction == GuideDirection.Right) 1 else -1
        var i = from.index + step
        while (i in row.indices) {
            val span = row[i]
            if (!span.isEmpty && span.overlaps(window)) {
                return GuideMove.Moved(GuidePosition(from.row, i), anchorFor(span, visible))
            }
            // Past the window there is nothing more to find.
            if (step > 0 && span.start >= window.end) break
            if (step < 0 && span.end <= window.start) break
            i += step
        }
        return GuideMove.Edge(direction)
    }

    private fun vertical(from: GuidePosition, direction: GuideDirection, anchor: Long): GuideMove {
        val step = if (direction == GuideDirection.Down) 1 else -1
        val target = positionNear(from.row + step, anchor, step) ?: return GuideMove.Edge(direction)
        return GuideMove.Moved(target, anchor)
    }
}
