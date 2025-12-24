package com.example.ideajar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.example.ideajar.data.Note
import com.example.ideajar.data.NoteWithCategory
import com.example.ideajar.ui.theme.IdeaJarTheme
import kotlinx.coroutines.launch

enum class Screen { HOME, UNIVERSE_EDITOR }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IdeaJarTheme {
                val app = LocalContext.current.applicationContext as IdeaJarApp
                val notesState = app.repository.allNotes.collectAsState(initial = emptyList())
                val noteDao = com.example.ideajar.data.AppDatabase.getDatabase(LocalContext.current).noteDao()
                val categoriesState = noteDao.getAllCategories().collectAsState(initial = emptyList())
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences("ideajar_prefs", android.content.Context.MODE_PRIVATE) }
                
                // Initialize SoundManager
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    SoundManager.init(context)
                }
                
                var showTutorial by remember { mutableStateOf(!prefs.getBoolean("has_seen_tutorial", false)) }
                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                var showStarfield: Boolean by remember { mutableStateOf(true) }
                var selectedNote: Note? by remember { mutableStateOf<Note?>(null) }
                var noteToDelete: Note? by remember { mutableStateOf<Note?>(null) }
                var searchQuery by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()

                if (currentScreen == Screen.UNIVERSE_EDITOR) {
                    UniverseEditor(noteDao = noteDao, onBack = { currentScreen = Screen.HOME })
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                        floatingActionButton = {
                            Column(
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                // View Toggle FAB
                                androidx.compose.material3.FloatingActionButton(
                                    onClick = { showStarfield = !showStarfield },
                                    containerColor = com.example.ideajar.ui.theme.NeonPurple,
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = if (showStarfield) Icons.Filled.List else Icons.Filled.Star,
                                        contentDescription = "Toggle View"
                                    )
                                }

                                // Add Note FAB
                                androidx.compose.material3.FloatingActionButton(
                                    onClick = { 
                                        context.startActivity(Intent(context, CaptureActivity::class.java))
                                    },
                                    containerColor = com.example.ideajar.ui.theme.NeonBlue,
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                                        contentDescription = "Add Signal"
                                    )
                                }
                            }
                        },
                        topBar = {
                            androidx.compose.material3.CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        "IdeaJar",
                                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                },
                                navigationIcon = {
                                     androidx.compose.material3.IconButton(onClick = { currentScreen = Screen.UNIVERSE_EDITOR }) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Universe Editor"
                                        )
                                    }
                                },
                                actions = {
                                    androidx.compose.material3.IconButton(onClick = { showTutorial = true }) {
                                         androidx.compose.material3.Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "Help / Tutorial"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                             if (showStarfield) {
                                StarfieldView(
                                    notes = notesState.value, 
                                    categories = categoriesState.value,
                                    onNoteClick = { note -> selectedNote = note }
                                )
                            } else {
                                NoteListScreen(
                                    notes = notesState.value,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    onShare = { note ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.content}")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Share Transmission")
                                        context.startActivity(shareIntent)
                                    },
                                    onEdit = { note ->
                                        val intent = Intent(context, CaptureActivity::class.java).apply {
                                            putExtra("NOTE_ID", note.id)
                                        }
                                        context.startActivity(intent)
                                    },
                                    onDelete = { note -> noteToDelete = note }
                                )
                            }
                        }
                    }
                }

                // Delete Confirmation Dialog
                if (noteToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { noteToDelete = null },
                        title = { Text("Delete Signal?") },
                        text = { Text("Are you sure you want to delete this signal from the archives?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        noteToDelete?.let { app.repository.delete(it) }
                                        noteToDelete = null
                                    }
                                }
                            ) {
                                Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { noteToDelete = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                // Tutorial Dialog
                if (showTutorial) {
                    com.example.ideajar.ui.TutorialDialog(
                        onDismiss = {
                            showTutorial = false
                            prefs.edit().putBoolean("has_seen_tutorial", true).apply()
                        }
                    )
                }

                // Note Detail Dialog
                if (selectedNote != null) {
                    val note: Note = selectedNote!!
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    
                    com.example.ideajar.ui.NoteDetailDialog(
                        note = note,
                        onDismiss = { selectedNote = null },
                        onDelete = {
                            scope.launch {
                                app.repository.delete(note)
                                selectedNote = null
                            }
                        },
                        onCopy = { text ->
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                            android.widget.Toast.makeText(context, "Copied to Clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onShare = { title, content ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Transmission")
                            context.startActivity(shareIntent)
                        }
                    )
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen(
    notes: List<NoteWithCategory>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onShare: (Note) -> Unit,
    onEdit: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredNotes = notes.filter { 
        it.note.title.contains(searchQuery, ignoreCase = true) || 
        it.note.content.contains(searchQuery, ignoreCase = true)
    }

    // Grouping Logic:
    // 1. Critical (Has Deadline)
    // 2. Categorized
    // 3. Drifting (No Category, No Deadline)
    
    val criticalNotes = filteredNotes.filter { it.note.deadline != null }.sortedBy { it.note.deadline }
    val remainingNotes = filteredNotes.filter { it.note.deadline == null }
    val categorizedNotes = remainingNotes.filter { it.category != null }.groupBy { it.category!!.name }
    val driftingNotes = remainingNotes.filter { it.category == null }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search Mission Log...", color = androidx.compose.ui.graphics.Color.Gray) },
            leadingIcon = { 
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Search, 
                    contentDescription = "Search",
                    tint = androidx.compose.ui.graphics.Color.Gray
                ) 
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        )

        if (filteredNotes.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching signals found." else "No signals detected.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.ui.graphics.Color.DarkGray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    if (searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The Void is waiting...",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.DarkGray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                // 1. Critical Section
                if (criticalNotes.isNotEmpty()) {
                    stickyHeader {
                        MissionLogHeader(title = "CRITICAL", color = androidx.compose.ui.graphics.Color.Red)
                    }
                    items(criticalNotes.size) { index ->
                        NoteItem(
                            item = criticalNotes[index],
                            onShare = { onShare(criticalNotes[index].note) },
                            onEdit = { onEdit(criticalNotes[index].note) },
                            onDelete = { onDelete(criticalNotes[index].note) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // 2. Categorized Sections
                categorizedNotes.forEach { (categoryName, notesInGroup) ->
                    stickyHeader {
                        val catColor = notesInGroup.first().category?.colorHex?.toInt() ?: 0xFFFFFFFF.toInt()
                        MissionLogHeader(title = categoryName, color = androidx.compose.ui.graphics.Color(catColor))
                    }
                    items(notesInGroup.size) { index ->
                        NoteItem(
                            item = notesInGroup[index],
                            onShare = { onShare(notesInGroup[index].note) },
                            onEdit = { onEdit(notesInGroup[index].note) },
                            onDelete = { onDelete(notesInGroup[index].note) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // 3. Drifting Signals
                if (driftingNotes.isNotEmpty()) {
                    stickyHeader {
                        MissionLogHeader(title = "DRIFTING SIGNALS", color = androidx.compose.ui.graphics.Color.Gray)
                    }
                    items(driftingNotes.size) { index ->
                        NoteItem(
                            item = driftingNotes[index],
                            onShare = { onShare(driftingNotes[index].note) },
                            onEdit = { onEdit(driftingNotes[index].note) },
                            onDelete = { onDelete(driftingNotes[index].note) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MissionLogHeader(title: String, color: androidx.compose.ui.graphics.Color) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background.copy(alpha = 0.95f) // Slightly transparent dark bg
    ) {
        Column {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title.uppercase(),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = color,
                    letterSpacing = 2.sp
                )
            }
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp),
                color = color.copy(alpha = 0.3f)
            )
        }
    }
}



@Composable
fun NoteItem(
    item: NoteWithCategory,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.note.content,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                 Text(
                    text = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.note.timestamp)),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
                if (item.category != null) {
                    Text(
                        text = item.category.name,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color(item.category.colorHex.toInt())
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Action Buttons
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                androidx.compose.material3.IconButton(onClick = onShare) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Share,
                        contentDescription = "Share",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
                androidx.compose.material3.IconButton(onClick = onEdit) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
                androidx.compose.material3.IconButton(onClick = onDelete) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IdeaJarTheme {
        Text("Preview Not Available")
    }
}