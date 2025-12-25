package com.example.ideajar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ideajar.data.Category
import com.example.ideajar.data.Note
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import com.example.ideajar.ui.theme.IdeaJarTheme
import com.example.ideajar.ui.theme.NeonBlue
import com.example.ideajar.ui.theme.NeonPurple
import com.example.ideajar.ui.theme.NeonRed
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IdeaJarTheme {
                CaptureScreen(activity = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(activity: android.app.Activity) {
    val context = LocalContext.current
    val app = context.applicationContext as IdeaJarApp
    val noteDao = com.example.ideajar.data.AppDatabase.getDatabase(context).noteDao()
    val repository = app.repository
    val categoriesState = noteDao.getAllCategories().collectAsState(initial = emptyList())
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDeadline by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    var currentNoteId by remember { mutableStateOf<Long?>(null) }
    
    // Load Note if Editing
    LaunchedEffect(Unit) {
        val noteId = activity.intent.getLongExtra("note_id", -1L)
        if (noteId != -1L) {
            currentNoteId = noteId
            val note = repository.getNoteById(noteId)
            if (note != null) {
                title = note.title
                content = TextFieldValue(note.content, TextRange(note.content.length))
                selectedDeadline = note.deadline
                val category = categoriesState.value.find { it.id == note.categoryId }
                selectedCategory = category
            }
        }
    }

    // AI Prediction Hook (Only for new notes)
    LaunchedEffect(title, content.text) {
        if (currentNoteId == null && selectedCategory == null && (title.length > 3 || content.text.length > 5)) {
             delay(500) // Debounce
             val predictedId = repository.predictCategory("$title ${content.text}")
             if (predictedId != null) {
                 selectedCategory = categoriesState.value.find { it.id == predictedId }
             }
        }
    }

    // Date Picker Logic
    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            // Time Picker
            android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                    calendar.set(java.util.Calendar.MINUTE, minute)
                    selectedDeadline = calendar.timeInMillis
                },
                12, 0, false
            ).show()
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { activity.finish() }, // Tap outside to close
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}, // CRITICAL: Consume all clicks so they don't pass through
            colors = CardDefaults.cardColors(containerColor = com.example.ideajar.ui.theme.BlackBackground.copy(alpha = 0.95f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Brush.linearGradient(listOf(com.example.ideajar.ui.theme.NeonBlue, com.example.ideajar.ui.theme.NeonPurple)))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "INCOMING TRANSMISSION",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.ideajar.ui.theme.NeonBlue,
                    letterSpacing = 2.sp
                )
                
                // Top Actions Row
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .offset(y = (-40).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Creative Spark Button
                    IconButton(
                        onClick = {
                            val prompts = listOf(
                                "What if gravity reversed? 🌌",
                                "Explain it to a 5-year-old 👶", 
                                "Design a tool for... 🛠️",
                                "Write a poem about... 🖋️",
                                "Pros and Cons of... ⚖️",
                                "A day in the life of... 📅",
                                "The secret history of... 🕵️‍♀️"
                            )
                            val prompt = prompts.random()
                            val currentText = content.text
                            val newText = if (currentText.isBlank()) prompt else "$currentText\n$prompt"
                            content = TextFieldValue(newText, TextRange(newText.length))
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Star,
                            contentDescription = "Creative Spark",
                            tint = com.example.ideajar.ui.theme.NeonBlue
                        )
                    }

                    // Cancel Button
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.Gray
                        )
                    }
                }
                
                // Headline Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Signal Headline", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = com.example.ideajar.ui.theme.NeonBlue,
                        focusedBorderColor = com.example.ideajar.ui.theme.NeonBlue,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Details Input
                // Details Input
                // Details Input
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 240.dp)
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                                try {
                                    val currentText = content.text
                                    val selection = content.selection
                                    val newText = currentText.replaceRange(selection.start, selection.end, "\n")
                                    val newCursorPos = selection.start + 1
                                    content = TextFieldValue(newText, TextRange(newCursorPos))
                                    true
                                } catch (e: Exception) {
                                    // Fallback: just append if something goes wrong
                                    content = TextFieldValue(content.text + "\n", TextRange(content.text.length + 1))
                                    true
                                }
                            } else {
                                false
                            }
                        },
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                    singleLine = false,
                    maxLines = 20,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Default
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFFBB86FC)
                    ),
                    placeholder = { Text("Signal Data...", color = Color.Gray) }
                )
                
                // Meta Row: Chips + Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Chips (Horizontal Scroll)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoriesState.value.forEach { category ->
                            FilterChip(
                                selected = selectedCategory?.id == category.id,
                                onClick = { selectedCategory = category },
                                label = { Text(category.name) },
                                leadingIcon = if (selectedCategory?.id == category.id) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(category.colorHex.toInt()).copy(alpha = 0.5f),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Deadline Button
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Set Deadline",
                            tint = if (selectedDeadline != null) com.example.ideajar.ui.theme.NeonRed else Color.Gray
                        )
                    }
                }
                
                if (selectedDeadline != null) {
                    Text(
                        text = "Target: " + java.text.SimpleDateFormat("MMM dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(selectedDeadline!!)),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ideajar.ui.theme.NeonRed,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Button(
                    onClick = {
                        if (content.text.isNotBlank() || title.isNotBlank()) {
                            scope.launch {
                                var finalNoteId = currentNoteId
                                if (currentNoteId != null) {
                                    // Update Existing
                                    val updatedNote = repository.getNoteById(currentNoteId!!)?.copy(
                                        title = title,
                                        content = if (content.text.isBlank()) title else content.text,
                                        categoryId = selectedCategory?.id,
                                        deadline = selectedDeadline
                                    )
                                    if (updatedNote != null) repository.update(updatedNote)
                                } else {
                                    // Insert New
                                    val newId = repository.insert(
                                        Note(
                                            title = title,
                                            content = if (content.text.isBlank()) title else content.text,
                                            categoryId = selectedCategory?.id,
                                            deadline = selectedDeadline
                                        )
                                    )
                                    finalNoteId = newId
                                }

                                // Schedule Alarm
                                if (selectedDeadline != null && finalNoteId != null) {
                                    try {
                                         repository.scheduleDeadlineNotification(
                                            context, 
                                            Note(
                                                id = finalNoteId!!,
                                                title = title,
                                                content = if (content.text.isBlank()) title else content.text,
                                                deadline = selectedDeadline
                                            )
                                        )
                                    } catch (e: SecurityException) {
                                        android.util.Log.e("IdeaJar", "Alarm permission missing: ${e.message}")
                                    } catch (e: Exception) {
                                        android.util.Log.e("IdeaJar", "Alarm failed: ${e.message}")
                                    }
                                }
                                SoundManager.playSaveSound()
                                activity.finish()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ideajar.ui.theme.NeonBlue)
                ) {
                    Text("ENCODE SIGNAL", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
