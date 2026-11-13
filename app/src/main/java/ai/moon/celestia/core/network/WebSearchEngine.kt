package ai.moon.celestia.core.network

import android.content.Context
import android.util.LruCache
import ai.moon.celestia.core.network.agent.OrchestratorAgent
import ai.moon.celestia.core.network.agent.ScraperExtractorAgent
import ai.moon.celestia.core.network.agent.SearchDiscoveryAgent
import ai.moon.celestia.core.network.agent.SynthesizerAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

data class CachedSearchResult(
    val resultText: String,
    val timestampMs: Long = System.currentTimeMillis()
)

class WebSearchEngine {

    companion object {
        private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes LRU TTL
        private val searchCache = LruCache<String, CachedSearchResult>(50)
    }

    private val orchestrator = OrchestratorAgent.getInstance()
    private val searchDiscoveryAgent = SearchDiscoveryAgent.getInstance()
    private val scraperExtractorAgent = ScraperExtractorAgent.getInstance()
    private val synthesizerAgent = SynthesizerAgent.getInstance()

    /**
     * Executes the Hierarchical Multi-Agent Deep Research System Pipeline:
     * Step 1: Orchestrator decomposes query into ResearchPlan.
     * Step 2: SearchDiscoveryAgent queries web APIs & prioritizes URLs.
     * Step 3: ScraperExtractorAgent fetches clean markdown via Jina Reader r.jina.ai / direct HTML scraper.
     * Step 4: SynthesizerAgent verifies facts & generates cited report.
     */
    suspend fun searchWithMiniModel(
        context: Context,
        userQuery: String,
        onProgress: ((ai.moon.celestia.feature.chat.ChatUiState) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        if (userQuery.isBlank()) return@withContext ""

        val startTimeMs = System.currentTimeMillis()

        // Check LRU Cache
        val cacheKey = userQuery.trim().lowercase()
        val cached = searchCache.get(cacheKey)
        if (cached != null && (System.currentTimeMillis() - cached.timestampMs) < CACHE_TTL_MS) {
            return@withContext cached.resultText
        }

        val report = try {
            // Step 1: Orchestrator generates ResearchPlan
            onProgress?.invoke(ai.moon.celestia.feature.chat.ChatUiState.SearchingWeb("Evaluating intent & decomposing query..."))
            val plan = orchestrator.createResearchPlan(context, userQuery)

            // Step 2: Search & Discovery Agent finds candidate URLs
            onProgress?.invoke(ai.moon.celestia.feature.chat.ChatUiState.SearchingWeb("Discovering candidate URLs across web sources..."))
            val prioritizedUrls = searchDiscoveryAgent.discoverPrioritizedUrls(plan.subQueries)

            // Step 3: Scraper Agent extracts clean markdown via Jina Reader API / direct HTML
            onProgress?.invoke(ai.moon.celestia.feature.chat.ChatUiState.ScrapeAndExtract(prioritizedUrls.size))
            val extractedSources = scraperExtractorAgent.scrapeAndExtractSources(prioritizedUrls)

            // Step 4: Synthesizer Agent verifies facts & formats report with citations
            onProgress?.invoke(ai.moon.celestia.feature.chat.ChatUiState.Synthesizing(extractedSources.size))
            synthesizerAgent.synthesizeReport(userQuery, extractedSources, startTimeMs)
        } catch (_: Exception) {
            null
        }

        if (report != null && report.finalReportText.isNotBlank()) {
            searchCache.put(cacheKey, CachedSearchResult(report.finalReportText))
            return@withContext report.finalReportText
        }

        // Fallback: Direct search if multi-agent report was empty
        onProgress?.invoke(ai.moon.celestia.feature.chat.ChatUiState.SearchingWeb("Executing fallback direct search..."))
        val fallbackResult = search(userQuery)
        if (fallbackResult.isNotBlank()) {
            searchCache.put(cacheKey, CachedSearchResult(fallbackResult))
        }
        fallbackResult
    }

    suspend fun search(query: String): String = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext ""

        val cleanQuery = cleanSearchQuery(query)
        val cacheKey = "raw_$cleanQuery".lowercase()
        val cached = searchCache.get(cacheKey)
        if (cached != null && (System.currentTimeMillis() - cached.timestampMs) < CACHE_TTL_MS) {
            return@withContext cached.resultText
        }

        val raw = fetchRawSnippetsForQuery(cleanQuery)
        val cleanSnippets = filterAndDeduplicateSnippets(raw)

        val result = if (cleanSnippets.isNotEmpty()) {
            "REAL-TIME VERIFIED WEB SEARCH FACTS:\n" + cleanSnippets.take(5).mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")
        } else {
            ""
        }

        if (result.isNotBlank()) {
            searchCache.put(cacheKey, CachedSearchResult(result))
        }

        result
    }

