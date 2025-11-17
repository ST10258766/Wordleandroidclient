package vcmsa.projects.wordleandroidclient.badges

import vcmsa.projects.wordleandroidclient.R

object BadgeConstants {

    // Badge IDs
    const val FIRST_DAILY = "FIRST_DAILY"
    const val FIRST_SPEEDLE = "FIRST_SPEEDLE"
    const val STREAK_3 = "STREAK_3"
    const val NEVER_GIVE_UP = "NEVER_GIVE_UP"
    const val AI_CHALLENGER = "AI_CHALLENGER"
    const val NOTIFICATION_GURU = "NOTIFICATION_GURU"

    /** All badges living here */
    val allBadges = listOf(
        Badge(
            id = FIRST_DAILY,
            title = "Daily Player",
            description = "You completed your first Daily WordRush!",
            iconUnlocked = R.drawable.ic_daily_badge1,
            iconLocked = R.drawable.badge_locked // grey version
        ),
        Badge(
            id = FIRST_SPEEDLE,
            title = "SpeedSolver",
            description = "You solved your first Speedle challenge!",
            iconUnlocked = R.drawable.ic_speed_badge2,
            iconLocked = R.drawable.badge_locked
        ),
        Badge(
            id = AI_CHALLENGER,
            title = "AI Vanquisher",
            description = "You played your first AI match!",
            iconUnlocked = R.drawable.ic_ai_badge3,
            iconLocked = R.drawable.badge_locked
        ),
        Badge(
            id = STREAK_3,
            title = "Speedster Streak",
            description = "You reached a 3-day solving streak!",
            iconUnlocked = R.drawable.ic_speedster_streak_badge4,
            iconLocked = R.drawable.badge_locked
        ),
        Badge(
            id = NOTIFICATION_GURU,
            title = "Notification Guru",
            description = "You enabled daily reminders!",
            iconUnlocked = R.drawable.ic_notificationguru_badge5,
            iconLocked = R.drawable.badge_locked
        ),
        Badge(
            id = NEVER_GIVE_UP,
            title = "Never Give Up",
            description = "You won on the very last row!",
            iconUnlocked = R.drawable.ic_nevergiveup_badge6,
            iconLocked = R.drawable.badge_locked
        )
    )

    fun getBadge(badgeId: String) =
        allBadges.find { it.id == badgeId }
}
