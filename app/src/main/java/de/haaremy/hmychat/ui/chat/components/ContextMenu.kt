package de.haaremy.hmychat.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haaremy.hmychat.data.db.entity.MessageEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageContextMenu(
    message: MessageEntity,
    isOwn: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ContextMenuItem(
                icon = Icons.AutoMirrored.Filled.Reply,
                label = "Antworten",
                onClick = {
                    onReply()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.AutoMirrored.Filled.Forward,
                label = "Weiterleiten",
                onClick = {
                    onForward()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.ContentCopy,
                label = "Kopieren",
                onClick = {
                    onCopy()
                    onDismiss()
                }
            )

            if (isOwn) {
                ContextMenuItem(
                    icon = Icons.Default.Edit,
                    label = "Bearbeiten",
                    onClick = {
                        onEdit()
                        onDismiss()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Delete,
                    label = "Löschen",
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    isDestructive = true
                )
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = contentColor
        )
    }
}
