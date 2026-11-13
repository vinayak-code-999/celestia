package ai.moon.celestia.core.network.agent

import ai.moon.celestia.core.network.PrioritizedUrl
import ai.moon.celestia.core.network.SubQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class SearchDiscoveryAgent private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: SearchDiscoveryAgent? = null

        fun getInstance(): SearchDiscoveryAgent {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SearchDiscoveryAgent().also { INSTANCE = it }
            }
        }
    }

    suspend fun discoverPrioritizedUrls(subQueries: List<SubQuery>): List<PrioritizedUrl> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<PrioritizedUrl>()
        for (subQuery in subQueries) {
            val mainKeyword = subQuery.keywords.firstOrNull() ?: subQuery.objective
            val encoded = URLEncoder.encode(mainKeyword, "UTF-8")

            discovered.add(
                PrioritizedUrl(
                    url = "https://lite.duckduckgo.com/lite/?q=$encoded",
                    title = "DuckDuckGo Search: $mainKeyword",
                    snippet = "Search results for $mainKeyword",
                    subQueryObjective = subQuery.objective,
                    score = 1.0f
                )
            )
            discovered.add(
                PrioritizedUrl(
                    url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&format=json",
                    title = "Wikipedia: $mainKeyword",
                    snippet = "Wikipedia encyclopedia facts for $mainKeyword",
                    subQueryObjective = subQuery.objective,
                    score = 0.9f
                )
            )
        }
        discovered.distinctBy { it.url }.take(6)
    }
}
