package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NoteDao {
    fun getAllNotes(): LiveData<List<Note>> {
        return MutableLiveData(DummyContent.ITEMS)
    }

    fun query(query: String): LiveData<List<Note>> {
        val filtered = DummyContent.ITEMS.filter { note -> note.title.contains(query) }
        return MutableLiveData(filtered)
    }
}