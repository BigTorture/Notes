@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.notes.presentation.screen.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.domain.GetAllNotesUseCase
import com.example.notes.domain.Note
import com.example.notes.domain.SearchNotesUseCase
import com.example.notes.domain.SwitchPinnedStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getAllNotes: GetAllNotesUseCase,
    private val searchNote: SearchNotesUseCase,
    private val switchPinnedStatus: SwitchPinnedStatusUseCase
) : ViewModel() {


    private val _state = MutableStateFlow(NotesScreenState())
    val state = _state.asStateFlow()

    private val query = MutableStateFlow("")


    init {
        query
            .onEach { input ->
                _state.update {
                    it.copy(query = input)
                }
            }.map {
                it.trim()
            }
            .flatMapLatest { input ->
                if (input.isBlank()) {
                    getAllNotes()
                } else {
                    searchNote(input)
                }
            }.onEach { notes ->
                val pinned = notes.filter { it.isPinned }
                val other = notes.filter { !it.isPinned }
                _state.update {
                    it.copy(pinnedNotes = pinned, otherNotes = other)
                }
            }.launchIn(viewModelScope)
    }

    fun processCommands(command: NotesCommand) {
        viewModelScope.launch {
            when (command) {
                is NotesCommand.InputSearchQuery -> {
                    query.update {
                        command.query
                    }
                }

                is NotesCommand.SwitchPinnedStatus -> {
                    switchPinnedStatus(command.noteId)
                }
            }
        }
    }
}

sealed interface NotesCommand {
    data class InputSearchQuery(val query: String) : NotesCommand

    data class SwitchPinnedStatus(val noteId: Int) : NotesCommand

}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf()
)