package io.github.halilozel1903.tvguide.sample

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Screenshot scenes, picked with the `scene` intent extra (see `scripts/screenshots.sh`).
 *
 * @property dayOffset the guide day, relative to today.
 * @property channelIndex the channel focused first.
 * @property minutesFromNow the time focused first, relative to now.
 * @property focusDays focus starts on the day switcher instead of the grid.
 */
enum class Scene(
    val key: String,
    val dayOffset: Long,
    val channelIndex: Int,
    val minutesFromNow: Long,
    val focusDays: Boolean,
) {
    Interactive("interactive", 0, 0, 0, focusDays = false),
    Guide("guide", 0, 0, 0, focusDays = false),
    Detail("detail", 0, 3, 75, focusDays = false),
    Day("day", 1, 1, 24 * 60, focusDays = true),
    ;

    companion object {
        fun from(key: String?): Scene = entries.firstOrNull { it.key == key } ?: Interactive
    }
}

private const val TAG = "TvGuideScene"

/**
 * Logs `SCENE_READY <scene>` once [isReady] holds (the guide laid out and focus where the scene wants it) and the
 * screen has settled, or `SCENE_FAILED` when it does not happen in time. The screenshot script waits for these.
 */
@Composable
fun ReportScene(scene: Scene, isReady: () -> Boolean) {
    if (scene == Scene.Interactive) return
    LaunchedEffect(scene) {
        val ready = withTimeoutOrNull(30_000) { snapshotFlow { isReady() }.first { it } }
        if (ready == null) {
            Log.e(TAG, "SCENE_FAILED ${scene.key}: focus did not reach the expected place")
            return@LaunchedEffect
        }
        // Let scroll animations and focus styling finish.
        delay(1_500)
        withFrameNanos { }
        Log.i(TAG, "SCENE_READY ${scene.key}")
    }
}
