package com.example.notes.data

import com.example.notes.domain.ContentItem
import com.example.notes.domain.Note
import kotlinx.serialization.json.Json

fun Note.toDbModel(): NoteDbModel{
        return NoteDbModel(id,name,updatedAt,isPinned)
    }

fun List<ContentItem>.toContentItemDbModel(noteId: Int): List<ContentItemDBModel> {
    return this.mapIndexed {index, contentItem ->
       when(contentItem) {
           is ContentItem.Image -> {
               ContentItemDBModel(
                   noteId = noteId,
                   contentType = ContentType.IMAGE,
                   content = contentItem.url,
                   order = index
               )
           }
           is ContentItem.Text -> {
               ContentItemDBModel(
                   noteId = noteId,
                   contentType = ContentType.TEXT,
                   content = contentItem.content,
                   order = index
               )
           }
       }
    }
}

fun List<ContentItemDBModel>.toContentItem(): List<ContentItem> {
    return this.map {contentItemDB ->
        when(contentItemDB.contentType) {
            ContentType.TEXT -> {
                ContentItem.Text(contentItemDB.content)
            }
            ContentType.IMAGE -> {
                ContentItem.Image(contentItemDB.content)
            }
        }
    }
}

    fun NoteWithContentDbModel.toEntity(): Note {
        return Note(
            id = noteDbModel.id,
            name = noteDbModel.name,
            contentItemDBModel.toContentItem(),
            updatedAt = noteDbModel.updatedAt,
            isPinned = noteDbModel.isPinned
        )
    }

fun List<NoteWithContentDbModel>.toEntities(): List<Note> {
    return map { it.toEntity() }
}