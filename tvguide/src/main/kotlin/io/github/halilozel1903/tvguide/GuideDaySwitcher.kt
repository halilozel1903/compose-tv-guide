package io.github.halilozel1903.tvguide

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.halilozel1903.tvguide.core.DayRelation
import io.github.halilozel1903.tvguide.core.GuideDays
import java.time.LocalDate

/**
 * A row of day chips to switch the guide between days. Focus coming into the row lands on the [selected] day.
 *
 * @param days the days to offer, usually from [GuideDays.range].
 * @param selected the day the guide shows.
 * @param onSelect called with the chosen day; show its window with [GuideDays.window].
 * @param today the day labelled "Today".
 * @param selectOnFocus switch days as soon as a chip gets focus instead of on click.
 * @param focusRequester optional requester to move focus to the selected chip from outside.
 * @param label the text of a chip.
 */
@Composable
public fun GuideDaySwitcher(
    days: List<LocalDate>,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { LocalDate.now() },
    selectOnFocus: Boolean = false,
    focusRequester: FocusRequester? = null,
    label: (date: LocalDate, relation: DayRelation) -> String = TvGuideDefaults::dayLabel,
) {
    val requesters = remember(days) { days.associateWith { FocusRequester() } }
    Row(
        modifier = modifier
            .focusProperties { onEnter = { requesters[selected]?.requestFocus() } }
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        days.forEach { day ->
            val isSelected = day == selected
            val chipModifier = Modifier
                .focusRequester(requesters.getValue(day))
                .let { if (isSelected && focusRequester != null) it.focusRequester(focusRequester) else it }
                .onFocusChanged { if (it.isFocused && selectOnFocus && day != selected) onSelect(day) }
            Surface(
                onClick = { onSelect(day) },
                modifier = chipModifier,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                    focusedContentColor = MaterialTheme.colorScheme.surface,
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            ) {
                Text(
                    text = label(day, GuideDays.relation(day, today)),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
