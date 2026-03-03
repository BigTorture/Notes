package com.example.notes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.notes.domain.ContentItem
import com.example.notes.domain.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteWithContentDbModel>>

    @Query("SELECT * FROM notes WHERE id == :noteId")
    suspend fun getNote(noteId: Int): NoteWithContentDbModel

    @Query(
        """SELECT DISTINCT notes.* FROM notes JOIN contentNotes ON notes.id == contentNotes.noteId
        WHERE name LIKE '%' || :query || '%'
        OR content LIKE '%' || :query || '%' 
        ORDER BY updatedAt DESC"""
    )
    fun searchNote(query: String): Flow<List<NoteWithContentDbModel>>

    @Transaction
    @Query("DELETE FROM notes WHERE id == :noteId")
    suspend fun deleteNote(noteId: Int)

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id == :noteId")
    suspend fun switchPinnedStatus(noteId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(noteDbModel: NoteDbModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNoteContent(content: List<ContentItemDBModel>)

    @Query("DELETE FROM contentNotes WHERE noteId == :noteId")
    suspend fun deleteNoteContent(noteId: Int)

    @Transaction
    suspend fun addNoteWithContent(
        noteDbModel: NoteDbModel,
        content: List<ContentItem>
    ) {
        val noteId = addNote(noteDbModel).toInt()
        val contentItem = content.toContentItemDbModel(noteId)
        addNoteContent(contentItem)
    }

    @Transaction
    suspend fun updateNote(
        note: Note,
        content: List<ContentItem>
    ) {
        addNote(note.toDbModel())
        deleteNoteContent(note.id)
        addNoteContent(content.toContentItemDbModel(note.id))
    }
}