package io.github.halilozel1903.tvguide

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusRequester
import io.github.halilozel1903.tvguide.core.GuideDirection
import io.github.halilozel1903.tvguide.core.GuideMove
import io.github.halilozel1903.tvguide.core.GuidePosition
import io.github.halilozel1903.tvguide.core.GuideScroll
import io.github.halilozel1903.tvguide.core.TimeSpan
import io.github.halilozel1903.tvguide.core.CellLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Creates and remembers a [TvGuideState]. The focused channel, the focused time and the scroll position survive
 * configuration changes and process death.
 *
 * @param initialChannelIndex the channel row focused first.
 * @param initialTime the time focused first, in epoch milliseconds; `null` focuses what is on now.
 */
@Composable
public fun rememberTvGuideState(initialChannelIndex: Int = 0, initialTime: Long? = null): TvGuideState =
    rememberSaveable(saver = TvGuideState.Saver) { TvGuideState(initialChannelIndex, initialTime, restoredOffsetTime = null) }

/**
 * State of a [TvGuide]: which programme has focus (or gets it when focus comes back to the guide), the scroll
 * position on both axes, and commands to scroll to now, a time or a channel.
 *
 * Focus is tracked as a channel row, a programme key and an anchor time. The anchor is the column of time that up
 * and down keep, so moving through channels does not drift to the left or right.
 */
