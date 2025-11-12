package vcmsa.projects.wordleandroidclient.data


import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await

class WordRepository(private val wordDao: WordDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("words")

    val allWords = wordDao.getAllWords() // Flow<List<Word>>

    // Insert into Room
    suspend fun insert(word: Word) {
        wordDao.insertWord(word)
    }

    // Delete all
    suspend fun deleteAll() {
        wordDao.deleteAll()
    }

    // Upload unsynced words to Firebase
    suspend fun syncUnsyncedWords(userId: String) {
        // 1️⃣ Collect current list from Flow
        val unsyncedWords: List<Word> = wordDao.getAllWords().first() // MUST be called in suspend function

        for (word in unsyncedWords.filter { !it.isSynced }) {
            val docId = "${userId}_${word.timestamp}"
            collection.document(docId).set(word).await() // Firestore upload
            word.isSynced = true
            wordDao.insertWord(word) // update Room
        }
    }

    // Pull from Firebase
    suspend fun pullWordsFromFirebase(userId: String) {
        val snapshot = collection.get().await()
        val wordsFromFirebase = snapshot.documents.mapNotNull { it.toObject(Word::class.java) }
        wordsFromFirebase.forEach { wordDao.insertWord(it) }
    }
}
