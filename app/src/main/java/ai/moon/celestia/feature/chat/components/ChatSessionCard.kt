package ai.moon.celestia.feature.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.moon.celestia.core.data.ChatSessionDto
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatSessionCard(
    session: ChatSessionDto,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    searchQuery: String = "",
    isSelectedInMultiSelect: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val celestiaHaptics = rememberCelestiaHaptics()
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeFormatted = dateFormat.format(Date(session.lastUpdatedAt))
    val iconVector = getIconVector(session.iconName)

    val highlightedTitle = remember(session.title, searchQuery) {
        if (searchQuery.isBlank() || !session.title.contains(searchQuery, ignoreCase = true)) {
            AnnotatedString(session.title)
        } else {
            buildAnnotatedString {
                val title = session.title
                val lowerTitle = title.lowercase()
                val lowerQuery = searchQuery.lowercase()
                var startIndex = 0
                while (true) {
                    val index = lowerTitle.indexOf(lowerQuery, startIndex)
                    if (index == -1) {
                        append(title.substring(startIndex))
                        break
                    }
                    if (index > startIndex) {
                        append(title.substring(startIndex, index))
                    }
                    withStyle(SpanStyle(background = Color(0xFFFFE082), color = Color.Black, fontWeight = FontWeight.Bold)) {
                        append(title.substring(index, index + searchQuery.length))
                    }
                    startIndex = index + searchQuery.length
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        celestiaHaptics.performActionPop()
                        onClick()
                    },
                    onLongPress = {
                        celestiaHaptics.performActionPop()
                        onLongClick?.invoke()
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = when {
            isSelectedInMultiSelect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            session.pinned -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = if (isSelectedInMultiSelect || session.pinned) 1.5.dp else 0.5.dp,
            color = if (isSelectedInMultiSelect || session.pinned) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        tonalElevation = if (isSelectedInMultiSelect || session.pinned) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon Badge + Pin / Delete actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (session.pinned) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onPin,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (session.pinned) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Pin session",
                            tint = if (session.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete conversation",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Middle: Title Text
            Text(
                text = highlightedTitle,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom: Model / Tag Badge (left) & Formatted Time (right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (session.tags.isNotEmpty()) {
                    Text(
                        text = "#${session.tags.first()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = session.modelName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun getIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "Terminal" -> Icons.Default.Terminal
        "Code" -> Icons.Default.Code
        "Lightbulb" -> Icons.Default.Lightbulb
        "Translate" -> Icons.Default.Translate
        "Description" -> Icons.Default.Description
        "Search" -> Icons.Default.Search
        "Star" -> Icons.Default.Star
        "Rocket" -> Icons.Default.Rocket
        "Psychology" -> Icons.Default.Psychology
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "Book" -> Icons.AutoMirrored.Filled.MenuBook
        "Build" -> Icons.Default.Build
        "School" -> Icons.Default.School
        "Palette" -> Icons.Default.Palette
        "MusicNote" -> Icons.Default.MusicNote
        "Shield" -> Icons.Default.Security
        "Extension" -> Icons.Default.Extension
        "Headphones" -> Icons.Default.Headphones
        "Pets" -> Icons.Default.Pets
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Fastfood" -> Icons.Default.Fastfood
        "Monitor" -> Icons.Default.Monitor
        "DirectionsRun" -> Icons.AutoMirrored.Filled.DirectionsRun
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Flight" -> Icons.Default.Flight
        "CameraAlt" -> Icons.Default.CameraAlt
        "Movie" -> Icons.Default.Movie
        else -> Icons.Default.ChatBubbleOutline
    }
}
