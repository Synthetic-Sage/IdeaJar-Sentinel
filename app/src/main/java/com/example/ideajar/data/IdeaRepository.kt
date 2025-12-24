package com.example.ideajar.data

import kotlinx.coroutines.flow.Flow

class IdeaRepository(private val noteDao: NoteDao) {

    private val gson = com.google.gson.Gson()
    val allNotes: Flow<List<NoteWithCategory>> = noteDao.getAllNotes()

    suspend fun insert(note: Note): Long {
        val id = noteDao.insert(note)
        // Auto-Train: If category is set, train the brain
        if (note.categoryId != null) {
            val text = "${note.title} ${note.content}"
            trainBrain(text, note.categoryId)
        }
        return id
    }

    fun scheduleDeadlineNotification(context: android.content.Context, note: Note) {
        if (note.deadline == null || note.deadline <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(context, com.example.ideajar.DeadlineReceiver::class.java).apply {
            putExtra("note_id", note.id.toInt())
            putExtra("note_content", note.title.ifBlank { note.content })
        }
        
        // Unique ID for PendingIntent based on Note ID
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            note.id.toInt(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    note.deadline,
                    pendingIntent
                )
            } else {
                // Fallback or request permission - for now just normal set
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    note.deadline,
                    pendingIntent
                )
            }
        } else {
             alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                note.deadline,
                pendingIntent
            )
        }
        
        android.util.Log.d("IdeaJar", "Scheduled Alarm for ${note.title} at ${java.util.Date(note.deadline)}")
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    // AI / Naive Bayes Implementation
    suspend fun trainBrain(text: String, categoryId: Long) {
        val words = text.lowercase().replace(Regex("[^a-z ]"), "").split(" ").filter { it.length > 3 }
        words.forEach { word ->
            val signal = noteDao.getBrainSignal(word)
            val scores: MutableMap<Long, Int> = if (signal != null) {
                val type = object : com.google.gson.reflect.TypeToken<MutableMap<Long, Int>>() {}.type
                gson.fromJson(signal.categoryScores, type) ?: mutableMapOf()
            } else {
                mutableMapOf()
            }
            scores[categoryId] = (scores[categoryId] ?: 0) + 1
            noteDao.insertBrainSignal(BrainSignal(word, gson.toJson(scores)))
        }
    }

    suspend fun predictCategory(text: String): Long? {
        val words = text.lowercase().replace(Regex("[^a-z ]"), "").split(" ").filter { it.length > 3 }
        val categoryScores = mutableMapOf<Long, Double>()

        words.forEach { word ->
            val signal = noteDao.getBrainSignal(word)
            if (signal != null) {
                val type = object : com.google.gson.reflect.TypeToken<Map<Long, Int>>() {}.type
                val scores: Map<Long, Int> = gson.fromJson(signal.categoryScores, type) ?: emptyMap()
                
                for ((catId, count) in scores) {
                    // Log Probability addition
                    categoryScores[catId] = (categoryScores[catId] ?: 0.0) + Math.log(count.toDouble() + 1)
                }
            }
        }

        return categoryScores.maxByOrNull { it.value }?.key
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
        // Re-train if needed or handle logic? For now just update.
    }

    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun exportToJson(): String {
        val categories = noteDao.getCategoriesOnce()
        val notes = noteDao.getNotesOnce()
        val backupData = BackupData(categories, notes)
        return com.google.gson.Gson().toJson(backupData)
    }

    suspend fun exportUniverse(): String {
        val categories = noteDao.getCategoriesOnce()
        val notes = noteDao.getNotesOnce()
        val backupData = BackupData(categories, notes)
        val jsonString = com.google.gson.Gson().toJson(backupData)

        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        val filename = "IdeaJar_Backup_$timestamp.json"
        
        val file = java.io.File(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
            filename
        )
        
        file.writeText(jsonString)
        return file.absolutePath
    }

    suspend fun importFromJson(jsonString: String) {
        val backupData = com.google.gson.Gson().fromJson(jsonString, BackupData::class.java)
        
        // Clear existing data
        noteDao.deleteAllNotes()
        noteDao.deleteAllCategories()

        // Insert new data
        noteDao.insertAllCategories(backupData.categories)
        noteDao.insertAllNotes(backupData.notes)
    }

    private data class BackupData(
        val categories: List<Category>,
        val notes: List<Note>
    )
}
