package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NoteDao {
    fun getAllNotes() : LiveData<List<Note>> {
        return MutableLiveData(DummyContent.ITEMS)
    }
}