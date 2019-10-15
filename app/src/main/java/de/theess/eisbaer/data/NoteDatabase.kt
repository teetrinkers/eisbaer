package de.theess.eisbaer.data

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import androidx.preference.PreferenceManager
import de.theess.eisbaer.BuildConfig
import de.theess.eisbaer.EisbaerApplication
import io.requery.android.sqlite.DatabaseSource
import io.requery.sql.KotlinEntityDataStore
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Manages the requery data store.
 */
abstract class NoteDatabase {

    /**
     * Open the database once to initialize the android_metadata table.
     * https://github.com/requery/requery/issues/856
     */
    internal class DbInitializer(context: Context, dbName: String) :
        SQLiteOpenHelper(context, dbName, null, 1) {
        override fun onCreate(db: SQLiteDatabase?) {}
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

        fun initDatabase() {
            readableDatabase
            close()
        }
    }

    companion object {
        private const val DATABASE_NAME = "note_db"

        @Volatile
        private var instance: KotlinEntityDataStore<Any>? = null

        fun getDataStore(context: Context): KotlinEntityDataStore<Any> {
            return instance ?: synchronized(this) {

                try {
                    copyDatabase(context)
                    DbInitializer(context, DATABASE_NAME).initDatabase()
                } catch (e: FileNotFoundException) {
                    Timber.w(e)
                }

                val source = DatabaseSource(context, Models.DEFAULT, DATABASE_NAME, 1)
                if (BuildConfig.DEBUG) {
                    source.setLoggingEnabled(true)
                }
                val store = KotlinEntityDataStore<Any>(source.configuration)

                instance = store
                return store
            }
        }

        /**
         * Copies the database selected in the preferences to the internal database.
         */
        private fun copyDatabase(context: Context) {
            Timber.d("copyDatabase")

            // Internal database file.
            val dbPath = context.getDatabasePath(DATABASE_NAME)

            // Make sure we have a path to the file.
            dbPath?.parentFile?.mkdirs()

            // Copy data.
            FileOutputStream(dbPath).use {
                getExternalDatabaseInputStream(context).copyTo(it)
            }

            Timber.d("copyDatabase done")
        }

        /**
         * Opens an InputStream to the SQLite database selected in the preferences.
         */
        private fun getExternalDatabaseInputStream(context: Context): InputStream {
            val uriString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(EisbaerApplication.PREF_DATABASE_URI, null)
                ?: throw FileNotFoundException("no database selected")
            val uri = Uri.parse(uriString)
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            return context.contentResolver.openInputStream(uri)!!
        }

    }
}