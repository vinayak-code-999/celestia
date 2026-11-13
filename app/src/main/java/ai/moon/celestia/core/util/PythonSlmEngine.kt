package ai.moon.celestia.core.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SearchIntentResult(
    val searchQueries: List<String>
)

class PythonSlmEngine private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: PythonSlmEngine? = null

        fun getInstance(): PythonSlmEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PythonSlmEngine().also { INSTANCE = it }
            }
        }
    }

    suspend fun evaluateSearchIntent(context: Context, prompt: String): SearchIntentResult = withContext(Dispatchers.Default) {
        val cleaned = prompt.trim()
        if (cleaned.isBlank()) {
            return@withContext SearchIntentResult(emptyList())
        }
        val queries = mutableListOf<String>()
        queries.add(cleaned)
        val noPrefix = cleaned.replace(Regex("^(what is|who is|where is|how to|tell me about|explain|find|search for)\\s+", RegexOption.IGNORE_CASE), "").trim()
        if (noPrefix.isNotBlank() && noPrefix != cleaned) {
            queries.add(noPrefix)
        }
        SearchIntentResult(queries.distinct())
    }
}
