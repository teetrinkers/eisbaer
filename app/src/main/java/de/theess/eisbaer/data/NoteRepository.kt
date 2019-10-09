package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import timber.log.Timber

class NoteRepository private constructor(private val dao: NoteDao) {

    fun getAll(): LiveData<List<Note>> {
        Timber.d("getAll")
        return dao.getAllNotes()
    }

    fun query(query: String): LiveData<List<Note>> {
        Timber.d("query: $query")
        return dao.query(query)
    }

    fun getById(id: String): LiveData<Note> {
        Timber.d("getById: $id")
        return dao.getById(id)
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: NoteRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: NoteRepository(NoteDao()).also { instance = it }
            }
    }
}