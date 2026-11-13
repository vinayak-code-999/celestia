package ai.moon.celestia.core.util

import android.content.Context
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TemporalContextUtil {

    fun getTemporalAndLocationContext(context: Context): String {
        return try {
            val now = ZonedDateTime.now()
            val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a z", Locale.US)

            val formattedDate = now.format(dateFormatter)
            val formattedTime = now.format(timeFormatter)
            val currentYear = now.year
            val timezoneId = now.zone.id

            val locale = Locale.getDefault()
            val countryName = locale.displayCountry.ifBlank { "United States" }
            val languageName = locale.displayLanguage.ifBlank { "English" }

            """
            INTERNAL SYSTEM METADATA (TEMPORAL & GEOGRAPHIC CONTEXT):
            - Internal Date Anchor: $formattedDate ($currentYear)
            - Internal Time: $formattedTime
            - Timezone: $timezoneId
            - Location Context: $countryName ($languageName)

            SILENT METADATA DIRECTIVES:
            1. INTERNAL ANCHOR ONLY: The metadata above is strictly for your internal factual alignment, timeline grounding, and year verification ($currentYear).
            2. DO NOT MENTION DATE/TIME: Never output, recite, or state the date, time, year, or location in your response preamble (e.g. do NOT start with "As of...", "Today is...", "In 2025...").
            3. ONLY MENTION IF ASKED: Only state the date/time if the user explicitly asks "what is the date?", "what time is it?", or similar queries.

            INTENT RE-ANALYSIS & ANTI-GARBAGE RESPONSE DIRECTIVE:
            1. NO CONVERSATIONAL FLUFF: Do NOT output introductory filler ("Sure!", "Here is what you asked for", "As of [Date]..."). Jump straight into the direct, useful content.
            2. PROMPT REFLECTION & THINKING: Re-analyze the user's intent to ensure 100% precision before responding.
            3. RICH FORMATTING & CHARTS: When presenting numbers, comparisons, or distributions, use ```chart, ```pie, ```bar, or ```line blocks (JSON or key-value format) so Celestia can render visual UI charts.
            """.trimIndent()
        } catch (_: Exception) {
            "CURRENT YEAR: ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}"
        }
    }

    fun buildSystemPromptWithTemporalContext(context: Context, baseSystemPrompt: String): String {
        val temporalContext = getTemporalAndLocationContext(context)
        return if (baseSystemPrompt.isNotBlank()) {
            "$baseSystemPrompt\n\n$temporalContext"
        } else {
            temporalContext
        }
    }

    fun buildWebSearchGroundedPrompt(context: Context, searchResult: String): String {
        val temporalContext = getTemporalAndLocationContext(context)

        return """
        MANDATORY DIRECTIVE: REAL-TIME WEB SEARCH IS ENABLED AND WAS EXECUTED FIRST.

        $temporalContext

        REAL-TIME VERIFIED WEB SEARCH RESULTS:
        $searchResult

        STRICT RESPONSE INSTRUCTIONS:
        1. WEB SEARCH FIRST MANDATE: Real-time web search was executed FIRST before answering. You MUST prioritize and summarize the real-time search results above all else.
        2. DO NOT MENTION METADATA: Do NOT recite or output the current date, time, or location in your response text preamble unless the user explicitly asks for it.
        3. COMPACT SHORT LINKS MANDATE: Do NOT type out long raw URLs token by token. Use short domain citations (e.g. `[domain.com](URL)` or `[1]`) so responses stay concise, readable, and stream fast.
        4. Provide an accurate, up-to-date response synthesizing the real-time web facts directly and concisely.
        """.trimIndent()
    }
}
