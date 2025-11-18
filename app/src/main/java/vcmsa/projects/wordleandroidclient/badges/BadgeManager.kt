package vcmsa.projects.wordleandroidclient.badges

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object BadgeManager {

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    //Check if badge already unlocked
    suspend fun isUnlocked(badgeId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val snap = db.collection("profiles")
            .document(uid)
            .collection("badges")
            .document(badgeId)
            .get().await()

        return snap.exists() && (snap.getBoolean("unlocked") == true)
    }

   // Attempt to unlock badge
    suspend fun unlock(badgeId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        if (isUnlocked(badgeId)) return false

        db.collection("profiles")
            .document(uid)
            .collection("badges")
            .document(badgeId)
            .set(mapOf(
                "unlocked" to true,
                "timestamp" to System.currentTimeMillis()
            )).await()

        return true
    }

    // Return list with unlocked/locked status
    suspend fun getBadgeStates(): List<BadgeDisplay> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val unlockedDocs = db.collection("profiles")
            .document(uid)
            .collection("badges")
            .get().await()

        val unlocked = unlockedDocs.documents.map { it.id }.toSet()

        return BadgeConstants.allBadges.map { badge ->
            val isUnlocked = unlocked.contains(badge.id)
            BadgeDisplay(
                badge = badge,
                unlocked = isUnlocked,
                displayIcon = if (isUnlocked) badge.iconUnlocked else badge.iconLocked
            )
        }
    }
}
