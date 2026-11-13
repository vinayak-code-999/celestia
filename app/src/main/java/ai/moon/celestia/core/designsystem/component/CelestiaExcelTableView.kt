package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CelestiaExcelTableView(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    var showChart by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val celestiaHaptics = rememberCelestiaHaptics()

    val numericSeries = remember(headers, rows) {
        extractNumericSeries(headers, rows)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Spreadsheet Table",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Data Table (${rows.size} rows)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (numericSeries != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (showChart) primaryColor else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (showChart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .applePressScale {
                                    celestiaHaptics.performActionPop()
                                    showChart = !showChart
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (showChart) Icons.Default.TableChart else Icons.Default.BarChart,
                                    contentDescription = "Toggle Chart",
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (showChart) "Table" else "Graph",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    val csvString = remember(headers, rows) {
                        buildString {
                            append(headers.joinToString(",")).append("\n")
                            rows.forEach { row -> append(row.joinToString(",")).append("\n") }
                        }
                    }
                    CelestiaCopyButton(textToCopy = csvString)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (showChart && numericSeries != null) {
                CelestiaBarChartView(
                    title = numericSeries.title,
                    labels = numericSeries.labels,
                    values = numericSeries.values
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(vertical = 4.dp)
                        ) {
                            headers.forEachIndexed { _, header ->
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 120.dp, max = 240.dp)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = parseInlineMarkdown(header, MaterialTheme.colorScheme.onPrimaryContainer),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        softWrap = true,
                                        maxLines = Int.MAX_VALUE
                                    )
                                }
                            }
                        }

                        rows.forEachIndexed { rowIndex, row ->
                            val rowBg = if (rowIndex % 2 == 0) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 2.dp)
                            ) {
                                headers.indices.forEach { colIndex ->
                                    val cellText = row.getOrNull(colIndex) ?: ""
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = 120.dp, max = 240.dp)
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = parseInlineMarkdown(cellText, textColor),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = textColor,
                                                fontFamily = if (cellText.toDoubleOrNull() != null) FontFamily.Monospace else FontFamily.Default
                                            ),
                                            softWrap = true,
                                            maxLines = Int.MAX_VALUE
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CelestiaBarChartView(
    title: String,
    labels: List<String>,
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val maxVal = remember(values) { (values.maxOrNull() ?: 1f).coerceAtLeast(0.001f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "📊 $title Bar Graph",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            labels.zip(values).forEach { (label, valNum) ->
                val barRatio = (valNum / maxVal).coerceIn(0.05f, 1f)
                val animatedHeightRatio by animateFloatAsState(
                    targetValue = barRatio,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "barHeight"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.widthIn(min = 48.dp)
                ) {
                    Text(
                        text = if (valNum % 1f == 0f) "%.0f".format(valNum) else "%.1f".format(valNum),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height((120 * animatedHeightRatio).dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor, secondaryColor)
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class NumericSeries(val title: String, val labels: List<String>, val values: List<Float>)

fun extractNumericSeries(headers: List<String>, rows: List<List<String>>): NumericSeries? {
    if (headers.isEmpty() || rows.isEmpty()) return null

    var numColIdx = -1
    for (c in 1 until headers.size) {
        val allNum = rows.all { r ->
            val cell = r.getOrNull(c)?.replace(Regex("[^0-9.-]"), "") ?: ""
            cell.toFloatOrNull() != null
        }
        if (allNum) {
            numColIdx = c
            break
        }
    }

    if (numColIdx == -1) return null

    val labels = rows.map { it.getOrNull(0) ?: "Item" }
    val values = rows.map { r ->
        val cell = r.getOrNull(numColIdx)?.replace(Regex("[^0-9.-]"), "") ?: "0"
        cell.toFloatOrNull() ?: 0f
    }

    return NumericSeries(
        title = headers.getOrNull(numColIdx) ?: "Data Series",
        labels = labels,
        values = values
    )
}
