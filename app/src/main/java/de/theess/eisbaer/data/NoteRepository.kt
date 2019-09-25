package de.theess.eisbaer.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NoteRepository(application: Application) {
    private val dao: NoteDao

    init {
        dao = NoteDao()
    }

    fun getAll() : List<Note> {
        return dao.getAllNotes().value!!
    }

    fun query(query: String): List<Note> {
        return dao.getAllNotes().value!!.filter { note -> note.title.contains(query) }
    }

}