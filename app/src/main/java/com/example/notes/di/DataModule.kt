package com.example.notes.di

import android.content.Context
import androidx.room.Room
import com.example.notes.data.NotesDao
import com.example.notes.data.NotesDatabase
import com.example.notes.data.NotesRepositoryImpl
import com.example.notes.domain.NotesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface DataModule {
    @Singleton
    @Binds
    fun bindNotesRepository(impl: NotesRepositoryImpl): NotesRepository

    companion object {
        @Singleton
        @Provides
        fun provideDataBase(@ApplicationContext context: Context): NotesDatabase {
            return Room.databaseBuilder(
                context = context,
                klass = NotesDatabase::class.java,
                name = "notes.db"
            ).fallbackToDestructiveMigration(dropAllTables = true).build()
        }

        @Singleton
        @Provides
        fun provideNotesDao(database: NotesDatabase): NotesDao {
            return database.notesDao()
        }
    }
}