    private suspend fun fetchRawSnippetsForQuery(searchQuery: String): List<String> = coroutineScope {
        val ddgLiteJob = async { withTimeoutOrNull(4000L) { searchDuckDuckGoLite(searchQuery) } ?: emptyList() }
        val ddgApiJob = async { withTimeoutOrNull(4000L) { searchDuckDuckGoApi(searchQuery) } ?: emptyList() }
        val googleNewsJob = async { withTimeoutOrNull(4000L) { searchGoogleNewsRss(searchQuery) } ?: emptyList() }
        val bingRssJob = async { withTimeoutOrNull(4000L) { searchBingRss(searchQuery) } ?: emptyList() }
        val wikiJob = async { withTimeoutOrNull(4000L) { searchWikipediaApi(searchQuery) } ?: emptyList() }

        val ddgLiteSnippets = ddgLiteJob.await()
        val ddgApiSnippets = ddgApiJob.await()
        val newsSnippets = googleNewsJob.await()
        val bingSnippets = bingRssJob.await()
        val wikiSnippets = wikiJob.await()

        val raw = ddgLiteSnippets + ddgApiSnippets + newsSnippets + wikiSnippets + bingSnippets
        filterAndDeduplicateSnippets(raw)
    }

    private fun searchDuckDuckGoLite(query: String): List<String> {
        var conn: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://lite.duckduckgo.com/lite/")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 4000
                readTimeout = 4000
                doOutput = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            val postData = "q=$encoded"
            conn.outputStream.use { it.write(postData.toByteArray(Charsets.UTF_8)) }

            if (conn.responseCode == 200) {
                val htmlStr = conn.inputStream.bufferedReader().use { it.readText() }
                val snippets = mutableListOf<String>()

                val snippetPattern = Pattern.compile("<td[^>]*class=[\"']result-snippet[\"'][^>]*>(.*?)</td>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
                val matcher = snippetPattern.matcher(htmlStr)
                while (matcher.find() && snippets.size < 6) {
                    val rawSnippet = matcher.group(1) ?: continue
                    val clean = cleanText(rawSnippet)
                    if (clean.length >= 15) {
                        snippets.add(clean)
                    }
                }
                return snippets
            }
        } catch (_: Exception) {
        } finally {
            conn?.disconnect()
        }
        return emptyList()
    }

