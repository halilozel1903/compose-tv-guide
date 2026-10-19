package io.github.halilozel1903.tvguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.halilozel1903.tvguide.core.TimeRuler
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/** The sticky time ruler: a label and a tick every [TvGuideDimensions.rulerStepMinutes], and a marker at now. */
@Composable
internal fun GuideTimeRuler(
    model: GuideModel<*, *>,
    state: TvGuideState,
    now: Long,
    zone: ZoneId,
    formatter: DateTimeFormatter,
    dimensions: TvGuideDimensions,
    colors: TvGuideColors,
    modifier: Modifier = Modifier,
) {
    val step = dimensions.rulerStepMinutes
    val ticks by remember(model, state, zone, step) {
        derivedStateOf {
            val span = state.composeTime()
            if (span == null) emptyList() else TimeRuler.ticks(span, step, zone)
        }
    }
    val shown = ticks
    Layout(
        content = {
            shown.forEach { tick ->
                key(tick) {
                    Text(
                        text = formatter.format(Instant.ofEpochMilli(tick).atZone(zone)),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.rulerContent,
                        maxLines = 1,
                    )
                }
            }
        },
        modifier = modifier
            .clipToBounds()
            .drawBehind {
                val offset = state.offsetX.value
                val stroke = 1.dp.toPx()
                shown.forEach { tick ->
                    val x = model.scale.xOf(tick) - offset
                    drawLine(colors.rulerTick, Offset(x, size.height * 0.45f), Offset(x, size.height), stroke)
                }
                val nowX = model.scale.xOf(now) - offset
                if (nowX in 0f..size.width) {
                    val half = 6.dp.toPx()
                    val marker = Path().apply {
                        moveTo(nowX - half, size.height - half * 1.6f)
                        lineTo(nowX + half, size.height - half * 1.6f)
                        lineTo(nowX, size.height)
                        close()
                    }
                    drawPath(marker, colors.nowLine)
                }
            },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        val labelGap = 6.dp.roundToPx()
        layout(constraints.maxWidth, constraints.maxHeight) {
            val offset = state.offsetX.value
            placeables.forEachIndexed { i, placeable ->
                val tick = shown.getOrNull(i) ?: return@forEachIndexed
                val x = (model.scale.xOf(tick) - offset).roundToInt() + labelGap
                val y = (constraints.maxHeight * 0.45f - placeable.height / 2f).roundToInt().coerceAtLeast(0)
                placeable.place(x, y)
            }
        }
    }
}
