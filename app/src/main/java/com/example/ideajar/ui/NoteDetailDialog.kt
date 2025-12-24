package com.example.ideajar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ideajar.data.Note
import com.example.ideajar.ui.theme.NeonBlue
import com.example.ideajar.ui.theme.NeonRed
import com.example.ideajar.ui.theme.NeonPurple

@Composable
fun NoteDetailDialog(
    note: Note,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f) // Translucent Black
            ),
            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.8f)), // Neon Border
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Category/Date) could go here, but let's stick to simple first
                
                // Title
                Text(
                    text = note.title.ifBlank { "Untitled Signal" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                if (note.deadline != null) {
                    Text(
                        text = "Target: " + java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(note.deadline)),
                        style = MaterialTheme.typography.labelMedium,
                        color = com.example.ideajar.ui.theme.NeonRed
                    )
                }

                // Body
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy
                    IconButton(onClick = { 
                        val textToCopy = if (note.title.isNotBlank()) "${note.title}\n\n${note.content}" else note.content
                        onCopy(textToCopy) 
                    }) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy", tint = NeonPurple)
                    }

                    // Share
                    IconButton(onClick = { 
                         val textToShare = if (note.title.isNotBlank()) "${note.title}\n\n${note.content}" else note.content
                         onShare(note.title.ifBlank { "IdeaJar Note" }, textToShare)
                    }) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", tint = NeonPurple)
                    }

                    // Edit
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = { 
                        onDismiss()
                        val intent = android.content.Intent(context, com.example.ideajar.CaptureActivity::class.java).apply {
                            putExtra("note_id", note.id)
                        }
                        context.startActivity(intent)
                    }) {
                         Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Delete
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = NeonRed)
                    }
                }
            }
        }
    }
}
