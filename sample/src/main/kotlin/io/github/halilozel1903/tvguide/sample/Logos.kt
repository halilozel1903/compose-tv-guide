package io.github.halilozel1903.tvguide.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** A made-up channel logo: a coloured shape with the last two digits of the channel number. */
@Composable
fun ChannelLogo(channel: DemoChannel, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) { drawLogoShape(channel.logo, channel.color) }
        Text(
            text = (channel.number % 100).toString().padStart(2, '0'),
            color = if (channel.logo == LogoShape.Ring) channel.color else Color(0xFF10131A),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun DrawScope.drawLogoShape(shape: LogoShape, color: Color) {
    val w = size.width
    val h = size.height
    when (shape) {
        LogoShape.Circle -> drawCircle(color)
        LogoShape.Square -> drawRoundRect(color, cornerRadius = CornerRadius(w * 0.22f))
        LogoShape.Ring -> drawCircle(color, radius = w * 0.42f, style = Stroke(width = w * 0.12f))
        LogoShape.Triangle -> drawPath(
            Path().apply {
                moveTo(w / 2f, h * 0.04f)
                lineTo(w * 0.98f, h * 0.92f)
                lineTo(w * 0.02f, h * 0.92f)
                close()
            },
            color,
        )
        LogoShape.Diamond -> drawPath(
            Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h / 2f)
                lineTo(w / 2f, h)
                lineTo(0f, h / 2f)
                close()
            },
            color,
        )
        LogoShape.Hexagon -> drawPath(
            Path().apply {
                for (i in 0 until 6) {
                    val angle = PI / 3 * i - PI / 2
                    val x = w / 2f + (w / 2f) * cos(angle).toFloat()
                    val y = h / 2f + (h / 2f) * sin(angle).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            },
            color,
        )
    }
}

/** Abstract artwork for a programme, generated from its id: a gradient with soft circles and bars. */
@Composable
fun ProgrammeArt(programme: DemoProgramme, channel: DemoChannel, modifier: Modifier = Modifier) {
    val seed = programme.title.hashCode()
    Canvas(modifier) {
        val random = Random(seed)
        val base = channel.color
        drawRect(
            Brush.linearGradient(
                listOf(lerp(base, Color.Black, 0.55f), lerp(base, Color(0xFF0A0D14), 0.85f)),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
        )
        repeat(4) {
            val radius = size.minDimension * (0.25f + random.nextFloat() * 0.45f)
            drawCircle(
                color = lerp(base, Color.White, random.nextFloat() * 0.4f).copy(alpha = 0.18f + random.nextFloat() * 0.2f),
                radius = radius,
                center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height),
            )
        }
        val bars = 3 + random.nextInt(3)
        repeat(bars) { i ->
            val barHeight = size.height * (0.12f + random.nextFloat() * 0.3f)
            val barWidth = size.width * 0.06f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(size.width * 0.08f + i * barWidth * 1.6f, size.height * 0.88f - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f),
            )
        }
    }
}
