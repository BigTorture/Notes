package com.example.notes.data

import android.content.Context
import com.example.notes.domain.ContentItem
import com.example.notes.domain.Note
import com.example.notes.domain.NotesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val notesDao: NotesDao,
    private val imageFileManager: ImageFileManager
) : NotesRepository {

    override suspend fun addNote(
        title: String,
        content: List<ContentItem>,
        isPinned: Boolean,
        updatedAt: Long
    ) {
        val processedContent = content.toIternalStorage()
        val noteDbModel = NoteDbModel(0,title,updatedAt,isPinned)
        notesDao.addNoteWithContent(noteDbModel,processedContent)
    }


    override suspend fun deleteNote(noteId: Int) {
        val note = notesDao.getNote(noteId).toEntity()
        notesDao.deleteNote(noteId)
        note.content.filterIsInstance<ContentItem.Image>().map { it.url }.forEach {
            imageFileManager.deleteImage(it)
        }
    }

    override suspend fun editNote(note: Note) {
        val oldNote = notesDao.getNote(note.id).toEntity()
        val oldUrls = oldNote.content.filterIsInstance<ContentItem.Image>().map { it.url }
        val newUrls = note.content.filterIsInstance<ContentItem.Image>().map { it.url }

        val processedUrls = oldUrls - newUrls
        processedUrls.forEach {
            imageFileManager.deleteImage(it)
        }
        val processedContent = note.content.toIternalStorage()
        val processedNote = note.copy(content = processedContent)

        notesDao.updateNote(processedNote,processedContent)
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesDao.getAllNotes().map {
            it.toEntities()
        }
    }

    override suspend fun getNote(noteId: Int): Note {
        return notesDao.getNote(noteId).toEntity()
    }

    override fun searchNote(query: String): Flow<List<Note>> {
        return notesDao.searchNote(query).map {
            it.toEntities()
        }
    }

    override suspend fun switchPinnedStatus(noteId: Int) {
        return notesDao.switchPinnedStatus(noteId)
    }

    private suspend fun List<ContentItem>.toIternalStorage(): List<ContentItem> {
        return map { contentItem ->
            when (contentItem) {
                is ContentItem.Image -> {
                    if (imageFileManager.isIternal(contentItem.url)) {
                        contentItem
                    } else {
                        val filePath = imageFileManager.copyImageToIternalDirectory(contentItem.url)
                        ContentItem.Image(filePath)
                    }
                }

                is ContentItem.Text -> contentItem
            }
        }
    }
}