    private fun searchDuckDuckGoApi(query: String): List<String> {
        var conn: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&no_redirect=1")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            }

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val snippets = mutableListOf<String>()

                val abstractText = jsonObj.optString("AbstractText", "").trim()
                if (abstractText.isNotBlank()) {
                    val heading = jsonObj.optString("Heading", "")
                    snippets.add(if (heading.isNotBlank()) "$heading: $abstractText" else abstractText)
                }

                val relatedTopics = jsonObj.optJSONArray("RelatedTopics")
                if (relatedTopics != null) {
                    for (i in 0 until minOf(3, relatedTopics.length())) {
                        val topicObj = relatedTopics.optJSONObject(i)
                        val text = topicObj?.optString("Text", "")?.trim() ?: ""
                        if (text.isNotBlank() && text.length > 15) {
                            snippets.add(text)
                        }
                    }
                }
                return snippets
            }
        } catch (_: Exception) {
        } finally {
            conn?.disconnect()
        }
        return emptyList()
    }

    private fun searchGoogleNewsRss(query: String): List<String> {
        var conn: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://news.google.com/rss/search?q=$encoded&hl=en-US&gl=US&ceid=US:en")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            }

            if (conn.responseCode == 200) {
                val xmlStr = conn.inputStream.bufferedReader().use { it.readText() }
                return parseRssItems(xmlStr)
            }
        } catch (_: Exception) {
        } finally {
            conn?.disconnect()
        }
        return emptyList()
    }

    private fun searchBingRss(query: String): List<String> {
        var conn: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://www.bing.com/search?q=$encoded&format=rss")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            }

            if (conn.responseCode == 200) {
                val xmlStr = conn.inputStream.bufferedReader().use { it.readText() }
                return parseRssItems(xmlStr)
            }
        } catch (_: Exception) {
        } finally {
            conn?.disconnect()
        }
        return emptyList()
    }

    private fun searchWikipediaApi(query: String): List<String> {
        var conn: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&utf8=&format=json")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "CelestiaAI/1.0")
            }

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val queryObj = jsonObj.optJSONObject("query")
                val searchArr = queryObj?.optJSONArray("search")
                if (searchArr != null && searchArr.length() > 0) {
                    val snippets = mutableListOf<String>()
                    for (i in 0 until minOf(3, searchArr.length())) {
                        val item = searchArr.getJSONObject(i)
                        val title = item.optString("title", "")
                        val snippetHtml = item.optString("snippet", "")
                        val cleanSnippet = cleanText(snippetHtml)
                        if (cleanSnippet.isNotBlank()) {
                            snippets.add("$title: $cleanSnippet")
                        }
                    }
                    return snippets
                }
            }
        } catch (_: Exception) {
        } finally {
            conn?.disconnect()
        }
        return emptyList()
    }

    private fun filterAndDeduplicateSnippets(snippets: List<String>): List<String> {
        val filtered = mutableListOf<String>()
        val seenTexts = mutableSetOf<String>()

        for (s in snippets) {
            val clean = s.trim()
            val lower = clean.lowercase()

            if (lower.contains("privacy policy") ||
                lower.contains("terms of service") ||
                lower.contains("all rights reserved") ||
                lower.contains("cookie policy") ||
                lower.contains("subscribe now") ||
                clean.length < 15
            ) {
                continue
            }

            val normalized = lower.replace(Regex("[^a-z0-9]"), " ")
            if (seenTexts.none { isSimilar(it, normalized) }) {
                seenTexts.add(normalized)
                filtered.add(clean)
            }

            if (filtered.size >= 5) break
        }

        return filtered
    }

    private fun isSimilar(text1: String, text2: String): Boolean {
        if (text1.contains(text2) || text2.contains(text1)) return true
        val words1 = text1.split(" ").filter { it.length > 3 }.toSet()
        val words2 = text2.split(" ").filter { it.length > 3 }.toSet()
        if (words1.isEmpty() || words2.isEmpty()) return false
        val intersection = words1.intersect(words2).size
        val minSize = minOf(words1.size, words2.size)
        return (intersection.toDouble() / minSize) > 0.6
    }

    private fun parseRssItems(xmlStr: String): List<String> {
        val snippets = mutableListOf<String>()
        val itemMatcher = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL).matcher(xmlStr)
        val titlePattern = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL)
        val descPattern = Pattern.compile("<description>(.*?)</description>", Pattern.DOTALL)

        while (itemMatcher.find() && snippets.size < 4) {
            val itemBlock = itemMatcher.group(1) ?: continue
            val tMatch = titlePattern.matcher(itemBlock)
            val dMatch = descPattern.matcher(itemBlock)

            val title = if (tMatch.find()) cleanText(tMatch.group(1) ?: "") else ""
            val desc = if (dMatch.find()) cleanText(dMatch.group(1) ?: "") else ""

            val combined = when {
                title.isNotBlank() && desc.isNotBlank() && !desc.contains(title) -> "$title: $desc"
                title.isNotBlank() -> title
                desc.isNotBlank() -> desc
                else -> ""
            }

            if (combined.isNotBlank()) {
                snippets.add(combined)
            }
        }
        return snippets
    }

    private fun cleanSearchQuery(query: String): String {
        val clean = query.replace(
            Regex("^(can you|please|search for|search|look up|find|tell me about|what is|who is|where is|latest|current)\\s+", RegexOption.IGNORE_CASE),
            ""
        ).trim()
        return if (clean.length >= 3) clean else query
    }

    private fun cleanText(raw: String): String {
        return raw.replace(Regex("<!\\[CDATA\\[(.*?)\\]\\]>", RegexOption.DOT_MATCHES_ALL), "$1")
            .replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .trim()
    }
}
