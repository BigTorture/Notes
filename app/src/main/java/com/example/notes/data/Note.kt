package com.example.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity("notes")
data class NoteDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val updatedAt: Long,
    val isPinned: Boolean,
)