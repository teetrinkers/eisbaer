package de.theess.eisbaer.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.theess.eisbaer.data.Note
import de.theess.eisbaer.data.NoteRepository

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allItems : LiveData<List<Note>>

    init {
        repository = NoteRepository(application)
        allItems = repository.allItems
    }
}
