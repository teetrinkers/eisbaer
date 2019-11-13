package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.theess.eisbaer.EisbaerApplication
import io.requery.kotlin.desc
import io.requery.kotlin.eq
import io.requery.kotlin.like
import timber.log.Timber
import java.util.*

typealias NoteListProviderFunction = () -> List<Note>?

class NoteRepository private constructor(private val database: Database) : DatabaseListener {

    /**
     * Map of LiveData to value function. When the database changes, the livedatas will be updated
     * using the value function.
     */
    private val liveDataMap: MutableMap<MutableLiveData<List<Note>>, NoteListProviderFunction> =
        WeakHashMap<MutableLiveData<List<Note>>, NoteListProviderFunction>()

    init {
        database.addListener(this)
    }

    /**
     * Stores the function and a new LiveData in the liveDataMap, and updates the LiveData using
     * the function result.
     */
    private fun wrap(valueProvider: NoteListProviderFunction): LiveData<List<Note>> {
        val liveData = MutableLiveData<List<Note>>()
        liveDataMap[liveData] = valueProvider

        valueProvider()?.let {
            liveData.postValue(it)
        }

        return liveData
    }

    override fun update() {
        Timber.d("Updating %d listeners.", liveDataMap.size)
        // Update each LiveData using the provider function.
        liveDataMap.forEach {
            Timber.d("Updating note listener. Observers: %b", it.key.hasObservers())
            it.key.postValue(it.value())
        }
    }

    fun getAll(): LiveData<List<Note>> {
        return wrap {
            database.store?.run {
                select(Note::class)
                    .where(Note::trashed.eq(0))
                    .orderBy(Note::modificationDateRaw.desc()).limit(QUERY_LIMIT)
            }?.get()?.toList()
        }
    }

    fun query(query: String): LiveData<List<Note>> {
        Timber.d("query: $query")
        return wrap {
            database.store
                ?.run {
                    select(Note::class)
                        .where(Note::content.like("%$query%")).and(Note::trashed.eq(0))
                        .orderBy(Note::modificationDateRaw.desc()).limit(QUERY_LIMIT)
                }
                ?.get()?.toList()
                .also { Timber.d("query result count: ${it?.size}") }
        }
    }

    fun getById(id: Long): LiveData<Note> {
        Timber.d("getById: $id")
        return database.store
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
                instance ?: NoteRepository(application.database).also {
                    instance = it
                }
            }
    }

}