package io.github.halilozel1903.tvguide.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.halilozel1903.tvguide.core.AirStatus
import io.github.halilozel1903.tvguide.core.Airing
import io.github.halilozel1903.tvguide.core.DurationText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The hero area above the guide: artwork, status, title, time and description of the focused programme. */
@Composable
fun ProgrammeHero(
    channel: DemoChannel,
    programme: DemoProgramme,
    now: Long,
    zone: ZoneId,
    timeFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier,
) {
    val status = Airing.status(programme.time, now)
    Row(modifier.height(HeroHeight), verticalAlignment = Alignment.CenterVertically) {
        ProgrammeArt(
            programme = programme,
            channel = channel,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp)),
        )
        Spacer(Modifier.width(24.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(status)
                Spacer(Modifier.width(10.dp))
                ChannelLogo(channel, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${channel.number}  ${channel.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = programme.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val start = timeFormatter.format(Instant.ofEpochMilli(programme.time.start).atZone(zone))
            val end = timeFormatter.format(Instant.ofEpochMilli(programme.time.end).atZone(zone))
            val facts = listOfNotNull(
                "$start – $end",
                DurationText.format(programme.time.durationMillis),
                programme.genre.label,
                programme.episode,
                when (status) {
                    AirStatus.Live -> "${DurationText.format(Airing.remainingMillis(programme.time, now))} left"
                    AirStatus.Upcoming -> "starts in ${DurationText.format(Airing.startsInMillis(programme.time, now))}"
                    AirStatus.Past -> null
                },
            )
            Text(
                text = facts.joinToString("  ·  "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            if (status == AirStatus.Live) {
                Box(
                    Modifier
                        .padding(vertical = 4.dp)
                        .width(240.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(Airing.progress(programme.time, now))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
            Text(
                text = programme.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusBadge(status: AirStatus) {
    val (text, color) = when (status) {
        AirStatus.Live -> "ON NOW" to MaterialTheme.colorScheme.primary
        AirStatus.Upcoming -> "UPCOMING" to Color(0xFFFFB86B)
        AirStatus.Past -> "ENDED" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0A0D14),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

val HeroHeight = 120.dp
