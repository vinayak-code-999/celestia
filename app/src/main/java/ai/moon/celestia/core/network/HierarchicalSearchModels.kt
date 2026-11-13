package ai.moon.celestia.core.network

data class SubQuery(
    val objective: String,
    val keywords: List<String>
)

data class ResearchPlan(
    val originalPrompt: String,
    val subQueries: List<SubQuery>
)

data class PrioritizedUrl(
    val url: String,
    val title: String,
    val snippet: String,
    val subQueryObjective: String,
    val score: Float = 1.0f
)

data class ExtractedSource(
    val url: String,
    val title: String,
    val markdownContent: String,
    val summaryText: String,
    val subQueryObjective: String
)

data class DeepResearchReport(
    val finalReportText: String,
    val sources: List<ExtractedSource>,
    val totalSourcesScraped: Int,
    val executionTimeMs: Long = 0L
)
