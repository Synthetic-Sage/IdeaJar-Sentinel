package com.example.ideajar.utils

import android.content.Context
import android.net.Uri
import com.example.ideajar.data.AppDatabase
import com.example.ideajar.data.Category
import com.example.ideajar.data.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupData(
    val notes: List<Note>,
    val categories: List<Category>,
    val version: Int = 1
)

object BackupUtils {
    private val gson = Gson()

    suspend fun createBackup(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context).noteDao()
            val notes = db.getNotesOnce()
            val categories = db.getCategoriesOnce()
            
            val backupData = BackupData(notes, categories)
            val jsonString = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            }
        }
    }

    suspend fun restoreBackup(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context).noteDao()
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        reader.readText()
                    }
                } ?: return@withContext false

                val backupData = gson.fromJson(jsonString, BackupData::class.java)
                
                // Clear existing data? Or merge?
                // For simplicity and "Restore" semantics, let's clear and replace.
                // Or maybe safer to just insert (Room OnConflictStrategy.REPLACE handles IDs).
                // But if IDs clash, we might overwrite.
                
                // Strategy: Wipe and Load is cleaner for "Restore" but dangerous.
                // Let's go with Insert/Replace. User might duplicate data if IDs are auto-generated differently
                // but since we export IDs, they should match.
                
                db.deleteAllNotes()
                db.deleteAllCategories()
                
                db.insertAllNotes(backupData.notes)
                db.insertAllCategories(backupData.categories)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
