package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.core.util.PythonExecutionEngine
import ai.moon.celestia.core.util.PythonExecutionResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun CelestiaMarkdownView(
    text: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val textColor = MaterialTheme.colorScheme.onSurface
    val celestiaHaptics = rememberCelestiaHaptics()
    val scope = rememberCoroutineScope()

    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.TableBlock -> {
                    CelestiaExcelTableView(
                        headers = block.headers,
                        rows = block.rows
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    val isChartBlock = remember(block.language, block.code) {
                        val lang = block.language?.lowercase() ?: ""
                        lang == "chart" || lang == "mermaid" || lang == "graph" || lang == "bar" || lang == "pie" || lang == "line" ||
                                (parseChartSpec(block.code, block.language) != null)
                    }

                    if (isChartBlock) {
                        CelestiaChartView(
                            code = block.code,
                            languageHint = block.language
                        )
                    } else {
                        var executionResult by remember { mutableStateOf<PythonExecutionResult?>(null) }
                        var isRunning by remember { mutableStateOf(false) }
                        val isExecutable = remember(block.language) {
                            val lang = block.language?.lowercase() ?: ""
                            lang == "python" || lang == "py" || lang == "math" || lang == "code"
                        }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceHighest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        tonalElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = (block.language ?: "CODE").uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isExecutable) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .applePressScale {
                                                    celestiaHaptics.performActionPop()
                                                    isRunning = true
                                                    scope.launch {
                                                        executionResult = PythonExecutionEngine.executePythonCode(block.code)
                                                        isRunning = false
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Run Code",
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isRunning) "Running..." else "▶ Run Code",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                )
                                            }
                                        }
                                    }

                                    CelestiaCopyButton(textToCopy = block.code)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = block.code,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 20.sp
                                    ),
                                    color = textColor
                                )
                            }

                            // Interactive Output Box for Code Execution
                            executionResult?.let { res ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (res.isSuccess) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.errorContainer,
                                    border = BorderStroke(1.dp, if (res.isSuccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (res.isSuccess) "OUTPUT (${res.executionTimeMs} ms)" else "ERROR",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (res.isSuccess) primaryColor else MaterialTheme.colorScheme.error
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (res.isSuccess) res.stdout else res.error.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            ),
                                            color = if (res.isSuccess) textColor else MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is MarkdownBlock.MathBlock -> {
                    val formattedMath = remember(block.latex) { formatLatexToUnicode(block.latex) }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        tonalElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Functions,
                                        contentDescription = "LaTeX Equation",
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LaTeX Equation",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                    )
                                }
                                CelestiaCopyButton(textToCopy = block.latex)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formattedMath,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    lineHeight = 24.sp
                                ),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    val annotatedString = remember(block.content, primaryColor) {
                        parseInlineMarkdown(block.content, primaryColor)
                    }
                    val style = when (block.type) {
                        ParagraphType.Header1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = textColor, fontSize = 18.sp)
                        ParagraphType.Header2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                        ParagraphType.Header3 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 14.sp)
                        ParagraphType.Blockquote -> MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontStyle = FontStyle.Italic, lineHeight = 20.sp, color = textColor)
                        else -> MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, color = textColor)
                    }

                    val uriHandler = LocalUriHandler.current

                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (block.type) {
                            ParagraphType.Divider -> {
                                androidx.compose.material3.HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            ParagraphType.Blockquote -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                    border = BorderStroke(2.dp, primaryColor)
                                ) {
                                    Box(modifier = Modifier.padding(10.dp)) {
                                        ClickableText(
                                            text = annotatedString,
                                            style = style,
                                            onClick = { offset ->
                                                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                                    .firstOrNull()?.let { annotation ->
                                                        try {
                                                            uriHandler.openUri(annotation.item)
                                                        } catch (_: Exception) { }
                                                    }
                                            }
                                        )
                                    }
                                }
                            }
                            else -> {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    if (block.type == ParagraphType.Bullet) {
                                        Text(
                                            text = "• ",
                                            style = style.copy(fontWeight = FontWeight.Bold, color = primaryColor)
                                        )
                                    } else if (block.prefix.isNotBlank()) {
                                        Text(
                                            text = "${block.prefix} ",
                                            style = style.copy(fontWeight = FontWeight.Bold, color = primaryColor)
                                        )
                                    }
                                    ClickableText(
                                        text = annotatedString,
                                        style = style,
                                        modifier = Modifier.weight(1f),
                                        onClick = { offset ->
                                            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    try {
                                                        uriHandler.openUri(annotation.item)
                                                    } catch (_: Exception) { }
                                                }
                                        }
                                    )
                                }
                            }
                        }

                        if (block.urls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            block.urls.distinct().forEach { url ->
                                WebLinkPreviewCard(url = url)
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface MarkdownBlock {
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
    data class MathBlock(val latex: String) : MarkdownBlock
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock
    data class Paragraph(val content: String, val type: ParagraphType, val prefix: String = "", val urls: List<String> = emptyList()) : MarkdownBlock
}

enum class ParagraphType {
    Normal, Header1, Header2, Header3, Bullet, Numbered, Blockquote, Divider
}

fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var inCodeBlock = false
    var codeLang: String? = null
    val codeBuilder = StringBuilder()

    var inMathBlock = false
    val mathBuilder = StringBuilder()

    val normalBuffer = mutableListOf<String>()

    fun flushNormal() {
        if (normalBuffer.isNotEmpty()) {
            var i = 0
            while (i < normalBuffer.size) {
                val line = normalBuffer[i]
                val trimmed = line.trim()

                if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < normalBuffer.size) {
                    val nextLine = normalBuffer[i + 1].trim()
                    if (nextLine.startsWith("|") && nextLine.contains("-")) {
                        val headers = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        val rows = mutableListOf<List<String>>()
                        var j = i + 2
                        while (j < normalBuffer.size) {
                            val rLine = normalBuffer[j].trim()
                            if (rLine.startsWith("|") && rLine.endsWith("|")) {
                                val cells = rLine.split("|").map { it.trim() }
                                val cleanCells = if (cells.size >= headers.size + 2) {
                                    cells.subList(1, cells.size - 1)
                                } else {
                                    cells.filter { it.isNotEmpty() }
                                }
                                rows.add(cleanCells)
                                j++
                            } else {
                                break
                            }
                        }
                        blocks.add(MarkdownBlock.TableBlock(headers, rows))
                        i = j
                        continue
                    }
                }

                val urlRegex = "(https?://[^\\s\\)]+)".toRegex()
                val urls = urlRegex.findAll(line).map { it.value }.toList()

                val numberedMatch = "^(\\d+[.\\)])\\s+(.*)".toRegex().find(trimmed)

                when {
                    trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                        blocks.add(MarkdownBlock.Paragraph(content = "", type = ParagraphType.Divider, urls = urls))
                    }
                    trimmed.startsWith("# ") -> blocks.add(MarkdownBlock.Paragraph(trimmed.removePrefix("# "), ParagraphType.Header1, urls = urls))
                    trimmed.startsWith("## ") -> blocks.add(MarkdownBlock.Paragraph(trimmed.removePrefix("## "), ParagraphType.Header2, urls = urls))
                    trimmed.startsWith("### ") -> blocks.add(MarkdownBlock.Paragraph(trimmed.removePrefix("### "), ParagraphType.Header3, urls = urls))
                    trimmed.startsWith("#### ") -> blocks.add(MarkdownBlock.Paragraph(trimmed.removePrefix("#### "), ParagraphType.Header3, urls = urls))
                    trimmed.startsWith("##### ") -> blocks.add(MarkdownBlock.Paragraph(trimmed.removePrefix("##### "), ParagraphType.Header3, urls = urls))
                    trimmed.startsWith("###### ") -> blocks.add(MarkdownBlock.Paragraph(trimmed.removePrefix("###### "), ParagraphType.Header3, urls = urls))
                    trimmed.startsWith("> ") -> blocks.add(MarkdownBlock.Paragraph(trimmed.removePrefix("> "), ParagraphType.Blockquote, urls = urls))
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") || trimmed.startsWith("• ") -> {
                        val clean = trimmed.removePrefix("- ").removePrefix("* ").removePrefix("+ ").removePrefix("• ")
                        blocks.add(MarkdownBlock.Paragraph(clean, ParagraphType.Bullet, urls = urls))
                    }
                    numberedMatch != null -> {
                        val numPrefix = numberedMatch.groupValues[1]
                        val content = numberedMatch.groupValues[2]
                        blocks.add(MarkdownBlock.Paragraph(content = content, type = ParagraphType.Numbered, prefix = numPrefix, urls = urls))
                    }
                    trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length > 4 -> {
                        blocks.add(MarkdownBlock.MathBlock(trimmed.removeSurrounding("$$")))
                    }
                    else -> blocks.add(MarkdownBlock.Paragraph(line, ParagraphType.Normal, urls = urls))
                }
                i++
            }
            normalBuffer.clear()
        }
    }

    for (line in lines) {
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                inCodeBlock = false
                codeLang = null
            } else {
                flushNormal()
                inCodeBlock = true
                codeLang = trimmed.removePrefix("```").trim().ifEmpty { null }
            }
            continue
        }

        if (inCodeBlock) {
            codeBuilder.append(line).append("\n")
            continue
        }

        if (trimmed == "$$" || trimmed == "\\[" || trimmed == "\\]") {
            if (inMathBlock) {
                blocks.add(MarkdownBlock.MathBlock(mathBuilder.toString().trimEnd()))
                mathBuilder.clear()
                inMathBlock = false
            } else {
                flushNormal()
                inMathBlock = true
            }
            continue
        }

        if (inMathBlock) {
            mathBuilder.append(line).append("\n")
            continue
        }

        normalBuffer.add(line)
    }

    if (inCodeBlock && codeBuilder.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
    }
    if (inMathBlock && mathBuilder.isNotEmpty()) {
        blocks.add(MarkdownBlock.MathBlock(mathBuilder.toString().trimEnd()))
    }
    flushNormal()

    return blocks
}

