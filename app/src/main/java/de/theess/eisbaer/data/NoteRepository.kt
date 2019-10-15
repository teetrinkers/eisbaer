package de.theess.eisbaer.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.requery.kotlin.*
import io.requery.query.Result
import io.requery.sql.KotlinEntityDataStore
import timber.log.Timber

class NoteRepository private constructor(private val store: KotlinEntityDataStore<Any>) {

    fun getAll(): LiveData<List<Note>> {
        Timber.d("getAll")
        val result: Offset<Result<Note>> =
            store select (Note::class) orderBy (Note::modificationDateRaw.desc()) limit QUERY_LIMIT
        return MutableLiveData(result.get().toList())
    }

    fun query(query: String): LiveData<List<Note>> {
        Timber.d("query: $query")
        val result =
            store select (Note::class) where (Note::content.like("%$query%")) orderBy (Note::modificationDateRaw.desc()) limit QUERY_LIMIT
        val resultList = result.get().toList()
        Timber.d("query result count: ${resultList.size}")
        return MutableLiveData(resultList)
    }

    fun getById(id: Long): LiveData<Note> {
        Timber.d("getById: $id")
        return store.findByKey(Note::class, id)?.let { it -> MutableLiveData(it) }
            ?: MutableLiveData()
    }

    companion object {
        const val QUERY_LIMIT = 100

        // For Singleton instantiation
        @Volatile
        private var instance: NoteRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: NoteRepository(NoteDatabase.getDataStore(context)).also {
                    instance = it
                }
            }
    }
}