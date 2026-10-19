package io.github.halilozel1903.tvguide

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import io.github.halilozel1903.tvguide.core.AirStatus
import io.github.halilozel1903.tvguide.core.Airing
import io.github.halilozel1903.tvguide.core.CellGeometry
import io.github.halilozel1903.tvguide.core.CellLayout
import io.github.halilozel1903.tvguide.core.GuideDirection
import io.github.halilozel1903.tvguide.core.GuideScroll
import io.github.halilozel1903.tvguide.core.Schedule
import io.github.halilozel1903.tvguide.core.TimeScale
import io.github.halilozel1903.tvguide.core.TimeSpan
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * An electronic programme guide: channels down the side, time across the top, and a cell for every programme, as
 * wide as it is long.
 *
 * The channel column and the time ruler stay put while the programmes scroll under them. Only the rows and cells on
 * screen (plus a little overscan) are composed, so a full day of hundreds of channels is fine. The D-pad moves
 * between programmes by time: left and right go to the previous and next programme, up and down keep the column of
 * time ([TvGuideState.focusAnchor]) and the guide scrolls so the focused cell stays in view. Focus coming back into
 * the guide returns to the programme it left.
 *
 * Programmes in a channel should not overlap; they are sorted by start time if they are not in order.
 *
 * @param channels the channel rows, top to bottom.
 * @param programmes a channel's programmes; called lazily for rows that are shown or navigated through.
 * @param window the time the guide covers, usually a day from [io.github.halilozel1903.tvguide.core.GuideDays.window].
 * @param channelKey a stable key for a channel.
 * @param programmeKey a key for a programme, unique within its channel and stable across data refreshes.
 * @param programmeTime when a programme is on.
 * @param channelCell content of the sticky channel column, for example [GuideChannelContent].
 * @param state focus and scroll state, see [rememberTvGuideState].
 * @param now the current time for the now line and past, live and upcoming styling, see [rememberGuideNow].
 * @param zone the time zone of the ruler labels and ticks.
 * @param focusExits directions in which focus may leave the guide at its edges; the others stop at the edge.
 * @param onFocusedProgrammeChange called when another programme gets focus.
 * @param corner content above the channel column, left of the time ruler.
 * @param details a details panel for the focused programme, above the grid or at its end ([detailsPlacement]).
 * @param programmeCell content of a programme cell, for example [GuideProgrammeContent]. The cell itself (focus,
 * colours by status, click) is drawn by the guide.
 */