fun parseInlineMarkdown(text: String, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val regex = "(\\[.*?\\]\\(.*?\\)|https?://[^\\s\\)]+|\\*\\*.*?\\*\\*|\\*.*?\\*|`.*?`|\\$.*?\\$|\\\\\\(.*?\\\\\\))".toRegex()
        val matchResults = regex.findAll(text)

        var lastIndex = 0
        for (match in matchResults) {
            val range = match.range
            if (range.first > lastIndex) {
                append(text.substring(lastIndex, range.first))
            }
            val matchedText = match.value
            when {
                matchedText.startsWith("[") && matchedText.contains("](") && matchedText.endsWith(")") -> {
                    val textPart = matchedText.substringAfter("[").substringBeforeLast("](")
                    val urlPart = matchedText.substringAfterLast("](").substringBeforeLast(")")
                    pushStringAnnotation(tag = "URL", annotation = urlPart)
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold)) {
                        append(textPart)
                    }
                    pop()
                }
                matchedText.startsWith("http") -> {
                    pushStringAnnotation(tag = "URL", annotation = matchedText)
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                        append(matchedText)
                    }
                    pop()
                }
                matchedText.startsWith("**") && matchedText.endsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchedText.removeSurrounding("**"))
                    }
                }
                matchedText.startsWith("*") && matchedText.endsWith("*") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchedText.removeSurrounding("*"))
                    }
                }
                matchedText.startsWith("`") && matchedText.endsWith("`") -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)) {
                        append(matchedText.removeSurrounding("`"))
                    }
                }
                (matchedText.startsWith("$") && matchedText.endsWith("$") && matchedText.length > 2) ||
                (matchedText.startsWith("\\(") && matchedText.endsWith("\\)")) -> {
                    val rawMath = matchedText.removeSurrounding("$").removeSurrounding("\\(", "\\)")
                    val formatted = formatLatexToUnicode(rawMath)
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)) {
                        append(formatted)
                    }
                }
                else -> {
                    append(matchedText)
                }
            }
            lastIndex = range.last + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
