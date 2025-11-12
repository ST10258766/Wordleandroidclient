package vcmsa.projects.wordleandroidclient.data


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SyncManager {

    fun trySync(context: Context, userId: String, repository: WordRepository) {
        if (isOnline(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.syncUnsyncedWords(userId)
                repository.pullWordsFromFirebase(userId)
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
