package ai.moon.celestia.core.data

import ai.moon.celestia.core.network.OllamaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VectorSearchResult(
    val id: String,
    val text: String,
    val score: Float
)

class VectorSearchManager(
    private val apiService: OllamaApiService = OllamaApiService()
) {
    private val embeddingCache = mutableMapOf<String, List<Float>>()

    suspend fun getEmbedding(hostUrl: String, model: String, text: String): List<Float> {
        val cached = embeddingCache[text]
        if (cached != null) return cached

        val embedding = apiService.fetchEmbedding(hostUrl, model, text)
        if (embedding.isNotEmpty()) {
            embeddingCache[text] = embedding
        }
        return embedding
    }

    fun cosineSimilarity(vectorA: List<Float>, vectorB: List<Float>): Float {
        if (vectorA.size != vectorB.size || vectorA.isEmpty()) return 0f
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in vectorA.indices) {
            val a = vectorA[i]
            val b = vectorB[i]
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }

        val denominator = Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())
        return if (denominator > 0) (dotProduct / denominator).toFloat() else 0f
    }

    suspend fun rankBySimilarity(
        hostUrl: String,
        embeddingModel: String,
        query: String,
        candidates: List<Pair<String, String>> // List of Pair(Id, Content)
    ): List<VectorSearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank() || candidates.isEmpty()) return@withContext emptyList()

        val queryEmbedding = getEmbedding(hostUrl, embeddingModel, query)
        if (queryEmbedding.isEmpty()) {
            // Fallback to keyword matching if embedding model not available
            return@withContext candidates.mapNotNull { (id, text) ->
                if (text.contains(query, ignoreCase = true)) {
                    VectorSearchResult(id, text, 0.8f)
                } else null
            }
        }

        val results = mutableListOf<VectorSearchResult>()
        for ((id, content) in candidates) {
            val candEmbedding = getEmbedding(hostUrl, embeddingModel, content)
            val score = cosineSimilarity(queryEmbedding, candEmbedding)
            if (score > 0.35f) { // Threshold for relevance
                results.add(VectorSearchResult(id, content, score))
            }
        }

        results.sortedByDescending { it.score }
    }
}
