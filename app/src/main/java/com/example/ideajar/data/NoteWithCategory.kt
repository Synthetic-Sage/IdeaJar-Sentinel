package com.example.ideajar.data

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithCategory(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)
