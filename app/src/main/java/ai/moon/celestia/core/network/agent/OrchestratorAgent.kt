package ai.moon.celestia.core.network.agent

import android.content.Context
import ai.moon.celestia.core.network.ResearchPlan
import ai.moon.celestia.core.network.SubQuery
import ai.moon.celestia.core.util.PythonSlmEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrchestratorAgent private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: OrchestratorAgent? = null

        fun getInstance(): OrchestratorAgent {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OrchestratorAgent().also { INSTANCE = it }
            }
        }
    }

    private val pythonSlmEngine = PythonSlmEngine.getInstance()

    /**
     * Step 1: Decomposes the user's prompt into 2-4 distinct sub-queries for targeted web research.
     */
    suspend fun createResearchPlan(context: Context, userPrompt: String): ResearchPlan = withContext(Dispatchers.Default) {
        val cleanPrompt = userPrompt.trim()
        if (cleanPrompt.isBlank()) {
            return@withContext ResearchPlan(userPrompt, emptyList())
        }

        try {
            val intentResult = pythonSlmEngine.evaluateSearchIntent(context, cleanPrompt)
            val subQueries = mutableListOf<SubQuery>()

            if (intentResult.searchQueries.isNotEmpty()) {
                intentResult.searchQueries.take(3).forEach { q ->
                    subQueries.add(
                        SubQuery(
                            objective = "Research real-time facts for: $q",
                            keywords = listOf(q)
                        )
                    )
                }
            } else {
                val cleanedKeywords = cleanPrompt.replace(
                    Regex("^(can you|please|search for|search|look up|find|tell me about|what is|who is|where is|latest|current)\\s+", RegexOption.IGNORE_CASE),
                    ""
                ).trim()
                subQueries.add(
                    SubQuery(
                        objective = "Search $cleanPrompt",
                        keywords = listOf(if (cleanedKeywords.length >= 3) cleanedKeywords else cleanPrompt)
                    )
                )
            }

            ResearchPlan(
                originalPrompt = userPrompt,
                subQueries = subQueries
            )
        } catch (_: Exception) {
            ResearchPlan(
                originalPrompt = userPrompt,
                subQueries = listOf(SubQuery("Search $userPrompt", listOf(userPrompt)))
            )
        }
    }
}
