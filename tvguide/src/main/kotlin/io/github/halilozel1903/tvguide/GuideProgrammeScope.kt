package io.github.halilozel1903.tvguide

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.halilozel1903.tvguide.core.AirStatus
import io.github.halilozel1903.tvguide.core.CellLayout
import io.github.halilozel1903.tvguide.core.TimeSpan
import kotlin.math.roundToInt

/** What a programme cell knows while its content is composed. */
@Stable
public interface GuideProgrammeScope {
    /** The programme's full time, also the part outside the guide window. */
    public val time: TimeSpan

    /** Past, live or upcoming, at the guide's current time. */
    public val status: AirStatus

    /** How much of the programme has been on, 0 to 1. */
    public val progress: Float

    /** `true` while this cell has focus. */
    public val isFocused: Boolean

    /** The programme started before the guide window and is cut at the left edge. */
    public val clippedStart: Boolean

    /** The programme ends after the guide window and is cut at the right edge. */
    public val clippedEnd: Boolean

    /** The width of the cell, to leave out details in short programmes. */
    public val width: Dp

    /** Start and end time as text, such as "20:00 – 21:30". */
    public val timeText: String

    /** The guide's colours. */
    public val colors: TvGuideColors

    /**
     * Keeps content at the left edge of the programme area while a long programme is scrolled partly out of view,
     * so its title stays readable. Apply it to the content inside the cell, before any padding.
     */
    public fun Modifier.stickToViewport(): Modifier
}

internal class GuideProgrammeScopeImpl(
    override val time: TimeSpan,
    override val status: AirStatus,
    override val progress: Float,
    override val isFocused: Boolean,
    override val clippedStart: Boolean,
    override val clippedEnd: Boolean,
    override val width: Dp,
    override val timeText: String,
    override val colors: TvGuideColors,
    private val cellX: Float,
    private val cellWidth: Float,
    private val minContentWidth: Float,
    private val viewportStart: () -> Float,
) : GuideProgrammeScope {
    override fun Modifier.stickToViewport(): Modifier = layout { measurable, constraints ->
        val shift = CellLayout.stickyShift(cellX, cellWidth, viewportStart(), minContentWidth).roundToInt()
        if (!constraints.hasBoundedWidth) {
            val placeable = measurable.measure(constraints)
            return@layout layout(placeable.width + shift, placeable.height) { placeable.placeRelative(shift, 0) }
        }
        val maxWidth = (constraints.maxWidth - shift).coerceAtLeast(0)
        val placeable = measurable.measure(constraints.copy(minWidth = minOf(constraints.minWidth, maxWidth), maxWidth = maxWidth))
        layout(constraints.maxWidth, placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight)) {
            placeable.placeRelative(shift, 0)
        }
    }
}

/**
 * The default content of a programme cell: the title, a second line (the time by default) when the cell is wide
 * enough, marks for programmes cut at the window edges, and a progress bar while the programme is on.
 */
@Composable
public fun GuideProgrammeScope.GuideProgrammeContent(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = timeText,
    showProgress: Boolean = true,
) {
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .stickToViewport()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (clippedStart) {
                    ClipMark(pointsLeft = true)
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (clippedEnd) {
                    Spacer(Modifier.width(4.dp))
                    ClipMark(pointsLeft = false)
                }
            }
            if (subtitle != null && width >= 72.dp) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (showProgress && status == AirStatus.Live) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.progressTrack),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(colors.progress),
                )
            }
        }
    }
}

/** A small triangle that says the programme continues beyond the guide window. */
@Composable
private fun ClipMark(pointsLeft: Boolean) {
    val color = LocalContentColor.current.copy(alpha = 0.8f)
    Canvas(Modifier.size(width = 6.dp, height = 10.dp)) {
        val path = Path().apply {
            if (pointsLeft) {
                moveTo(size.width, 0f)
                lineTo(0f, size.height / 2f)
                lineTo(size.width, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
            }
            close()
        }
        drawPath(path, color)
    }
}
