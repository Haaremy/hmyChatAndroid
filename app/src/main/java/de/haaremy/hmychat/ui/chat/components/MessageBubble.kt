package de.haaremy.hmychat.ui.chat.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haaremy.hmychat.data.db.entity.MessageEntity
import de.haaremy.hmychat.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    isOwn: Boolean,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bubbleColor = if (isOwn) {
        if (isDark) BubbleSent else BubbleSentLight
    } else {
        if (isDark) BubbleReceived else BubbleReceivedLight
    }
    val textColor = if (isOwn && isDark) Color.White
    else if (isOwn) HmyPurpleDark
    else MaterialTheme.colorScheme.onSurface

    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isOwn) 16.dp else 4.dp,
        bottomEnd = if (isOwn) 4.dp else 16.dp
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (message.forwardedFromName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Forward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = textColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Weitergeleitet von ${message.forwardedFromName}",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }

                if (message.replyToContent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(textColor.copy(alpha = 0.08f))
                            .padding(8.dp)
                    ) {
                        Column {
                            if (message.replyToSenderName != null) {
                                Text(
                                    text = message.replyToSenderName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = message.replyToContent,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (!isOwn && message.senderName != null) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.isEdited) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Bearbeitet",
                            modifier = Modifier.size(12.dp),
                            tint = textColor.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = formatTime(message.createdAt),
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.5f)
                    )

                    if (isOwn) {
                        when (message.status) {
                            "sent" -> Icon(
                                Icons.Default.Done,
                                contentDescription = "Gesendet",
                                modifier = Modifier.size(14.dp),
                                tint = textColor.copy(alpha = 0.5f)
                            )
                            "delivered" -> Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Zugestellt",
                                modifier = Modifier.size(14.dp),
                                tint = StatusDelivered
                            )
                            "read" -> Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Gelesen",
                                modifier = Modifier.size(14.dp),
                                tint = StatusRead
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(isoTimestamp: String): String {
    return try {
        val instant = Instant.parse(isoTimestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (_: Exception) {
        ""
    }
}
