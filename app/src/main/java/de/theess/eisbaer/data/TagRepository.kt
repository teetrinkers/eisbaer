package de.theess.eisbaer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.theess.eisbaer.EisbaerApplication
import io.requery.kotlin.asc
import timber.log.Timber

class TagRepository private constructor(private val database: Database) {

    fun getAll(): LiveData<List<Tag>> {
        Timber.d("getAll")
        return database.store
            ?.run {
                select(Tag::class)
                    .orderBy(Tag::title.asc()).limit(QUERY_LIMIT)
            }
            ?.let { MutableLiveData(it.get().toList()) }
            ?: MutableLiveData()
    }

    companion object {
        const val QUERY_LIMIT = 200

        // For Singleton instantiation
        @Volatile
        private var instance: TagRepository? = null

        fun getInstance(application: EisbaerApplication) =
            instance ?: synchronized(this) {
                instance ?: TagRepository(application.database).also {
                    instance = it
                }
            }
    }
}