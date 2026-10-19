package io.github.halilozel1903.tvguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay

/**
 * The current time in epoch milliseconds, updated on every multiple of [stepMillis] (30 seconds by default) so the
 * now line, progress bars and live styling move without recomposing every frame.
 */
@Composable
public fun rememberGuideNow(stepMillis: Long = 30_000L): Long {
    require(stepMillis > 0) { "stepMillis must be positive" }
    val now by produceState(System.currentTimeMillis(), stepMillis) {
        while (true) {
            val current = System.currentTimeMillis()
            delay(stepMillis - current % stepMillis)
            value = System.currentTimeMillis()
        }
    }
    return now
}
