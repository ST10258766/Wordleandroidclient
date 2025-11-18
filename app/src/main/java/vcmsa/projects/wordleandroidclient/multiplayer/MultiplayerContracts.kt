package vcmsa.projects.wordleandroidclient.multiplayer

enum class AiDifficulty { EASY, MEDIUM, HARD }

enum class MatchPhase { WAITING, COUNTDOWN, PLAYING, FINISHED, CANCELLED }

data class EndMatchSummary(
    val won: Boolean,
    val yourGuesses: Int,
    val opponentGuesses: Int?,
    val definition: String? = null,
    val synonym: String? = null
)

/**
 * One guess event stored in Firestore: rooms/{code}/events/{autoId}
 */
data class GuessEvent(
    val userId: String = "",
    val guess: String = "",
    val feedback: List<String> = emptyList(), // ["G","Y","A",...]
    val row: Int = 0,
    val ts: Long = 0L
)

/**
 * What we show in the little opponent panel (AI or friend).
 */
data class OpponentProgress(
    val lastGuess: String? = null,
    val lastFeedback: List<String>? = null,
    val row: Int = 0, // 0-based
    val status: String = "Ready"
)
