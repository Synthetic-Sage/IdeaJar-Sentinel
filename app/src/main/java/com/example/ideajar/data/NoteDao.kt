package com.example.ideajar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note): Long

    @Insert
    suspend fun insertCategory(category: Category)

    @androidx.room.Delete
    suspend fun deleteCategory(category: Category)

    @androidx.room.Update
    suspend fun updateCategory(category: Category)

    @androidx.room.Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @androidx.room.Transaction
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<NoteWithCategory>>

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesOnce(): List<Category>

    @Query("SELECT * FROM notes")
    suspend fun getNotesOnce(): List<Note>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Insert
    suspend fun insertAllNotes(notes: List<Note>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<Category>)

    // AI Brain Signals
    @Query("SELECT * FROM brain_signals WHERE word = :word")
    suspend fun getBrainSignal(word: String): BrainSignal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrainSignal(signal: BrainSignal)

    @androidx.room.Update
    suspend fun update(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?
}
