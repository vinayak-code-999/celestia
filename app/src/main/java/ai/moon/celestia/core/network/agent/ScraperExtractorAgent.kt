package ai.moon.celestia.core.network.agent

import ai.moon.celestia.core.network.ExtractedSource
import ai.moon.celestia.core.network.PrioritizedUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ScraperExtractorAgent private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: ScraperExtractorAgent? = null

        fun getInstance(): ScraperExtractorAgent {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScraperExtractorAgent().also { INSTANCE = it }
            }
        }
    }

    /**
     * Agent 3: Fetches clean LLM-ready markdown text from prioritized URLs via Jina Reader API with direct HTML scraping fallback.
     */
    suspend fun scrapeAndExtractSources(urls: List<PrioritizedUrl>): List<ExtractedSource> = withContext(Dispatchers.IO) {
        if (urls.isEmpty()) return@withContext emptyList()

        coroutineScope {
            urls.map { prioritized ->
                async { scrapeSingleUrl(prioritized) }
            }.mapNotNull { it.await() }
        }
    }

    private fun scrapeSingleUrl(item: PrioritizedUrl): ExtractedSource? {
        // Tier 1: Try Jina Reader API (r.jina.ai)
        val jinaExtraction = scrapeViaJinaReader(item.url, item.title, item.subQueryObjective)
        if (jinaExtraction != null && jinaExtraction.markdownContent.length > 50) {
            return jinaExtraction
        }

        // Tier 2: Direct Native HTML Scraper
        val directExtraction = scrapeDirectHtml(item.url, item.title, item.subQueryObjective)
        if (directExtraction != null && directExtraction.markdownContent.length > 50) {
            return directExtraction
        }

        // Tier 3: Direct snippet fallback
        if (item.snippet.length >= 20) {
            return ExtractedSource(
                url = item.url,
                title = item.title,
                markdownContent = item.snippet,
                summaryText = item.snippet.take(250),
                subQueryObjective = item.subQueryObjective
            )
        }

        return null
    }

    private fun scrapeViaJinaReader(targetUrl: String, title: String, objective: String): ExtractedSource? {
        var conn: HttpURLConnection? = null
        try {
            val jinaUrl = URL("https://r.jina.ai/$targetUrl")
            conn = (jinaUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("X-No-Cache", "true")
            }

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val dataObj = jsonObj.optJSONObject("data")

                val fetchedTitle = dataObj?.optString("title", title)?.ifBlank { title } ?: title
                val content = dataObj?.optString("content", "")?.trim() ?: ""

                if (content.isNotBlank()) {
                    val cleanMarkdown = truncateToTokenBudget(content, maxWords = 400)
                    return ExtractedSource(
                        url = targetUrl,
                        title = fetchedTitle,
                        markdownContent = cleanMarkdown,
                        summaryText = cleanMarkdown.take(250),
                        subQueryObjective = objective
                    )
                }
            }
        } catch (_: Exception) {
        } finally {
            conn?.disconnect()
        }
        return null
    }

    private fun scrapeDirectHtml(targetUrl: String, title: String, objective: String): ExtractedSource? {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(targetUrl)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }

            if (conn.responseCode == 200) {
                val rawHtml = conn.inputStream.bufferedReader().use { it.readText() }
                val cleanText = extractMainTextFromHtml(rawHtml)
                if (cleanText.length > 50) {
                    val cleanMarkdown = truncateToTokenBudget(cleanText, maxWords = 400)
                    return ExtractedSource(
                        url = targetUrl,
                        title = title,
                        markdownContent = cleanMarkdown,
                        summaryText = cleanMarkdown.take(250),
                        subQueryObjective = objective
                    )
                }
            }
        } catch (_: Exception) {
        } finally {
            conn?.disconnect()
        }
        return null
    }

    private fun extractMainTextFromHtml(html: String): String {
        return html
            .replace(Regex("(?i)<script[^*]*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?i)<style[^*]*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?i)<nav[^*]*?</nav>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?i)<footer[^*]*?</footer>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?i)<header[^*]*?</header>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?i)<noscript[^*]*?</noscript>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</?(p|h1|h2|h3|h4|h5|h6|li|tr|div)[^>]*>"), "\n")
            .replace(Regex("<!\\[CDATA\\[(.*?)\\]\\]>", RegexOption.DOT_MATCHES_ALL), "$1")
            .replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .lines()
            .map { it.trim() }
            .filter { line ->
                line.isNotBlank() && line.length > 10 &&
                        !line.lowercase().contains("cookie") &&
                        !line.lowercase().contains("privacy policy") &&
                        !line.lowercase().contains("all rights reserved") &&
                        !line.lowercase().contains("subscribe")
            }
            .joinToString("\n")
    }

    private fun truncateToTokenBudget(text: String, maxWords: Int = 400): String {
        val lines = text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val words = mutableListOf<String>()
        for (line in lines) {
            val lineWords = line.split("\\s+".toRegex())
            words.addAll(lineWords)
            if (words.size >= maxWords) break
        }

        return words.take(maxWords).joinToString(" ")
    }
}
