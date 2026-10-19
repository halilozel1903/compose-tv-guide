package io.github.halilozel1903.tvguide

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import io.github.halilozel1903.tvguide.core.DayRelation
import io.github.halilozel1903.tvguide.core.GuideDirection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Sizes of the guide. Time is laid out at [minuteWidth] per minute, so a half hour programme is 30 times as wide.
 *
 * @property channelColumnWidth width of the sticky channel column.
 * @property rowHeight height of a channel row.
 * @property rowSpacing space between rows.
 * @property rulerHeight height of the sticky time ruler.
 * @property minuteWidth width of one minute.
 * @property cellSpacing space between programme cells in a row.
 * @property cellCornerRadius corner radius of programme cells and channel cells.
 * @property scrollPadding space kept between the focused cell and the edges of the programme area.
 * @property minStickyContentWidth narrowest a title may get when it sticks to the left edge of a long programme.
 * @property rulerStepMinutes minutes between ruler labels; must divide a day.
 * @property nowLineWidth width of the line at the current time.
 * @property detailsWidth width of the details panel with [GuideDetailsPlacement.End].
 */
@Immutable
public data class TvGuideDimensions(
    val channelColumnWidth: Dp = 168.dp,
    val rowHeight: Dp = 60.dp,
    val rowSpacing: Dp = 6.dp,
    val rulerHeight: Dp = 36.dp,
    val minuteWidth: Dp = 7.dp,
    val cellSpacing: Dp = 4.dp,
    val cellCornerRadius: Dp = 8.dp,
    val scrollPadding: Dp = 48.dp,
    val minStickyContentWidth: Dp = 96.dp,
    val rulerStepMinutes: Int = 30,
    val nowLineWidth: Dp = 2.dp,
    val detailsWidth: Dp = 320.dp,
)

/** Colours of the guide. Cells have a container and content colour for each [io.github.halilozel1903.tvguide.core.AirStatus] and for focus. */
@Immutable
public data class TvGuideColors(
    val background: Color,
    val channelContainer: Color,
    val channelContent: Color,
    val focusedChannelContainer: Color,
    val focusedChannelContent: Color,
    val rulerContent: Color,
    val rulerTick: Color,
    val pastContainer: Color,
    val pastContent: Color,
    val liveContainer: Color,
    val liveContent: Color,
    val upcomingContainer: Color,
    val upcomingContent: Color,
    val focusedContainer: Color,
    val focusedContent: Color,
    val nowLine: Color,
    val progress: Color,
    val progressTrack: Color,
)

/** Where the details panel goes relative to the grid. */
public enum class GuideDetailsPlacement { Top, End }

/** Defaults for [TvGuide] and [GuideDaySwitcher]. */
public object TvGuideDefaults {

    /** Directions in which focus may leave the guide at its edges: up to a header, left to a navigation drawer. */
    public val FocusExits: Set<GuideDirection> = setOf(GuideDirection.Up, GuideDirection.Left)

    /** The default sizes. */
    public val Dimensions: TvGuideDimensions = TvGuideDimensions()

    /** Colours derived from the Compose for TV colour scheme. */
    @Composable
    public fun colors(
        background: Color = MaterialTheme.colorScheme.background,
        channelContainer: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        channelContent: Color = MaterialTheme.colorScheme.onSurface,
        focusedChannelContainer: Color = lerp(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary, 0.28f),
        focusedChannelContent: Color = MaterialTheme.colorScheme.onSurface,
        rulerContent: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        rulerTick: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        pastContainer: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        pastContent: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        liveContainer: Color = lerp(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary, 0.22f),
        liveContent: Color = MaterialTheme.colorScheme.onSurface,
        upcomingContainer: Color = MaterialTheme.colorScheme.surfaceVariant,
        upcomingContent: Color = MaterialTheme.colorScheme.onSurface,
        focusedContainer: Color = MaterialTheme.colorScheme.onSurface,
        focusedContent: Color = MaterialTheme.colorScheme.surface,
        nowLine: Color = MaterialTheme.colorScheme.primary,
        progress: Color = MaterialTheme.colorScheme.primary,
        progressTrack: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
    ): TvGuideColors = TvGuideColors(
        background, channelContainer, channelContent, focusedChannelContainer, focusedChannelContent, rulerContent,
        rulerTick, pastContainer, pastContent, liveContainer, liveContent, upcomingContainer, upcomingContent,
        focusedContainer, focusedContent, nowLine, progress, progressTrack,
    )

    /** Times as "18:30", or "6:30 PM" when the device uses the 12 hour clock. */
    @Composable
    public fun timeFormatter(): DateTimeFormatter {
        val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
        return DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
    }

    /** "Yesterday", "Today", "Tomorrow", or the short day and date such as "Thu 15". */
    public fun dayLabel(date: LocalDate, relation: DayRelation): String = when (relation) {
        DayRelation.Yesterday -> "Yesterday"
        DayRelation.Today -> "Today"
        DayRelation.Tomorrow -> "Tomorrow"
        DayRelation.Other -> date.format(DateTimeFormatter.ofPattern("EEE d", Locale.getDefault()))
    }
}
