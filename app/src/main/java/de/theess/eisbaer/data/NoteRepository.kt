package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.theess.eisbaer.EisbaerApplication
import io.requery.kotlin.desc
import io.requery.kotlin.eq
import io.requery.kotlin.like
import timber.log.Timber

class NoteRepository private constructor(private val holder: EntityStoreHolder) {

    fun getAll(): LiveData<List<Note>> {
        Timber.d("getAll")
        return holder.store
            ?.run {
                select(Note::class)
                    .where(Note::trashed.eq(0))
                    .orderBy(Note::modificationDateRaw.desc()).limit(QUERY_LIMIT)
            }
            ?.let { MutableLiveData(it.get().toList()) }
            ?: MutableLiveData()
    }

    fun query(query: String): LiveData<List<Note>> {
        Timber.d("query: $query")
        return holder.store
            ?.run {
                select(Note::class)
                    .where(Note::content.like("%$query%")).and(Note::trashed.eq(0))
                    .orderBy(Note::modificationDateRaw.desc()).limit(QUERY_LIMIT)
            }
            ?.get()?.toList()
            .also { Timber.d("query result count: ${it?.size}") }
            ?.let { MutableLiveData(it) }
            ?: MutableLiveData()
    }

    fun getById(id: Long): LiveData<Note> {
        Timber.d("getById: $id")
        return holder.store
            ?.findByKey(Note::class, id)
            ?.let { MutableLiveData(it) }
            ?: MutableLiveData()
    }

    companion object {
        const val QUERY_LIMIT = 200

        // For Singleton instantiation
        @Volatile
        private var instance: NoteRepository? = null

        fun getInstance(application: EisbaerApplication) =
            instance ?: synchronized(this) {
                instance ?: NoteRepository(application.database.entityStoreHolder).also {
                    instance = it
                }
            }
    }
}