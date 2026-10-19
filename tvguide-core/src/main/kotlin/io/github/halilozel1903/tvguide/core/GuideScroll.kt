package io.github.halilozel1903.tvguide.core

import kotlin.math.ceil
import kotlin.math.floor

/** Scroll targets for the guide, for either axis. Offsets, sizes and positions are in pixels. */
public object GuideScroll {

    /** The largest scroll offset for content of [contentSize] in a viewport of [viewportSize]. */
    public fun maxOffset(contentSize: Float, viewportSize: Float): Float = (contentSize - viewportSize).coerceAtLeast(0f)

    /** [offset] kept between 0 and [maxOffset]. */
    public fun clamp(offset: Float, contentSize: Float, viewportSize: Float): Float =
        offset.coerceIn(0f, maxOffset(contentSize, viewportSize))

    /**
     * The scroll offset that brings the item `[start, end)` into view with [padding] around it, moving as little as
     * possible. An item that is already in view keeps the current [offset].
     *
     * An item wider than the viewport counts as in view once half of the space inside the padding shows it; when it
     * does not, the guide scrolls just far enough for that half.
     */
    public fun keepVisible(
        start: Float,
        end: Float,
        offset: Float,
        viewportSize: Float,
        contentSize: Float,
        padding: Float = 0f,
    ): Float {
        if (viewportSize <= 0f) return clamp(offset, contentSize, viewportSize)
        val pad = padding.coerceIn(0f, viewportSize / 4f)
        val available = viewportSize - 2 * pad
        val size = end - start
        val lowEdge = offset + pad
        val highEdge = offset + viewportSize - pad
        val target = if (size <= available) {
            when {
                start < lowEdge -> start - pad
                end > highEdge -> end - viewportSize + pad
                else -> offset
            }
        } else {
            val needed = available / 2f
            val shown = minOf(end, highEdge) - maxOf(start, lowEdge)
            when {
                shown >= needed -> offset
                start >= lowEdge -> start + needed - viewportSize + pad
                else -> end - needed - pad
            }
        }
        return clamp(target, contentSize, viewportSize)
    }

    /** The offset that puts [position] at [fraction] of the viewport (0 = left or top edge, 0.5 = centre). */
    public fun align(position: Float, viewportSize: Float, contentSize: Float, fraction: Float = 0f): Float =
        clamp(position - viewportSize * fraction.coerceIn(0f, 1f), contentSize, viewportSize)

    /**
     * Indices of the rows of a list of [rowCount] rows, each [rowStride] apart (height plus spacing), that are at
     * least partly inside a viewport of [viewportSize] scrolled by [offset], plus [overscan] rows on each side.
     */
    public fun visibleRows(offset: Float, viewportSize: Float, rowStride: Float, rowCount: Int, overscan: Int = 0): IntRange {
        if (rowCount <= 0 || rowStride <= 0f || viewportSize <= 0f) return IntRange.EMPTY
        val first = floor(offset / rowStride).toInt() - overscan
        val last = ceil((offset + viewportSize) / rowStride).toInt() - 1 + overscan
        val from = first.coerceIn(0, rowCount - 1)
        val to = last.coerceIn(0, rowCount - 1)
        return if (to < from) IntRange.EMPTY else from..to
    }
}
