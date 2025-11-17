package vcmsa.projects.wordleandroidclient.badges

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconUnlocked: Int,
    val iconLocked: Int
)
