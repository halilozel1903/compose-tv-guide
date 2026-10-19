package io.github.halilozel1903.tvguide.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CellLayoutTest {
    private val window = span("06:00", "12:00")
    private val scale = TimeScale(window.start, pxPerMinute = 2f)

    @Test
    fun cellInsideTheWindow() {
        val cell = assertNotNull(CellLayout.place(span("07:00", "08:30"), window, scale))
        assertEquals(120f, cell.x)
        assertEquals(180f, cell.width)
        assertEquals(300f, cell.right)
        assertFalse(cell.clippedStart)
        assertFalse(cell.clippedEnd)
    }

    @Test
    fun gapIsTakenOffTheEnd() {
        val cell = assertNotNull(CellLayout.place(span("07:00", "07:30"), window, scale, gap = 4f))
        assertEquals(120f, cell.x)
        assertEquals(56f, cell.width)
    }

    @Test
    fun clippedAtTheStartOfTheWindow() {
        val cell = assertNotNull(CellLayout.place(span("05:00", "06:30"), window, scale))
        assertEquals(0f, cell.x)
        assertEquals(60f, cell.width)
        assertTrue(cell.clippedStart)
        assertFalse(cell.clippedEnd)
        assertEquals(span("06:00", "06:30"), cell.visible)
    }

    @Test
    fun clippedAtTheEndOfTheWindow() {
        val cell = assertNotNull(CellLayout.place(span("11:30", "13:00"), window, scale))
        assertEquals(660f, cell.x)
        assertEquals(60f, cell.width)
        assertFalse(cell.clippedStart)
        assertTrue(cell.clippedEnd)
    }

    @Test
    fun clippedAtBothEnds() {
        val cell = assertNotNull(CellLayout.place(span("05:00", "13:00"), window, scale))
        assertEquals(0f, cell.x)
        assertEquals(720f, cell.width)
        assertTrue(cell.clippedStart && cell.clippedEnd)
    }

    @Test
    fun outsideOrEmptyIsNotPlaced() {
        assertNull(CellLayout.place(span("04:00", "06:00"), window, scale))
        assertNull(CellLayout.place(span("12:00", "13:00"), window, scale))
        assertNull(CellLayout.place(span("08:00", "08:00"), window, scale))
    }

    @Test
    fun tinyCellWithLargeGapHasZeroWidth() {
        val cell = assertNotNull(CellLayout.place(span("07:00", "07:01"), window, scale, gap = 8f))
        assertEquals(0f, cell.width)
    }

    @Test
    fun stickyShiftKeepsTheTitleOnScreen() {
        // Cell 100..500, viewport scrolled to 250: push content by 150.
        assertEquals(150f, CellLayout.stickyShift(cellX = 100f, cellWidth = 400f, viewportStart = 250f, minContentWidth = 120f))
        // Cell starts inside the viewport: no shift.
        assertEquals(0f, CellLayout.stickyShift(cellX = 300f, cellWidth = 400f, viewportStart = 250f, minContentWidth = 120f))
        // Almost scrolled past: stop so 120 px of content stay inside the cell.
        assertEquals(280f, CellLayout.stickyShift(cellX = 100f, cellWidth = 400f, viewportStart = 480f, minContentWidth = 120f))
        // Narrower than the minimum content: never shifted.
        assertEquals(0f, CellLayout.stickyShift(cellX = 100f, cellWidth = 80f, viewportStart = 150f, minContentWidth = 120f))
    }
}
