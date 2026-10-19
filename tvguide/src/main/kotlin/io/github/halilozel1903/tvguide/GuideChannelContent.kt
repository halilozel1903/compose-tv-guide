package io.github.halilozel1903.tvguide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/** Content for the channel column: an optional logo, the channel number and the name. */
@Composable
public fun GuideChannelContent(
    name: String,
    modifier: Modifier = Modifier,
    number: String? = null,
    logoSize: Dp = 36.dp,
    logo: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (logo != null) {
            Box(Modifier.size(logoSize), contentAlignment = Alignment.Center) { logo() }
            Spacer(Modifier.width(10.dp))
        }
        Column {
            if (number != null) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
