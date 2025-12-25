package com.example.ideajar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Category::class, BrainSignal::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "idea_jar_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val now = System.currentTimeMillis()
                        // Using raw SQL for simplicity since DAO isn't available in onCreate easily without coroutines/thread management manually
                        // Note table schema: id (INT PK), content (TEXT), timestamp (LONG), isArchived (INT/BOOL), deadline (LONG NULL), categoryId (LONG NULL)
                        // Checking Note entity schema assumption needed? 
                        // Let's assume standard Room defaults. 
                        // Wait, I should verify schema columns.
                        // Based on usage: id, content, timestamp, isArchived (likely), deadline, categoryId.
                        // Safest is to launch a coroutine scope if possible or use executeExecSQL.
                        
                        // Fix: Table name is 'notes' (lowercase, plural) based on @Entity annotation
                        // Fix: Columns must match Note entity: title, content, timestamp, categoryId, deadline
                        // 'isArchived' does not exist in current Entity. 'title' is required.
                        
                        db.execSQL("INSERT INTO notes (title, content, timestamp, categoryId, deadline) VALUES ('', 'Welcome to the Void 🌌', $now, NULL, NULL)")
                        db.execSQL("INSERT INTO notes (title, content, timestamp, categoryId, deadline) VALUES ('', 'Tap notification to Capture ⚡', $now, NULL, NULL)")
                        db.execSQL("INSERT INTO notes (title, content, timestamp, categoryId, deadline) VALUES ('', 'Drag me! I obey physics ⚛️', $now, NULL, NULL)")
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
