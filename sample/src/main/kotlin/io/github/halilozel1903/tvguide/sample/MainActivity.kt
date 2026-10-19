package io.github.halilozel1903.tvguide.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.darkColorScheme

/**
 * D-pad presses can't be timed reliably through adb on a fresh emulator, so `scripts/screenshots.sh` starts the app
 * with `--es scene <scene>` to set up each screenshot:
 *
 * - `guide`: today, the first channel's programme on now focused
 * - `detail`: today, the fourth channel's programme in about 75 minutes focused, its details in the hero
 * - `day`: tomorrow selected in the day switcher, which has focus
 *
 * Without the extra the app starts on today with the programme on now focused.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            MaterialTheme(colorScheme = SampleColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                    colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    SampleApp(scene)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}

private val SampleColors = darkColorScheme(
    primary = Color(0xFF7FD1FF),
    onPrimary = Color(0xFF00243A),
    secondary = Color(0xFFFFB86B),
    background = Color(0xFF0A0D14),
    onBackground = Color(0xFFF1F3F8),
    surface = Color(0xFF0A0D14),
    onSurface = Color(0xFFF1F3F8),
    surfaceVariant = Color(0xFF1E2533),
    onSurfaceVariant = Color(0xFFA9B2C3),
)
