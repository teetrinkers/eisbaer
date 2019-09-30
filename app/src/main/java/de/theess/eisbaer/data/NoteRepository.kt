package de.theess.eisbaer.data

import android.app.Application
import androidx.lifecycle.LiveData
import timber.log.Timber

class NoteRepository(application: Application) {
    private val dao: NoteDao

    init {
        dao = NoteDao()
    }

    fun getAll() : LiveData<List<Note>> {
        Timber.d("getAll")
        return dao.getAllNotes()
    }

    fun query(query: String): LiveData<List<Note>> {
        Timber.d("query: $query")
        return dao.query(query)
    }

}