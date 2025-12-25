package com.example.ideajar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ideajar.data.Category
import com.example.ideajar.data.NoteDao
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseEditor(
    noteDao: NoteDao,
    onBack: () -> Unit
) {
    val categoriesState = noteDao.getAllCategories().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showQnaDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as IdeaJarApp
    val repository = app.repository
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mission Control") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Gravity Well")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Service Control Section
            item {
                Text("Service Status", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("IdeaJar Service")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                             Button(
                                onClick = {
                                    val intent = android.content.Intent(context, ShakeService::class.java)
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                    android.widget.Toast.makeText(context, "Service Started", android.widget.Toast.LENGTH_LONG).show()
                                }
                            ) { Text("Start") }
                            
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(context, ShakeService::class.java).apply {
                                         action = ShakeService.ACTION_STOP
                                    }
                                    context.startService(intent)
                                    android.widget.Toast.makeText(context, "Service Stopped", android.widget.Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Stop") }
                        }
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
                        if (!alarmManager.canScheduleExactAlarms()) {
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("⚠️ Grant Alarm Permission")
                            }
                        }
                    }


                }
            }

            // 2. Gravity Wells List Section
            item {
                Text("Gravity Wells", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (categoriesState.value.isEmpty()) {
                item {
                    Text("No Black Holes found. Create one with +", color = Color.Gray, modifier = Modifier.padding(8.dp))
                }
            } else {
                items(categoriesState.value) { category ->
                    CategoryItem(
                        category = category, 
                        onEdit = { categoryToEdit = category },
                        onDelete = {
                            scope.launch {
                                noteDao.deleteCategory(category)
                            }
                        }
                    )
                }
            }

            // 3. Data Management (Export/Import)
            item {
                Text("Data Management", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        
                        val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                            uri?.let {
                                scope.launch {
                                    try {
                                        com.example.ideajar.utils.BackupUtils.createBackup(context, it)
                                        SoundManager.playWhooshSound()
                                        android.widget.Toast.makeText(context, "Universe Validated & Archived to File", android.widget.Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Archive Validation Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }

                        val loadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                            uri?.let {
                            uri?.let {
                                scope.launch {
                                    try {
                                        val success = com.example.ideajar.utils.BackupUtils.restoreBackup(context, it)
                                        if (success) {
                                            SoundManager.playWhooshSound()
                                            android.widget.Toast.makeText(context, "Universe Restored from Archives", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Restoration Failed", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Restoration Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            }
                        }

                        Button(
                            onClick = { saveLauncher.launch("universe_backup.json") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Universe to File")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { loadLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        ) {
                            Text("Load Universe from File")
                        }
                    }
                }
            }

            
            // 4. Help & About
            item {
                Text("Field Manual", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Button(
                    onClick = { showQnaDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Open Field Manual (FAQ)")
                }
            }

            // 5. App Info
            item {
                Text("System Info", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("IdeaJar v1.0 (Sentinel)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Dev: Synthetic Sage", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        OutlinedButton(
                            onClick = { uriHandler.openUri("https://github.com/Synthetic-Sage/IdeaJar-Sentinel/issues") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Warning, contentDescription = "Report Bug")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Report Anomaly")
                        }
                    }
                }
            }
        }
        }
        
        if (showQnaDialog) {
            com.example.ideajar.ui.QnaDialog(onDismiss = { showQnaDialog = false })
        }

        if (showImportDialog) {
            ImportDialog(
                onDismiss = { showImportDialog = false },
                onImport = { json ->
                    scope.launch {
                        try {
                            repository.importFromJson(json)
                            SoundManager.playWhooshSound()
                            android.widget.Toast.makeText(context, "Universe Restored Successfully", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Restoration Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                        showImportDialog = false
                    }
                }
            )
        }

        if (showAddDialog) {
            AddCategoryDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, color ->
                    scope.launch {
                        noteDao.insertCategory(
                            Category(
                                name = name,
                                colorHex = color.toArgb().toLong(),
                                xPos = Random.nextFloat() * 0.8f + 0.1f, // Safe zone: 0.1 to 0.9
                                yPos = Random.nextFloat() * 0.8f + 0.1f  // Safe zone: 0.1 to 0.9
                            )
                        )
                        SoundManager.playWhooshSound()
                        showAddDialog = false
                    }
                }
            )
        }

        if (categoryToEdit != null) {
            AddCategoryDialog(
                onDismiss = { categoryToEdit = null },
                onAdd = { name, color ->
                    scope.launch {
                        noteDao.updateCategory(
                            categoryToEdit!!.copy(name = name, colorHex = color.toArgb().toLong())
                        )
                        SoundManager.playWhooshSound()
                        categoryToEdit = null
                    }
                },
                initialName = categoryToEdit!!.name,
                initialColor = Color(categoryToEdit!!.colorHex.toInt())
            )
        }
    }


@Composable
fun CategoryItem(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(category.colorHex.toInt()))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ImportDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
             Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 Text("Import Universe", style = MaterialTheme.typography.titleLarge)
                 Text("Paste the JSON string below. WARNING: This will overwrite current data!", color = MaterialTheme.colorScheme.error)
                 
                 OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    placeholder = { Text("Paste JSON here...") }
                 )
                 
                 Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { if (text.isNotBlank()) onImport(text) }) {
                        Text("Restore")
                    }
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit, 
    onAdd: (String, Color) -> Unit,
    initialName: String = "",
    initialColor: Color? = null
) {
    var name by remember { mutableStateOf(initialName) }
    // Simple color picker: just a few presets for now
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Cyan, Color.Magenta, Color.Yellow, Color.White)
    var selectedColor by remember { mutableStateOf(initialColor ?: colors.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Create Black Hole", style = MaterialTheme.typography.titleLarge)
                
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Name (e.g. Work)") },
                    singleLine = true
                )

                Text("Select Color:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) Modifier.background(Color.Transparent) // Border effect placeholder
                                    else Modifier
                                )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        if (name.isNotBlank()) onAdd(name, selectedColor)
                    }) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
