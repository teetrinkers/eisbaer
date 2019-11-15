package de.theess.eisbaer

import android.app.Application
import de.theess.eisbaer.data.Database
import greenberg.moviedbshell.logging.DebuggingTree
import greenberg.moviedbshell.logging.NoLogTree
import timber.log.Timber

class EisbaerApplication : Application() {

    lateinit var database : Database

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebuggingTree())
        } else {
            Timber.plant(NoLogTree())
        }

        database = Database(this)
    }

    companion object {
        const val PREF_DATABASE_URI: String = "database_uri"
        const val PREF_DATABASE_FILE_SIZE: String = "database_file_size"
    }
}