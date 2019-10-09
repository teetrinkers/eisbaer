package de.theess.eisbaer.ui.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import de.theess.eisbaer.data.Note
import de.theess.eisbaer.data.NoteRepository

class NoteViewViewModel : ViewModel() {
    private val repository: NoteRepository = NoteRepository.getInstance()

    fun note(id: String): LiveData<Note> {
        return repository.getById(id)
    }
}
