package com.example.notes.presentation.screen.editing

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.ContentItem
import com.example.notes.domain.DeleteNoteUseCase
import com.example.notes.domain.EditNoteUseCase
import com.example.notes.domain.GetNoteUseCase
import com.example.notes.domain.Note
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditNoteViewModel.Factory::class)
class EditNoteViewModel @AssistedInject constructor(
    @Assisted("noteId")private val noteId: Int,
    private val editNote: EditNoteUseCase,
    private val getNote: GetNoteUseCase,
    private val deleteNote: DeleteNoteUseCase

) : ViewModel() {

    private val _state = MutableStateFlow<EditNoteState>(EditNoteState.Initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch{
            _state.update {
                val note = getNote(noteId)
                val content = if (note.content.lastOrNull() !is ContentItem.Text) {
                    note.content + ContentItem.Text("")
                } else {
                    note.content
                }
                EditNoteState.Editing(note.copy(content = content))
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun editNote (@Assisted("noteId")noteId: Int): EditNoteViewModel
    }

    fun processCommands(command: EditNoteCommand) {
        when (command) {
            EditNoteCommand.GoBack -> {
                _state.update { EditNoteState.Finished }
            }

            is EditNoteCommand.AddImage -> {
                _state.update { previousState ->
                    if (previousState is EditNoteState.Editing) {
                        previousState.note.content.toMutableList().apply {
                            val last = last()
                            if (last is ContentItem.Text && last.content.isBlank()) {
                                removeAt(lastIndex)
                            }
                            add(ContentItem.Image(command.uri.toString()))
                            add(ContentItem.Text(""))
                        }.let {
                            val note = previousState.note.copy(content = it)
                            previousState.copy(note)
                        }
                    } else {
                        previousState
                    }
                }
            }

            is EditNoteCommand.DeleteImage -> {
                _state.update { previousState ->
                    if (previousState is EditNoteState.Editing) {
                        val newItem = previousState.note.content.toMutableList()
                        newItem.removeAt(command.index)
                        val note = previousState.note.copy(content = newItem)
                        previousState.copy(note)
                    }else {
                        previousState
                    }
                }
            }
            is EditNoteCommand.InputContent -> {
                _state.update { previousState ->
                    if (previousState is EditNoteState.Editing) {
                        val newItem = previousState.note.content
                            .mapIndexed {index, contentItem ->
                                if (index == command.index && contentItem is ContentItem.Text) {
                                    contentItem.copy(content = command.content)
                                } else {
                                    contentItem
                                }
                            }
                        val newNote = previousState.note.copy(content = newItem)
                        previousState.copy(note = newNote)
                    } else {
                        previousState
                    }
                }
            }

            is EditNoteCommand.InputTitle -> {
                _state.update { previousState ->
                    if (previousState is EditNoteState.Editing) {
                        val newNote = previousState.note.copy(name = command.title)
                        previousState.copy(note = newNote)
                    } else {
                        previousState
                    }
                }
            }

            EditNoteCommand.Save -> {
                viewModelScope.launch {
                    _state.update { previousState ->
                        if (previousState is EditNoteState.Editing) {
                            val note = previousState.note
                            val content = note.content.filter {
                                it !is ContentItem.Text || it.content.isNotBlank()
                            }
                            editNote(note.copy(content = content))
                            EditNoteState.Finished
                        } else {
                            previousState
                        }
                    }
                }
            }

            EditNoteCommand.Delete -> {
                viewModelScope.launch {
                    _state.update {previousState ->
                        if (previousState is EditNoteState.Editing) {
                            val note = previousState.note
                            deleteNote(note.id)
                            EditNoteState.Finished
                        } else {
                            previousState
                        }
                    }
                }

            }
        }
    }
}

sealed interface EditNoteCommand {

    data class InputTitle(val title: String) : EditNoteCommand

    data class InputContent(val content: String, val index: Int) : EditNoteCommand

    data class AddImage(val uri: Uri): EditNoteCommand

    data class DeleteImage(val index: Int): EditNoteCommand
    data object Save : EditNoteCommand

    data object GoBack : EditNoteCommand

    data object Delete: EditNoteCommand
}


sealed interface EditNoteState {

    data object Initial: EditNoteState

    data class Editing(
        val note: Note
    ) : EditNoteState {

        val isSaveEnabled: Boolean
            get() {
                return when {
                    note.name.isBlank() -> false
                    note.content.isEmpty() -> false
                    else -> {
                        note.content.any {
                            it !is ContentItem.Text || it.content.isNotBlank()
                        }
                    }
                }
            }
    }

    data object Finished : EditNoteState
}