@Stable
public class TvGuideState internal constructor(
    initialChannelIndex: Int,
    private val initialTime: Long?,
    private val restoredOffsetTime: Long?,
) {
    /** The channel row of the focused programme (or the one focus returns to). */
    public var focusedChannelIndex: Int by mutableIntStateOf(initialChannelIndex.coerceAtLeast(0))
        private set

    /** The key of the focused programme, `null` before the guide was laid out or for an empty guide. */
    public var focusedProgrammeKey: Any? by mutableStateOf(null)
        private set

    /** The time column kept by up and down, in epoch milliseconds. */
    public var focusAnchor: Long by mutableLongStateOf(initialTime ?: 0L)
        private set

    /** `true` while a programme in the guide has focus. */
    public var hasFocus: Boolean by mutableStateOf(false)
        internal set

    /** Horizontal scroll in pixels from the start of the guide window. */
    public val horizontalOffset: Float get() = offsetX.value

    /** Vertical scroll in pixels from the first channel. */
    public val verticalOffset: Float get() = offsetY.value

    /** The time shown in the programme area, or `null` before the guide was laid out. */
    public val visibleTime: TimeSpan?
        get() {
            val m = model ?: return null
            if (viewportWidth <= 0f) return null
            return m.scale.spanOf(offsetX.value, viewportWidth)
        }

    internal var focusedIndex by mutableIntStateOf(-1)
        private set

    internal val offsetX = Animatable(0f)
    internal val offsetY = Animatable(0f)

    internal var model: GuideModel<*, *>? by mutableStateOf(null)
        private set
    internal var viewportWidth by mutableFloatStateOf(0f)
    internal var viewportHeight by mutableFloatStateOf(0f)
    internal var now by mutableLongStateOf(System.currentTimeMillis())
    internal var scrollPadding: Float = 0f
    internal var exits: Set<GuideDirection> = TvGuideDefaults.FocusExits
    internal var scope: CoroutineScope? = null

    /** The cell that should take focus as soon as it is composed and attached. */
    internal var pendingFocus: GuideCellId? by mutableStateOf(null)
        private set

    private var initialized by mutableStateOf(false)
    private var focusWhenReady = false
    private val requesters = HashMap<GuideCellId, FocusRequester>()

    /** Moves keyboard focus into the guide, onto the focused programme (what is on now, at first). */
    public fun requestFocus() {
        val id = targetId()
        if (id == null) focusWhenReady = true else pendingFocus = id
    }

    /**
     * Scrolls so the current time is a quarter of the way into the programme area. With [focus], the programme on
     * now in the focused channel becomes the focused one (and takes focus if the guide has it).
     */
    public suspend fun scrollToNow(focus: Boolean = true, animate: Boolean = true) {
        awaitLayout()
        scrollToTime(now, focus, animate)
    }

    /** Scrolls so [instant] is a quarter of the way into the programme area; see [scrollToNow]. */
    public suspend fun scrollToTime(instant: Long, focus: Boolean = true, animate: Boolean = true) {
        val m = awaitLayout()
        val time = m.window.clamp(instant).coerceAtMost(m.window.end - 1)
        if (focus) {
            focusAnchor = time
            m.navigator.positionNear(focusedChannelIndex, time)?.let { setTarget(m, it) }
                ?: m.navigator.positionNear(focusedChannelIndex, time, step = -1)?.let { setTarget(m, it) }
            if (hasFocus) pendingFocus = targetId()
        }
        val x = GuideScroll.align(m.scale.xOf(time), viewportWidth, m.contentWidth, NOW_FRACTION)
        scrollTo(x, targetRowOffset(m), animate)
    }

    /**
     * Scrolls so channel [index] is the first row. With [focus], the programme at the focus anchor time in that
     * channel becomes the focused one (and takes focus if the guide has it).
     */
    public suspend fun scrollToChannel(index: Int, focus: Boolean = true, animate: Boolean = true) {
        val m = awaitLayout()
        if (m.rowCount == 0) return
        val row = index.coerceIn(0, m.rowCount - 1)
        val y = GuideScroll.align(m.rowTop(row), viewportHeight, m.contentHeight, 0f)
        var x = offsetX.targetValue
        if (focus) {
            val position = m.navigator.positionNear(row, focusAnchor) ?: m.navigator.positionNear(row, focusAnchor, step = -1)
            if (position != null) {
                setTarget(m, position)
                x = targetColumnOffset(m)
                if (hasFocus) pendingFocus = targetId()
            }
        }
        scrollTo(x, y, animate)
    }

    /**
     * Focuses the programme with [programmeKey] in channel [channelIndex] and scrolls it into view. Does nothing when
     * that programme is not in the guide.
     */
    public suspend fun focusProgramme(channelIndex: Int, programmeKey: Any, animate: Boolean = true) {
        val m = awaitLayout()
        if (channelIndex !in 0 until m.rowCount) return
        val index = m.indexOf(channelIndex, programmeKey)
        if (index < 0) return
        val span = m.spans(channelIndex)[index]
        focusAnchor = m.navigator.anchorFor(span, m.scale.spanOf(offsetX.targetValue, viewportWidth))
        setTarget(m, GuidePosition(channelIndex, index))
        if (hasFocus) pendingFocus = targetId()
        scrollTo(targetColumnOffset(m), targetRowOffset(m), animate)
    }

    // ---- Called by TvGuide ----

    internal fun composeTime(): TimeSpan? {
        val m = model ?: return null
        if (viewportWidth <= 0f) return null
        // Half a viewport of overscan on each side so cells exist before they scroll in.
        return m.scale.spanOf(offsetX.value - viewportWidth / 2f, viewportWidth * 2f)
    }

    internal suspend fun sync(newModel: GuideModel<*, *>) {
        val old = model
        model = newModel
        if (viewportWidth <= 0f || viewportHeight <= 0f || newModel.rowCount == 0) return
        val window = newModel.window
        if (!initialized) {
            val anchor = window.clamp(initialTime ?: now).coerceAtMost(window.end - 1)
            focusAnchor = anchor
            val row = focusedChannelIndex.coerceIn(0, newModel.rowCount - 1)
            (newModel.navigator.positionNear(row, anchor) ?: newModel.navigator.positionNear(row, anchor, step = -1))
                ?.let { setTarget(newModel, it) }
            val x = restoredOffsetTime?.let { newModel.scale.xOf(it) }
                ?: GuideScroll.align(newModel.scale.xOf(anchor), viewportWidth, newModel.contentWidth, NOW_FRACTION)
            offsetX.snapTo(GuideScroll.clamp(x, newModel.contentWidth, viewportWidth))
            offsetY.snapTo(targetRowOffset(newModel, from = 0f))
            initialized = true
            if (focusWhenReady) {
                focusWhenReady = false
                pendingFocus = targetId()
            }
            return
        }
        if (old != null && old.window != window) {
            // Another day: keep the time of day and the channel.
            val anchor = window.clamp(focusAnchor + (window.start - old.window.start)).coerceAtMost(window.end - 1)
            focusAnchor = anchor
            val row = focusedChannelIndex.coerceIn(0, newModel.rowCount - 1)
            (newModel.navigator.positionNear(row, anchor) ?: newModel.navigator.positionNear(row, anchor, step = -1))
                ?.let { setTarget(newModel, it) }
            if (hasFocus) pendingFocus = targetId()
        } else {
            // New data for the same window: follow the focused programme by key, or the anchor when it is gone.
            val row = focusedChannelIndex.coerceIn(0, newModel.rowCount - 1)
            val index = newModel.indexOf(row, focusedProgrammeKey)
            val position = if (index >= 0) GuidePosition(row, index) else newModel.navigator.positionNear(row, focusAnchor)
                ?: newModel.navigator.positionNear(row, focusAnchor, step = -1)
            position?.let { setTarget(newModel, it) }
        }
        offsetX.snapTo(GuideScroll.clamp(offsetX.value, newModel.contentWidth, viewportWidth))
        offsetY.snapTo(GuideScroll.clamp(offsetY.value, newModel.contentHeight, viewportHeight))
    }

    /** Handles a D-pad press inside the guide. Returns `true` when the guide consumed it. */
    internal fun navigate(direction: GuideDirection): Boolean {
        val m = model ?: return false
        if (!initialized || viewportWidth <= 0f) return false
        val from = GuidePosition(focusedChannelIndex, focusedIndex)
        val visible = m.scale.spanOf(offsetX.targetValue, viewportWidth)
        return when (val move = m.navigator.move(from, direction, focusAnchor, visible)) {
            is GuideMove.Moved -> {
                focusAnchor = move.anchor
                setTarget(m, move.position)
                pendingFocus = targetId()
                launchScroll(targetColumnOffset(m), targetRowOffset(m))
                true
            }
            is GuideMove.Edge -> direction !in exits
        }
    }

    internal fun onCellFocused(row: Int, index: Int, key: Any) {
        val id = GuideCellId(row, key)
        if (pendingFocus == id) pendingFocus = null
        if (row == focusedChannelIndex && key == focusedProgrammeKey) return
        // Focus arrived another way (a touch, the platform's own focus search): follow it.
        val m = model ?: return
        val span = m.spans(row).getOrNull(index) ?: return
        focusAnchor = m.navigator.anchorFor(span, m.scale.spanOf(offsetX.targetValue, viewportWidth))
        setTarget(m, GuidePosition(row, index))
        launchScroll(targetColumnOffset(m), targetRowOffset(m))
    }

    internal fun register(id: GuideCellId, requester: FocusRequester) {
        requesters[id] = requester
    }

    internal fun unregister(id: GuideCellId, requester: FocusRequester) {
        if (requesters[id] === requester) requesters.remove(id)
    }

    /** The requester of the focused programme's cell when it is on screen, to restore focus into the guide. */
    internal fun entryRequester(): FocusRequester? = targetId()?.let { requesters[it] }

    internal fun isTarget(row: Int, key: Any): Boolean = row == focusedChannelIndex && key == focusedProgrammeKey

    // ---- Internals ----

    private fun targetId(): GuideCellId? = focusedProgrammeKey?.let { GuideCellId(focusedChannelIndex, it) }

    private fun setTarget(m: GuideModel<*, *>, position: GuidePosition) {
        focusedChannelIndex = position.row
        focusedIndex = position.index
        focusedProgrammeKey = m.keyAt(position.row, position.index)
    }

    private fun targetColumnOffset(m: GuideModel<*, *>): Float {
        val span = m.spans(focusedChannelIndex).getOrNull(focusedIndex) ?: return offsetX.targetValue
        val cell = CellLayout.place(span, m.window, m.scale, m.cellGap) ?: return offsetX.targetValue
        return GuideScroll.keepVisible(cell.x, cell.right, offsetX.targetValue, viewportWidth, m.contentWidth, scrollPadding)
    }

    private fun targetRowOffset(m: GuideModel<*, *>, from: Float = offsetY.targetValue): Float {
        if (m.rowCount == 0) return 0f
        val top = m.rowTop(focusedChannelIndex.coerceIn(0, m.rowCount - 1))
        return GuideScroll.keepVisible(top, top + m.rowHeight, from, viewportHeight, m.contentHeight)
    }

    private fun launchScroll(x: Float, y: Float) {
        val scope = scope ?: return
        if (x != offsetX.targetValue) scope.launch { offsetX.animateTo(x, ScrollSpec) }
        if (y != offsetY.targetValue) scope.launch { offsetY.animateTo(y, ScrollSpec) }
    }

    private suspend fun scrollTo(x: Float, y: Float, animate: Boolean) {
        if (!animate) {
            offsetX.snapTo(x)
            offsetY.snapTo(y)
            return
        }
        coroutineScope {
            launch { offsetX.animateTo(x, ScrollSpec) }
            launch { offsetY.animateTo(y, ScrollSpec) }
        }
    }

    private suspend fun awaitLayout(): GuideModel<*, *> =
        snapshotFlow { model?.takeIf { initialized && viewportWidth > 0f && viewportHeight > 0f } }
            .first { it != null }!!

    private fun saveOffsetTime(): Long {
        val m = model
        return if (m != null && initialized) m.scale.instantAt(offsetX.value) else restoredOffsetTime ?: NONE
    }

    private fun saveAnchor(): Long = if (initialized) focusAnchor else initialTime ?: NONE

    public companion object {
        private const val NONE = Long.MIN_VALUE
        private const val NOW_FRACTION = 0.25f
        private val ScrollSpec = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)

        /** Saves the focused channel, the focus anchor time and the horizontal scroll position (as a time). */
        public val Saver: Saver<TvGuideState, Any> = listSaver<TvGuideState, Long>(
            save = { listOf(it.focusedChannelIndex.toLong(), it.saveAnchor(), it.saveOffsetTime()) },
            restore = {
                TvGuideState(
                    initialChannelIndex = it[0].toInt(),
                    initialTime = it[1].takeIf { value -> value != NONE },
                    restoredOffsetTime = it[2].takeIf { value -> value != NONE },
                )
            },
        )
    }
}
