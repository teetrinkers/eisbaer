package de.theess.eisbaer.data

import android.app.Application
import androidx.lifecycle.LiveData

class NoteRepository(application: Application) {
    private val dao : NoteDao
    val allItems : LiveData<List<Note>>

    init {
        dao = NoteDao()
        allItems = dao.getAllNotes()
    }
}