package io.github.halilozel1903.tvguide.sample

import androidx.compose.ui.graphics.Color
import io.github.halilozel1903.tvguide.core.GuideDays
import io.github.halilozel1903.tvguide.core.TimeSpan
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/** The shape drawn as a demo channel's logo. */
enum class LogoShape { Circle, Square, Triangle, Diamond, Hexagon, Ring }

/** A channel of the demo guide. Every channel is made up; the names all start with "Demo". */
data class DemoChannel(
    val id: String,
    val number: Int,
    val name: String,
    val genre: Genre,
    val color: Color,
    val logo: LogoShape,
)

/** A programme of the demo guide. Titles and descriptions are invented for the sample. */
data class DemoProgramme(
    val id: String,
    val title: String,
    val episode: String?,
    val description: String,
    val genre: Genre,
    val time: TimeSpan,
)

enum class Genre(
    val label: String,
    val minutes: List<Int>,
    val titles: List<String>,
    val descriptions: List<String>,
    val series: Boolean,
) {
    News(
        "News", listOf(30, 30, 60),
        listOf("Morning Bulletin", "World Desk", "Midday Report", "Evening Headlines", "Late Roundup", "Weather Watch", "Market Hour"),
        listOf(
            "The day's main stories, weather and sport from the demo newsroom.",
            "Headlines, interviews and a look at the week ahead.",
            "A calm half hour of news, business and the forecast.",
        ),
        series = false,
    ),
    Movies(
        "Film", listOf(95, 105, 120, 135),
        listOf("The Glass Orchard", "Northbound Signal", "A Quiet Harbour", "Paper Lanterns", "The Late Ferry", "Copper Skies", "Winter in Halden", "Salt and Ember"),
        listOf(
            "A made-up feature: two strangers share a long train ride and a secret.",
            "A made-up feature: a lighthouse keeper finds a message that changes everything.",
            "A made-up feature: a small town gets ready for its last summer festival.",
        ),
        series = false,
    ),
    Kids(
        "Kids", listOf(15, 25, 30),
        listOf("Pip and the Moon Kite", "Tiny Builders", "Bubble Lab", "Captain Crumb", "Forest Friends", "Doodle Town"),
        listOf(
            "Bright, gentle adventures for young viewers.",
            "Building, drawing and asking big questions.",
            "Songs, puzzles and a lot of bubbles.",
        ),
        series = true,
    ),
    Sports(
        "Sport", listOf(60, 90, 120),
        listOf("Matchday Live", "Track and Field Weekly", "Cup Highlights", "Mountain Bike Tour", "Sailing Stories", "Courtside"),
        listOf(
            "Live coverage, analysis and the best moments of the day.",
            "Highlights and interviews from a fictional season.",
            "A look behind the scenes with the demo sports team.",
        ),
        series = false,
    ),
    Nature(
        "Nature", listOf(45, 60),
        listOf("Wild Coasts", "Secret Lives of Owls", "River Journeys", "Desert Nights", "Whale Road", "Meadow Year"),
        listOf(
            "Quiet wildlife watching from coast to mountain.",
            "A season in the life of a fictional valley.",
            "Animals at night, filmed with infrared cameras.",
        ),
        series = true,
    ),
    Music(
        "Music", listOf(30, 60, 90),
        listOf("Studio Sessions", "Chart Rewind", "Jazz After Dark", "Acoustic Corner", "Festival Nights"),
        listOf(
            "Live sets and conversations in the studio.",
            "Songs from the last four decades, back to back.",
            "Late night sounds from the demo stage.",
        ),
        series = false,
    ),
    History(
        "History", listOf(45, 60),
        listOf("Forgotten Towns", "Engines of Change", "The Map Makers", "Inside the Archive", "Old Roads"),
        listOf(
            "Stories of places and people from long ago.",
            "How inventions changed the way we live.",
            "Letters, maps and photographs from the archive.",
        ),
        series = true,
    ),
    Food(
        "Food", listOf(30, 30, 60),
        listOf("Kitchen Basics", "Street Food Road Trip", "Bake It Simple", "Market to Table", "One Pot Wonders"),
        listOf(
            "Simple recipes for every day, step by step.",
            "A road trip in search of the best snacks.",
            "From the market stall to the dinner table.",
        ),
        series = true,
    ),
    Comedy(
        "Comedy", listOf(25, 30),
        listOf("Flatmates", "Front Desk Nights", "Office Hours", "Stand-up Showcase", "Next Door"),
        listOf(
            "Four flatmates, one bathroom and a lot of opinions.",
            "Chaos at a hotel reception after midnight.",
            "New comedians try out their first sets.",
        ),
        series = true,
    ),
    Science(
        "Science", listOf(30, 60),
        listOf("How Things Work", "Space Frontier", "Future Lab", "Tiny Machines", "Big Questions"),
        listOf(
            "Taking everyday machines apart to see what is inside.",
            "Rockets, telescopes and the people behind them.",
            "Experiments you can try at home.",
        ),
        series = true,
    ),
}

