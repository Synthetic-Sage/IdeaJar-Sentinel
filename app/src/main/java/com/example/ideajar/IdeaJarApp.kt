package com.example.ideajar

import android.app.Application
import com.example.ideajar.data.AppDatabase
import com.example.ideajar.data.IdeaRepository

class IdeaJarApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { IdeaRepository(database.noteDao()) }
}
