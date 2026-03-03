package com.example.notes.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NoteDbModel::class, ContentItemDBModel::class],
    version = 3,
    exportSchema = false
)
abstract class NotesDatabase: RoomDatabase() {
    abstract fun notesDao(): NotesDao

 }