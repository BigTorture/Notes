package com.example.notes.presentation.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notes.presentation.screen.creations.CreateNoteScreen
import com.example.notes.presentation.screen.editing.EditNoteScreen
import com.example.notes.presentation.screen.notes.NotesScreen
import dagger.hilt.android.AndroidEntryPoint

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route
    ) {
        composable(Screen.Notes.route) {
            NotesScreen(
                onNoteClick = {
                    navController.navigate(Screen.EditNote.createRoute(it.id))
                },
                onActionButtonClick = {
                    navController.navigate(Screen.CreateNote.route)
                }
            )
        }
        composable(Screen.CreateNote.route) {
            CreateNoteScreen (
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.EditNote.route) {
            EditNoteScreen(
                noteId = Screen.EditNote.routeToInt(it.arguments) ,

                onFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Notes: Screen("notes")

    data object CreateNote: Screen("create_note")

    data object EditNote: Screen("edit_note/{note_id}")

    fun createRoute(noteid: Int): String {
        return "edit_note/$noteid"
    }

    fun routeToInt(arguments: Bundle?): Int {
        return arguments?.getString("note_id")?.toInt() ?: 0
    }
}


//custom navigation
@Composable
fun CustomNavGraph() {
    val customScreen = remember {
        mutableStateOf<CustomScreen>(CustomScreen.Notes)
    }
    val currentScreen = customScreen.value

    when (currentScreen) {
        CustomScreen.CreateNote -> {
            CreateNoteScreen(
                onFinished = {
                    customScreen.value = CustomScreen.Notes
                }
            )
        }

        is CustomScreen.EditNote -> {
            EditNoteScreen(
                noteId = currentScreen.noteId,
                onFinished = {
                    customScreen.value = CustomScreen.Notes
                }
            )
        }

        CustomScreen.Notes -> {
            NotesScreen(
                onNoteClick = {
                    customScreen.value = CustomScreen.EditNote(it.id)
                },
                onActionButtonClick = {
                    customScreen.value = CustomScreen.CreateNote
                }
            )
        }
    }
}

sealed interface CustomScreen {
    data object Notes : CustomScreen
    data object CreateNote : CustomScreen
    data class EditNote(val noteId: Int) : CustomScreen
}