@Composable
public fun <C, P> TvGuide(
    channels: List<C>,
    programmes: (channel: C) -> List<P>,
    window: TimeSpan,
    channelKey: (channel: C) -> Any,
    programmeKey: (programme: P) -> Any,
    programmeTime: (programme: P) -> TimeSpan,
    channelCell: @Composable (channel: C, isFocusedRow: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    state: TvGuideState = rememberTvGuideState(),
    now: Long = rememberGuideNow(),
    zone: ZoneId = remember { ZoneId.systemDefault() },
    dimensions: TvGuideDimensions = TvGuideDefaults.Dimensions,
    colors: TvGuideColors = TvGuideDefaults.colors(),
    timeFormatter: DateTimeFormatter = TvGuideDefaults.timeFormatter(),
    focusExits: Set<GuideDirection> = TvGuideDefaults.FocusExits,
    onProgrammeClick: (channel: C, programme: P) -> Unit = { _, _ -> },
    onProgrammeLongClick: ((channel: C, programme: P) -> Unit)? = null,
    onFocusedProgrammeChange: ((channel: C, programme: P) -> Unit)? = null,
    corner: @Composable BoxScope.() -> Unit = {},
    details: (@Composable (channel: C, programme: P) -> Unit)? = null,
    detailsPlacement: GuideDetailsPlacement = GuideDetailsPlacement.Top,
    programmeCell: @Composable GuideProgrammeScope.(channel: C, programme: P) -> Unit,
) {
    val density = LocalDensity.current
    val model = remember(channels, programmes, programmeKey, programmeTime, window, dimensions, density) {
        with(density) {
            GuideModel(
                channels = channels,
                programmesOf = programmes,
                programmeKey = programmeKey,
                programmeTime = programmeTime,
                window = window,
                scale = TimeScale(window.start, dimensions.minuteWidth.toPx()),
                rowHeight = dimensions.rowHeight.toPx(),
                rowStride = (dimensions.rowHeight + dimensions.rowSpacing).toPx(),
                cellGap = dimensions.cellSpacing.toPx(),
            )
        }
    }
    val scope = rememberCoroutineScope()
    SideEffect {
        state.scope = scope
        state.now = now
        state.exits = focusExits
        state.scrollPadding = with(density) { dimensions.scrollPadding.toPx() }
    }
    LaunchedEffect(state, model, state.viewportWidth, state.viewportHeight) { state.sync(model) }

    if (onFocusedProgrammeChange != null) {
        val callback by rememberUpdatedState(onFocusedProgrammeChange)
        LaunchedEffect(state, model) {
            snapshotFlow { state.focusedChannelIndex to state.focusedProgrammeKey }.collect { (row, key) ->
                focusedProgramme(model, row, key, state.focusedIndex)?.let { programme ->
                    callback(model.channels[row], programme)
                }
            }
        }
    }

    val grid: @Composable (Modifier) -> Unit = { gridModifier ->
        Column(gridModifier) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(dimensions.rulerHeight),
            ) {
                Box(
                    modifier = Modifier
                        .width(dimensions.channelColumnWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart,
                    content = corner,
                )
                GuideTimeRuler(
                    model = model,
                    state = state,
                    now = now,
                    zone = zone,
                    formatter = timeFormatter,
                    dimensions = dimensions,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            GuideBody(
                model = model,
                state = state,
                now = now,
                zone = zone,
                timeFormatter = timeFormatter,
                dimensions = dimensions,
                colors = colors,
                channelKey = channelKey,
                channelCell = channelCell,
                programmeCell = programmeCell,
                onProgrammeClick = onProgrammeClick,
                onProgrammeLongClick = onProgrammeLongClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }

    Box(modifier.background(colors.background)) {
        when {
            details == null -> grid(Modifier.fillMaxSize())
            detailsPlacement == GuideDetailsPlacement.Top -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth()) { FocusedDetails(model, state, details) }
                grid(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
            else -> Row(Modifier.fillMaxSize()) {
                grid(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Box(
                    Modifier
                        .width(dimensions.detailsWidth)
                        .fillMaxHeight(),
                ) { FocusedDetails(model, state, details) }
            }
        }
    }
}

private fun <P> focusedProgramme(model: GuideModel<*, P>, row: Int, key: Any?, index: Int): P? {
    if (key == null || row !in 0 until model.rowCount) return null
    val list = model.programmes(row)
    list.getOrNull(index)?.let { if (model.programmeKey(it) == key) return it }
    return list.firstOrNull { model.programmeKey(it) == key }
}

@Composable
private fun <C, P> FocusedDetails(
    model: GuideModel<C, P>,
    state: TvGuideState,
    details: @Composable (channel: C, programme: P) -> Unit,
) {
    val row = state.focusedChannelIndex
    val programme = focusedProgramme(model, row, state.focusedProgrammeKey, state.focusedIndex) ?: return
    details(model.channels[row], programme)
}

@Composable
private fun <C, P> GuideBody(
    model: GuideModel<C, P>,
    state: TvGuideState,
    now: Long,
    zone: ZoneId,
    timeFormatter: DateTimeFormatter,
    dimensions: TvGuideDimensions,
    colors: TvGuideColors,
    channelKey: (C) -> Any,
    channelCell: @Composable (channel: C, isFocusedRow: Boolean) -> Unit,
    programmeCell: @Composable GuideProgrammeScope.(channel: C, programme: P) -> Unit,
    onProgrammeClick: (C, P) -> Unit,
    onProgrammeLongClick: ((C, P) -> Unit)?,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val channelColumn = with(density) { dimensions.channelColumnWidth.toPx() }
    val nowLineWidth = with(density) { dimensions.nowLineWidth.toPx() }
    val nowDot = with(density) { (dimensions.nowLineWidth * 2.5f).toPx() }
    Box(
        modifier
            .onSizeChanged {
                state.viewportWidth = (it.width - channelColumn).coerceAtLeast(0f)
                state.viewportHeight = it.height.toFloat()
            }
            .clipToBounds()
            .drawWithContent {
                drawContent()
                val x = channelColumn + model.scale.xOf(now) - state.offsetX.value
                if (x >= channelColumn && x <= size.width) {
                    drawLine(colors.nowLine, Offset(x, 0f), Offset(x, size.height), nowLineWidth)
                    drawCircle(colors.nowLine, radius = nowDot, center = Offset(x, nowDot))
                }
            }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val direction = when (event.key) {
                    Key.DirectionLeft -> GuideDirection.Left
                    Key.DirectionRight -> GuideDirection.Right
                    Key.DirectionUp -> GuideDirection.Up
                    Key.DirectionDown -> GuideDirection.Down
                    else -> null
                }
                direction != null && state.navigate(direction)
            }
            .onFocusChanged { state.hasFocus = it.hasFocus }
            .focusProperties { onEnter = { state.entryRequester()?.requestFocus() } }
            .focusGroup(),
    ) {
        val rows by remember(model, state) {
            derivedStateOf {
                GuideScroll.visibleRows(state.offsetY.value, state.viewportHeight, model.rowStride, model.rowCount, overscan = 1)
            }
        }
        for (row in rows) {
            val channel = model.channels[row]
            key(channelKey(channel)) {
                GuideRow(
                    row = row,
                    channel = channel,
                    model = model,
                    state = state,
                    now = now,
                    zone = zone,
                    timeFormatter = timeFormatter,
                    dimensions = dimensions,
                    colors = colors,
                    channelCell = channelCell,
                    programmeCell = programmeCell,
                    onProgrammeClick = onProgrammeClick,
                    onProgrammeLongClick = onProgrammeLongClick,
                    modifier = Modifier
                        .offset { IntOffset(0, (model.rowTop(row) - state.offsetY.value).roundToInt()) }
                        .fillMaxWidth()
                        .height(dimensions.rowHeight),
                )
            }
        }
    }
}

@Composable
private fun <C, P> GuideRow(
    row: Int,
    channel: C,
    model: GuideModel<C, P>,
    state: TvGuideState,
    now: Long,
    zone: ZoneId,
    timeFormatter: DateTimeFormatter,
    dimensions: TvGuideDimensions,
    colors: TvGuideColors,
    channelCell: @Composable (channel: C, isFocusedRow: Boolean) -> Unit,
    programmeCell: @Composable GuideProgrammeScope.(channel: C, programme: P) -> Unit,
    onProgrammeClick: (C, P) -> Unit,
    onProgrammeLongClick: ((C, P) -> Unit)?,
    modifier: Modifier,
) {
    val isFocusedRow by remember(state, row) { derivedStateOf { state.hasFocus && state.focusedChannelIndex == row } }
    Row(modifier) {
        Box(
            Modifier
                .width(dimensions.channelColumnWidth - dimensions.cellSpacing)
                .fillMaxHeight()
                .clip(RoundedCornerShape(dimensions.cellCornerRadius))
                .background(if (isFocusedRow) colors.focusedChannelContainer else colors.channelContainer),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (isFocusedRow) colors.focusedChannelContent else colors.channelContent,
            ) {
                channelCell(channel, isFocusedRow)
            }
        }
        Spacer(Modifier.width(dimensions.cellSpacing))
        ProgrammeStrip(
            row = row,
            channel = channel,
            model = model,
            state = state,
            now = now,
            zone = zone,
            timeFormatter = timeFormatter,
            dimensions = dimensions,
            colors = colors,
            programmeCell = programmeCell,
            onProgrammeClick = onProgrammeClick,
            onProgrammeLongClick = onProgrammeLongClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clipToBounds(),
        )
    }
}

@Composable
private fun <C, P> ProgrammeStrip(
    row: Int,
    channel: C,
    model: GuideModel<C, P>,
    state: TvGuideState,
    now: Long,
    zone: ZoneId,
    timeFormatter: DateTimeFormatter,
    dimensions: TvGuideDimensions,
    colors: TvGuideColors,
    programmeCell: @Composable GuideProgrammeScope.(channel: C, programme: P) -> Unit,
    onProgrammeClick: (C, P) -> Unit,
    onProgrammeLongClick: ((C, P) -> Unit)?,
    modifier: Modifier,
) {
    val programmes = model.programmes(row)
    val spans = model.spans(row)
    val range by remember(model, state, row) {
        derivedStateOf { state.composeTime()?.let { Schedule.visibleRange(spans, it) } ?: IntRange.EMPTY }
    }
    val visibleRange = range
    val cells: List<Pair<Int, CellGeometry>> = remember(model, visibleRange) {
        visibleRange.mapNotNull { i -> CellLayout.place(spans[i], model.window, model.scale, model.cellGap)?.let { i to it } }
    }
    Layout(
        content = {
            cells.forEach { (index, geometry) ->
                val programme = programmes[index]
                val programmeKey = model.programmeKey(programme)
                key(programmeKey) {
                    ProgrammeCell(
                        row = row,
                        index = index,
                        programmeKey = programmeKey,
                        channel = channel,
                        programme = programme,
                        span = spans[index],
                        geometry = geometry,
                        state = state,
                        now = now,
                        zone = zone,
                        timeFormatter = timeFormatter,
                        dimensions = dimensions,
                        colors = colors,
                        programmeCell = programmeCell,
                        onProgrammeClick = onProgrammeClick,
                        onProgrammeLongClick = onProgrammeLongClick,
                    )
                }
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else 0
        val placeables = measurables.mapIndexed { i, measurable ->
            val width = cells.getOrNull(i)?.second?.width?.roundToInt() ?: 0
            measurable.measure(Constraints.fixed(width.coerceAtLeast(0), height))
        }
        layout(constraints.maxWidth, height) {
            val offset = state.offsetX.value
            placeables.forEachIndexed { i, placeable ->
                val geometry = cells.getOrNull(i)?.second ?: return@forEachIndexed
                placeable.place((geometry.x - offset).roundToInt(), 0)
            }
        }
    }
}

@Composable
private fun <C, P> ProgrammeCell(
    row: Int,
    index: Int,
    programmeKey: Any,
    channel: C,
    programme: P,
    span: TimeSpan,
    geometry: CellGeometry,
    state: TvGuideState,
    now: Long,
    zone: ZoneId,
    timeFormatter: DateTimeFormatter,
    dimensions: TvGuideDimensions,
    colors: TvGuideColors,
    programmeCell: @Composable GuideProgrammeScope.(channel: C, programme: P) -> Unit,
    onProgrammeClick: (C, P) -> Unit,
    onProgrammeLongClick: ((C, P) -> Unit)?,
) {
    val id = remember(row, programmeKey) { GuideCellId(row, programmeKey) }
    val requester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val status = Airing.status(span, now)
    val (container, content) = when (status) {
        AirStatus.Past -> colors.pastContainer to colors.pastContent
        AirStatus.Live -> colors.liveContainer to colors.liveContent
        AirStatus.Upcoming -> colors.upcomingContainer to colors.upcomingContent
    }
    val timeText = remember(span, zone, timeFormatter) {
        val start = timeFormatter.format(Instant.ofEpochMilli(span.start).atZone(zone))
        val end = timeFormatter.format(Instant.ofEpochMilli(span.end).atZone(zone))
        "$start – $end"
    }
    val scope = GuideProgrammeScopeImpl(
        time = span,
        status = status,
        progress = Airing.progress(span, now),
        isFocused = isFocused,
        clippedStart = geometry.clippedStart,
        clippedEnd = geometry.clippedEnd,
        width = with(density) { geometry.width.toDp() },
        timeText = timeText,
        colors = colors,
        cellX = geometry.x,
        cellWidth = geometry.width,
        minContentWidth = with(density) { dimensions.minStickyContentWidth.toPx() },
        viewportStart = { state.offsetX.value },
    )
    val shape = RoundedCornerShape(dimensions.cellCornerRadius)
    Surface(
        onClick = { onProgrammeClick(channel, programme) },
        onLongClick = onProgrammeLongClick?.let { longClick -> { longClick(channel, programme) } },
        modifier = Modifier
            .focusRequester(requester)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) state.onCellFocused(row, index, programmeKey)
            },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = container,
            contentColor = content,
            focusedContainerColor = colors.focusedContainer,
            focusedContentColor = colors.focusedContent,
            pressedContainerColor = colors.focusedContainer,
            pressedContentColor = colors.focusedContent,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        scope.programmeCell(channel, programme)
    }
    DisposableEffect(state, id) {
        state.register(id, requester)
        onDispose { state.unregister(id, requester) }
    }
    val pending by remember(state, id) { derivedStateOf { state.pendingFocus == id } }
    LaunchedEffect(pending) {
        if (pending) runCatching { requester.requestFocus() }
    }
}
