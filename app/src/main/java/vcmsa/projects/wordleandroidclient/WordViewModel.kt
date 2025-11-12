package vcmsa.projects.wordleandroidclient


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vcmsa.projects.wordleandroidclient.data.Word
import vcmsa.projects.wordleandroidclient.data.WordRepository

class WordViewModel(private val repository: WordRepository) : ViewModel() {

    val allWords = repository.allWords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun insert(word: Word) = viewModelScope.launch {
        repository.insert(word)
    }

    fun sync(userId: String) = viewModelScope.launch {
        repository.syncUnsyncedWords(userId)
        repository.pullWordsFromFirebase(userId)
    }
}

class WordViewModelFactory(private val repository: WordRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
