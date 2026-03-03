package com.example.notes.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SwitchPinnedStatusUseCase @Inject constructor(
    private val repository: NotesRepository
){
    suspend operator fun invoke(noteId: Int){
        repository.switchPinnedStatus(noteId)
    }
}