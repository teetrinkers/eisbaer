package de.theess.eisbaer.ui.note

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.theess.eisbaer.EisbaerApplication
import de.theess.eisbaer.data.Note
import de.theess.eisbaer.data.NoteRepository

class NoteViewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository = NoteRepository.getInstance(application as EisbaerApplication)

    fun note(id: Long): LiveData<Note> {
        return repository.getById(id)
    }
}
