package io.github.halilozel1903.tvguide.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GuideNavigatorTest {
    private val rows = listOf(
        // 0: hourly news
        listOf(span("18:00", "19:00"), span("19:00", "20:00"), span("20:00", "21:00"), span("21:00", "22:00")),
        // 1: a film in the middle
        listOf(span("18:00", "18:30"), span("18:30", "21:30"), span("21:30", "22:00")),
        // 2: nothing in the guide window
        listOf(span("01:00", "02:00")),
        // 3: half hour shows with a gap 19:30-20:00
        listOf(span("18:00", "18:30"), span("18:30", "19:00"), span("19:00", "19:30"), span("20:00", "20:30"), span("20:30", "22:00")),
    )
    private val window = span("18:00", "22:00")
    private val nav = GuideNavigator(rows.size, window) { rows[it] }

    private fun moved(row: Int, index: Int, anchor: Long) = GuideMove.Moved(GuidePosition(row, index), anchor)

    @Test
    fun rightAndLeftInARow() {
        assertEquals(moved(0, 2, at(20)), nav.move(GuidePosition(0, 1), GuideDirection.Right, anchor = at(19)))
        assertEquals(moved(0, 0, at(18)), nav.move(GuidePosition(0, 1), GuideDirection.Left, anchor = at(19)))
    }

    @Test
    fun edgesOfARow() {
        assertEquals(GuideMove.Edge(GuideDirection.Right), nav.move(GuidePosition(0, 3), GuideDirection.Right, at(21)))
        assertEquals(GuideMove.Edge(GuideDirection.Left), nav.move(GuidePosition(0, 0), GuideDirection.Left, at(18)))
    }

    @Test
    fun downKeepsTheTimeColumn() {
        // From news 19:00 (anchor 19:00) down lands on the film, which is on at 19:00.
        assertEquals(moved(1, 1, at(19)), nav.move(GuidePosition(0, 1), GuideDirection.Down, at(19)))
        // From the film, still anchored at 19:00, down skips the empty row 2 and lands on 19:00-19:30 in row 3.
        assertEquals(moved(3, 2, at(19)), nav.move(GuidePosition(1, 1), GuideDirection.Down, at(19)))
        // And back up through the film to the 19:00 news.
        assertEquals(moved(1, 1, at(19)), nav.move(GuidePosition(3, 2), GuideDirection.Up, at(19)))
        assertEquals(moved(0, 1, at(19)), nav.move(GuidePosition(1, 1), GuideDirection.Up, at(19)))
    }

    @Test
    fun verticalMoveIntoAGapPicksTheNearest() {
        // 19:40 is in row 3's gap: 10 min after 19:00-19:30 ends, 20 min before 20:00 starts.
        assertEquals(moved(3, 2, at(19, 40)), nav.move(GuidePosition(1, 1), GuideDirection.Down, at(19, 40)))
        assertEquals(moved(3, 3, at(19, 50)), nav.move(GuidePosition(1, 1), GuideDirection.Down, at(19, 50)))
    }

    @Test
    fun verticalEdges() {
        assertEquals(GuideMove.Edge(GuideDirection.Up), nav.move(GuidePosition(0, 1), GuideDirection.Up, at(19)))
        assertEquals(GuideMove.Edge(GuideDirection.Down), nav.move(GuidePosition(3, 1), GuideDirection.Down, at(19)))
    }

    @Test
    fun anchorUsesTheVisibleStartForProgrammesThatBeganOffScreen() {
        // Moving right from 18:00-18:30 onto the film while 19:15-21:15 is on screen: anchor at 19:15, not 18:30.
        val visible = span("19:15", "21:15")
        assertEquals(moved(1, 1, at(19, 15)), nav.move(GuidePosition(1, 0), GuideDirection.Right, at(18), visible))
        assertEquals(at(20), nav.anchorFor(span("20:00", "21:00"), visible))
    }

    @Test
    fun programmesOutsideTheWindowAreSkipped() {
        val r = listOf(span("16:00", "17:00"), span("17:00", "18:30"), span("18:30", "22:00"), span("22:00", "23:00"))
        val n = GuideNavigator(1, window) { r }
        assertEquals(GuideMove.Edge(GuideDirection.Right), n.move(GuidePosition(0, 2), GuideDirection.Right, at(19)))
        // The programme that started at 17:00 is still in the window (clipped), so Left reaches it; the window clamps its anchor.
        assertEquals(moved(0, 1, at(18)), n.move(GuidePosition(0, 2), GuideDirection.Left, at(19)))
        assertEquals(GuideMove.Edge(GuideDirection.Left), n.move(GuidePosition(0, 1), GuideDirection.Left, at(18)))
    }

    @Test
    fun positionLookups() {
        assertEquals(GuidePosition(1, 1), nav.positionAt(1, at(20)))
        assertNull(nav.positionAt(2, at(20)))
        assertNull(nav.positionAt(9, at(20)))
        assertEquals(GuidePosition(3, 3), nav.positionNear(2, at(20, 10)))
        assertEquals(GuidePosition(1, 1), nav.positionNear(2, at(20, 10), step = -1))
    }

    @Test
    fun staleIndexIsRepairedFromTheAnchor() {
        assertEquals(moved(0, 2, at(20, 30)), nav.move(GuidePosition(0, 42), GuideDirection.Right, at(20, 30)))
    }
}
