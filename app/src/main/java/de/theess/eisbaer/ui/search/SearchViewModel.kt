package de.theess.eisbaer.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.theess.eisbaer.EisbaerApplication
import de.theess.eisbaer.data.Note
import de.theess.eisbaer.data.NoteRepository
import timber.log.Timber
import java.util.*

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository = NoteRepository.getInstance(application as EisbaerApplication)

    private val _query = MutableLiveData<String>()
    val query: LiveData<String>
        get() = _query

    val results: LiveData<List<Note>> = Transformations
        .switchMap(_query) { searchQuery ->
            Timber.d("switchMap: $searchQuery")
            if (searchQuery.isEmpty())
                repository.getAll()
            else
                repository.query(searchQuery)
        }

    fun query(originalInput: String) {
        Timber.d("query: $originalInput")

        val input = originalInput.toLowerCase(Locale.getDefault()).trim()
        if (input == _query.value) {
            return
        }
        _query.value = input
    }

    /**
     * Tell the content provider to refresh (e.g. download) the database file. Works only on Android 8+.
     */
    fun refresh() {
        getApplication<EisbaerApplication>().database.refresh()
    }
}