fun WebLinkPreviewCard(url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val host = remember(url) {
        try {
            java.net.URL(url).host.removePrefix("www.")
        } catch (e: Exception) {
            url.take(25) + "..."
        }
    }

    val celestiaHaptics = rememberCelestiaHaptics()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .applePressScale {
                try {
                    celestiaHaptics.performActionPop()
                    uriHandler.openUri(url)
                } catch (e: Exception) { }
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = host.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open Link",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

fun formatLatexToUnicode(latex: String): String {
    return latex
        .replace("\\frac", "")
        .replace("\\sqrt", "√")
        .replace("\\int", "∫")
        .replace("\\sum", "∑")
        .replace("\\prod", "∏")
        .replace("\\pi", "π")
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\delta", "δ")
        .replace("\\theta", "θ")
        .replace("\\lambda", "λ")
        .replace("\\sigma", "σ")
        .replace("\\infty", "∞")
        .replace("\\approx", "≈")
        .replace("\\neq", "≠")
        .replace("\\le", "≤")
        .replace("\\ge", "≥")
        .replace("\\times", "×")
        .replace("\\div", "÷")
        .replace("\\pm", "±")
        .replace("\\cdot", "·")
        .replace("\\in", "∈")
        .replace("\\forall", "∀")
        .replace("\\exists", "∃")
        .replace("^2", "²")
        .replace("^3", "³")
        .replace("_0", "₀")
        .replace("_1", "₁")
        .replace("_2", "₂")
        .replace("{", "(")
        .replace("}", ")")
        .replace("\\", "")
        .trim()
}
