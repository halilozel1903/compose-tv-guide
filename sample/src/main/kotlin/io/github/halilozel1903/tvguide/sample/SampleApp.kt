package io.github.halilozel1903.tvguide.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.halilozel1903.tvguide.GuideChannelContent
import io.github.halilozel1903.tvguide.GuideDaySwitcher
import io.github.halilozel1903.tvguide.GuideProgrammeContent
import io.github.halilozel1903.tvguide.TvGuide
import io.github.halilozel1903.tvguide.TvGuideDefaults
import io.github.halilozel1903.tvguide.TvGuideDimensions
import io.github.halilozel1903.tvguide.rememberGuideNow
import io.github.halilozel1903.tvguide.rememberTvGuideState
import io.github.halilozel1903.tvguide.core.GuideDays
import io.github.halilozel1903.tvguide.core.TimeSpan
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

private val SampleDimensions = TvGuideDimensions(rowHeight = 52.dp, rowSpacing = 6.dp)

@Composable
fun SampleApp(scene: Scene) {
    val zone = remember { ZoneId.systemDefault() }
    val now = rememberGuideNow()
    val launchTime = remember { System.currentTimeMillis() }
    val today = remember { GuideDays.dayOf(launchTime, zone) }
    val days = remember(today) { GuideDays.range(today, before = 1, after = 5) }
    val schedules = remember(today) { DemoGuide.schedules(days.first(), days.last(), zone) }

    var selectedDay by rememberSaveable { mutableLongStateOf(today.plusDays(scene.dayOffset).toEpochDay()) }
    val day = LocalDate.ofEpochDay(selectedDay)
    val window = remember(day, zone) { GuideDays.window(day, zone) }

    val state = rememberTvGuideState(
        initialChannelIndex = scene.channelIndex,
        initialTime = launchTime + scene.minutesFromNow * TimeSpan.MINUTE,
    )
    val timeFormatter = TvGuideDefaults.timeFormatter()
    val scope = rememberCoroutineScope()
    val dayFocus = remember { FocusRequester() }
    var dayChipFocused by remember { mutableStateOf(false) }

    LaunchedEffect(scene) {
        if (scene.focusDays) dayFocus.requestFocus() else state.requestFocus()
    }
    ReportScene(scene) {
        state.focusedProgrammeKey != null && if (scene.focusDays) dayChipFocused else state.hasFocus
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("TV Guide", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Demo data: made-up channels and programmes, bundled with the sample",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (day == today) {
                // scrollToNow from outside the guide: focus comes back to what is on now.
                Surface(
                    onClick = { scope.launch { state.scrollToNow() } },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Text(
                        text = "Now",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
            }
            GuideDaySwitcher(
                days = days,
                selected = day,
                onSelect = { selectedDay = it.toEpochDay() },
                today = today,
                focusRequester = dayFocus,
                modifier = Modifier.onFocusChanged { dayChipFocused = it.hasFocus },
            )
        }
        Spacer(Modifier.height(14.dp))
        TvGuide(
            channels = DemoGuide.channels,
            programmes = { channel -> schedules[channel.id].orEmpty() },
            window = window,
            channelKey = { it.id },
            programmeKey = { it.id },
            programmeTime = { it.time },
            channelCell = { channel, _ ->
                GuideChannelContent(
                    name = channel.name,
                    number = channel.number.toString(),
                    logoSize = 30.dp,
                ) { ChannelLogo(channel, Modifier.size(30.dp)) }
            },
            modifier = Modifier.fillMaxSize(),
            state = state,
            now = now,
            zone = zone,
            dimensions = SampleDimensions,
            timeFormatter = timeFormatter,
            corner = {
                Text(
                    text = TvGuideDefaults.dayLabel(day, GuideDays.relation(day, today)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp),
                )
            },
            details = { channel, programme ->
                Column {
                    ProgrammeHero(channel, programme, now, zone, timeFormatter, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
            },
        ) { _, programme ->
            GuideProgrammeContent(
                title = programme.title,
                subtitle = listOfNotNull(timeText, programme.episode).joinToString("  ·  "),
            )
        }
    }
}
