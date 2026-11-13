package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

enum class ChartType {
    BAR, PIE, LINE
}

data class ChartItem(
    val label: String,
    val value: Float
)

data class ChartSpec(
    val type: ChartType,
    val title: String,
    val items: List<ChartItem>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CelestiaChartView(
    code: String,
    languageHint: String?,
    modifier: Modifier = Modifier
) {
    val celestiaHaptics = rememberCelestiaHaptics()
    val chartSpec = remember(code, languageHint) { parseChartSpec(code, languageHint) }

    if (chartSpec == null || chartSpec.items.isEmpty()) {
        // Fallback if chart parsing fails
        return
    }

    var showChartVisual by remember { mutableStateOf(true) }
    var animationProgress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ChartAnimation"
    )

    LaunchedEffect(chartSpec) {
        animationProgress = 1f
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val chartColors = listOf(
        primaryColor,
        secondaryColor,
        tertiaryColor,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = surfaceHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon: ImageVector = when (chartSpec.type) {
                        ChartType.BAR -> Icons.Default.BarChart
                        ChartType.PIE -> Icons.Default.PieChart
                        ChartType.LINE -> Icons.Default.ShowChart
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Chart Icon",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = chartSpec.title.ifBlank { "${chartSpec.type.name} CHART" }.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .applePressScale {
                                celestiaHaptics.performActionPop()
                                showChartVisual = !showChartVisual
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showChartVisual) Icons.Default.Code else Icons.Default.BarChart,
                                contentDescription = "Toggle View",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showChartVisual) "Spec" else "Visual",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    CelestiaCopyButton(textToCopy = code)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showChartVisual) {
                when (chartSpec.type) {
                    ChartType.BAR -> RenderBarChart(chartSpec, animatedProgress, chartColors)
                    ChartType.PIE -> RenderPieChart(chartSpec, animatedProgress, chartColors)
                    ChartType.LINE -> RenderLineChart(chartSpec, animatedProgress, primaryColor)
                }
            } else {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun RenderBarChart(
    spec: ChartSpec,
    progress: Float,
    colors: List<Color>
) {
    val maxValue = spec.items.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            spec.items.forEachIndexed { index, item ->
                val barColor = colors[index % colors.size]
                val targetHeightRatio = (item.value / maxValue).coerceIn(0.05f, 1f)
                val animatedHeightRatio = targetHeightRatio * progress

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (item.value % 1f == 0f) item.value.toInt().toString() else String.format("%.1f", item.value),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height((120 * animatedHeightRatio).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(barColor, barColor.copy(alpha = 0.6f))
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            spec.items.forEach { item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderPieChart(
    spec: ChartSpec,
    progress: Float,
    colors: List<Color>
) {
    val totalSum = spec.items.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                var startAngle = -90f
                spec.items.forEachIndexed { index, item ->
                    val sweepAngle = (item.value / totalSum) * 360f * progress
                    val sliceColor = colors[index % colors.size]

                    drawArc(
                        color = sliceColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Legend Items
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            spec.items.forEachIndexed { index, item ->
                val sliceColor = colors[index % colors.size]
                val pct = ((item.value / totalSum) * 100).toInt()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(sliceColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.label} ($pct%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderLineChart(
    spec: ChartSpec,
    progress: Float,
    lineColor: Color
) {
    val maxValue = spec.items.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
    val minValue = spec.items.minOfOrNull { it.value }?.coerceAtMost(0f) ?: 0f
    val valueRange = (maxValue - minValue).coerceAtLeast(1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val points = spec.items.mapIndexed { index, item ->
                    val x = if (spec.items.size > 1) (index.toFloat() / (spec.items.size - 1)) * width else width / 2
                    val normalizedY = ((item.value - minValue) / valueRange).coerceIn(0f, 1f)
                    val y = height - (normalizedY * height * progress)
                    Offset(x, y)
                }

                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                }

                points.forEach { point ->
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            spec.items.forEach { item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun parseChartSpec(code: String, languageHint: String?): ChartSpec? {
    return try {
        val trimmedCode = code.trim()

        // 1. Try JSON Parsing
        if (trimmedCode.startsWith("{")) {
            val json = JSONObject(trimmedCode)
            val typeStr = json.optString("type", "bar").lowercase()
            val title = json.optString("title", "Chart Data")
            val items = mutableListOf<ChartItem>()

            val labelsArr = json.optJSONArray("labels")
            val dataArr = json.optJSONArray("data")

            if (labelsArr != null && dataArr != null) {
                val count = minOf(labelsArr.length(), dataArr.length())
                for (i in 0 until count) {
                    val label = labelsArr.optString(i, "Item ${i + 1}")
                    val value = dataArr.optDouble(i, 0.0).toFloat()
                    items.add(ChartItem(label, value))
                }
            }

            val chartType = when (typeStr) {
                "pie", "donut" -> ChartType.PIE
                "line", "trend" -> ChartType.LINE
                else -> ChartType.BAR
            }

            return ChartSpec(chartType, title, items)
        }

        // 2. Try Key-Value / Mermaid Specs
        val lines = trimmedCode.lines().map { it.trim() }.filter { it.isNotBlank() }
        var chartType = when (languageHint?.lowercase()) {
            "pie" -> ChartType.PIE
            "line" -> ChartType.LINE
            else -> ChartType.BAR
        }
        var chartTitle = "Chart Overview"
        val items = mutableListOf<ChartItem>()

        for (line in lines) {
            val lowerLine = line.lowercase()
            if (lowerLine.startsWith("type:")) {
                val t = lowerLine.removePrefix("type:").trim()
                if (t.contains("pie")) chartType = ChartType.PIE
                else if (t.contains("line")) chartType = ChartType.LINE
                else chartType = ChartType.BAR
                continue
            }
            if (lowerLine.startsWith("title:") || lowerLine.startsWith("pie title")) {
                chartTitle = line.substringAfter("title").removePrefix(":").trim().replace("\"", "")
                continue
            }

            // Key-value pair like "Dogs: 386" or '"Cats": 85'
            val parts = line.split(":", "=")
            if (parts.size == 2) {
                val label = parts[0].trim().replace("\"", "").replace("'", "")
                val valStr = parts[1].trim().replace("\"", "").replace("'", "")
                val valFloat = valStr.toFloatOrNull()
                if (label.isNotBlank() && valFloat != null) {
                    items.add(ChartItem(label, valFloat))
                }
            }
        }

        if (items.isNotEmpty()) {
            ChartSpec(chartType, chartTitle, items)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
