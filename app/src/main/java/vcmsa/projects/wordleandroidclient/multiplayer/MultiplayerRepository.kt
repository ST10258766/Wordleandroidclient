package vcmsa.projects.wordleandroidclient.multiplayer

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles Firestore multiplayer rooms & guess events.
 *
 * Collection: rooms/{roomCode}
 *   - hostUid: String
 *   - hostName: String
 *   - status: "waiting" | "ready" | "cancelled"
 *   - createdAt: Long (ms)
 *
 * Subcollection: rooms/{roomCode}/players/{uid}
 *   - uid: String
 *   - displayName: String
 *   - joinedAt: Long
 *
 * Subcollection: rooms/{roomCode}/events/{autoId}
 *   - userId: String
 *   - guess: String
 *   - feedback: List<String>
 *   - row: Int
 *   - ts: Long
 */
object MultiplayerRepository {

    private val db get() = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()

    data class RoomInfo(
        val code: String,
        val hostUid: String,
        val status: String,
        val createdAt: Long
    )

    // ---------- helpers ----------
    private fun roomDoc(code: String) =
        db.collection("rooms").document(code.uppercase())

    private fun eventsCollection(code: String) =
        roomDoc(code).collection("events")

    private fun currentUserOrThrow() =
        auth.currentUser ?: throw IllegalStateException("User not logged in")

    // ---------- CREATE ROOM (HOST) ----------

    suspend fun createRoom(roomCode: String): RoomInfo {
        val user = currentUserOrThrow()
        val code = roomCode.uppercase()
        val now = System.currentTimeMillis()

        val roomRef = roomDoc(code)

        val roomData = mapOf(
            "hostUid" to user.uid,
            "hostName" to (user.displayName ?: ""),
            "status" to "waiting",
            "createdAt" to now
        )
        roomRef.set(roomData).await()

        val playerData = mapOf(
            "uid" to user.uid,
            "displayName" to (user.displayName ?: ""),
            "joinedAt" to now
        )
        roomRef.collection("players").document(user.uid).set(playerData).await()

        return RoomInfo(
            code = code,
            hostUid = user.uid,
            status = "waiting",
            createdAt = now
        )
    }

    // ---------- JOIN ROOM (FRIEND) ----------

    suspend fun joinRoom(roomCode: String): RoomInfo {
        val user = currentUserOrThrow()
        val code = roomCode.uppercase()
        val roomRef = roomDoc(code)

        val snap = roomRef.get().await()
        if (!snap.exists()) {
            throw IllegalStateException("Room does not exist")
        }

        val status = snap.getString("status") ?: "waiting"
        if (status != "waiting") {
            throw IllegalStateException("Room is not open anymore")
        }

        val now = System.currentTimeMillis()
        val playerData = mapOf(
            "uid" to user.uid,
            "displayName" to (user.displayName ?: ""),
            "joinedAt" to now
        )
        roomRef.collection("players").document(user.uid).set(playerData).await()

        // Mark room as "ready" when second player joins
        roomRef.update("status", "ready").await()

        val hostUid = snap.getString("hostUid") ?: ""
        val createdAt = snap.getLong("createdAt") ?: now

        return RoomInfo(
            code = code,
            hostUid = hostUid,
            status = "ready",
            createdAt = createdAt
        )
    }

    // ---------- LISTEN FOR PLAYERS ----------

    fun listenForPlayerCount(
        roomCode: String,
        onChange: (count: Int) -> Unit
    ): ListenerRegistration {
        val code = roomCode.uppercase()
        val roomRef = roomDoc(code)

        return roomRef.collection("players")
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                onChange(snap.size())
            }
    }

    // ---------- CLEAN UP ----------

    suspend fun cancelRoom(roomCode: String) {
        val code = roomCode.uppercase()
        roomDoc(code).delete().await()
    }

    // ---------- GUESS EVENTS (FRIENDS RACE) ----------

    fun observeEvents(code: String): Flow<GuessEvent> = callbackFlow {
        val reg = eventsCollection(code)
            .orderBy("ts")
            .addSnapshotListener { qs, _ ->
                qs?.documentChanges?.forEach { dc ->
                    val ev = dc.document.toObject<GuessEvent>()
                    trySend(ev)
                }
            }

        awaitClose { reg.remove() }
    }

    suspend fun postGuess(
        code: String,
        userId: String,
        guess: String,
        feedback: List<String>,
        row: Int
    ) {
        val ev = GuessEvent(
            userId = userId,
            guess = guess,
            feedback = feedback,
            row = row,
            ts = System.currentTimeMillis()
        )
        eventsCollection(code).add(ev).await()
    }
}