/**
 * A bundled demo guide: twelve made-up channels with generated schedules. Nothing here is real-world data; the
 * schedules are generated from a fixed seed so the same day always shows the same programmes.
 */
object DemoGuide {
    val channels: List<DemoChannel> = listOf(
        DemoChannel("news", 101, "Demo News 24", Genre.News, Color(0xFFE5484D), LogoShape.Square),
        DemoChannel("movies", 102, "Demo Movies", Genre.Movies, Color(0xFFF5A524), LogoShape.Diamond),
        DemoChannel("kids", 103, "Demo Kids", Genre.Kids, Color(0xFF46C46E), LogoShape.Circle),
        DemoChannel("sports", 104, "Demo Sports", Genre.Sports, Color(0xFF3E8BFF), LogoShape.Hexagon),
        DemoChannel("nature", 105, "Demo Nature", Genre.Nature, Color(0xFF2FB8A6), LogoShape.Triangle),
        DemoChannel("music", 106, "Demo Music", Genre.Music, Color(0xFFB57BFF), LogoShape.Ring),
        DemoChannel("history", 107, "Demo History", Genre.History, Color(0xFFC9905C), LogoShape.Square),
        DemoChannel("food", 108, "Demo Food", Genre.Food, Color(0xFFFF7A59), LogoShape.Circle),
        DemoChannel("comedy", 109, "Demo Comedy", Genre.Comedy, Color(0xFFFFD84D), LogoShape.Triangle),
        DemoChannel("science", 110, "Demo Science", Genre.Science, Color(0xFF59C2FF), LogoShape.Hexagon),
        DemoChannel("movies2", 111, "Demo Movies 2", Genre.Movies, Color(0xFFFF5FA2), LogoShape.Diamond),
        DemoChannel("news2", 112, "Demo News Local", Genre.News, Color(0xFF8E9BB0), LogoShape.Ring),
    )

    /**
     * Continuous schedules for every channel from [firstDay] to [lastDay] (plus a few hours on each side, so the
     * programmes at the window edges are cut like real ones that started the day before).
     */
    fun schedules(firstDay: LocalDate, lastDay: LocalDate, zone: ZoneId): Map<String, List<DemoProgramme>> {
        val from = GuideDays.window(firstDay, zone).start - 3 * TimeSpan.HOUR
        val to = GuideDays.window(lastDay, zone).end + 3 * TimeSpan.HOUR
        return channels.associate { channel -> channel.id to schedule(channel, from, to, seed = firstDay.toEpochDay()) }
    }

    private fun schedule(channel: DemoChannel, from: Long, to: Long, seed: Long): List<DemoProgramme> {
        val random = Random(seed * 31 + channel.number)
        val genre = channel.genre
        // Start a little before `from` at a random five minute step so channels do not all change at once.
        var start = from - random.nextInt(0, 12) * 5 * TimeSpan.MINUTE
        val result = ArrayList<DemoProgramme>()
        var i = 0
        while (start < to) {
            val minutes = genre.minutes[random.nextInt(genre.minutes.size)]
            val end = start + minutes * TimeSpan.MINUTE
            val title = genre.titles[random.nextInt(genre.titles.size)]
            val episode = if (genre.series) "S${random.nextInt(1, 6)} E${random.nextInt(1, 13)}" else null
            result += DemoProgramme(
                id = "${channel.id}-$seed-$i",
                title = title,
                episode = episode,
                description = genre.descriptions[random.nextInt(genre.descriptions.size)],
                genre = genre,
                time = TimeSpan(start, end),
            )
            start = end
            i++
        }
        return result
    }
}
