package com.example.notes.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "contentNotes",
    primaryKeys = ["noteId", "order"],
    foreignKeys = [
        ForeignKey(
            entity = NoteDbModel::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContentItemDBModel(
    val noteId: Int,
    val contentType: ContentType,
    val content: String,
    val order: Int
)

enum class ContentType() {
    TEXT, IMAGE
}