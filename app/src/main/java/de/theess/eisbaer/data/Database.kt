package de.theess.eisbaer.data

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import de.theess.eisbaer.BuildConfig
import de.theess.eisbaer.EisbaerApplication
import de.theess.eisbaer.Models
import io.requery.android.sqlite.DatabaseSource
import io.requery.sql.KotlinEntityDataStore
import timber.log.Timber
import java.io.FileOutputStream
import kotlin.concurrent.fixedRateTimer

/**
 * Manages the requery data store.
 */
class Database(private val context: Context) {

    companion object {
        /**
         * Name of the internal database.
         */
        private const val DATABASE_NAME = "note_db"
        /**
         * Interval in milliseconds to check for updates to the external database file.
         */
        private const val CHECK_INTERVAL: Long = 10 * 1000
    }

    private var _status: MutableLiveData<String> = MutableLiveData("")
    val status : LiveData<String>
        get() = _status

    /**
     * The requery entity store. This is null if the database is closed.
     */
    var store: KotlinEntityDataStore<Any>? = null

    /**
     * The requery database wrapper. This is null if the database is closed.
     */
    private var databaseSource: DatabaseSource? = null

    /**
     * Listeners interested in database changes.
     */
    private val listeners: MutableList<DatabaseListener> = mutableListOf()

    /**
     * Size of the external database. This value is persisted to the preferences.
     */
    private var externalDatabaseHash: String = ""
        set(value) {
            field = value
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putString(EisbaerApplication.PREF_DATABASE_HASH, value)
            }
        }

    /**
     * Reopens the database if the database uri pref changes.
     */
    private val preferenceListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == EisbaerApplication.PREF_DATABASE_URI) synchronized(this) {
                Timber.d("database uri pref changed")

                // Clear the saved hash.
                externalDatabaseHash = ""

                // Update the database in a background thread.
                checkForUpdates()
            }
        }

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Listen for pref changes.
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)

        externalDatabaseHash = preferences.getString(EisbaerApplication.PREF_DATABASE_HASH, "")!!

        openDatabase()

        // Update the database in a background timer.
        fixedRateTimer("databaseUpdates", true, 2 * 1000, CHECK_INTERVAL) {
            checkForUpdates()
        }
    }

    fun addListener(listener: DatabaseListener) {
        listeners.add(listener)
    }

    /**
     * Tell the content provider to refresh (e.g. download) the database file. Works only on Android 8+.
     */
    fun refresh() {
        val externalDbFile = getExternalDatabaseFile(context)
        if (externalDbFile == null || !externalDbFile.checkUriGrant()) {
            Timber.i("No database file uri.")
            return
        }

        Timber.d("Refresh started.")
        externalDbFile.refresh()
        Timber.d("Refresh done.")

        checkForUpdates()
    }

    /**
     * Copies the external database file to the internal database, if the external file is changed
     * compared to the saved hash.
     */
    @Synchronized
    private fun checkForUpdates() {
        Timber.d("Checking for updates.")
        _status.postValue("")

        // Only check for updates while the app is in the foreground.
        val lifecycleState = ProcessLifecycleOwner.get().lifecycle.currentState
        if (!lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
            Timber.d("Lifecycle state is not at least RESUMED. State = $lifecycleState.")
            return
        }

        val externalDbFile = getExternalDatabaseFile(context)
        if (externalDbFile == null || !externalDbFile.checkUriGrant()) {
            Timber.i("No database file uri.")
            return
        }

        Timber.d("Database file: $externalDbFile")

        val internalDatabasePath = context.getDatabasePath(DATABASE_NAME)

        Timber.d("Internal db exists: ${internalDatabasePath.exists()}")
        Timber.d("Hash of external db: ${externalDbFile.hash()}")
        Timber.d("Saved hash: $externalDatabaseHash")

        // Check for changes to the external db file.
        if (internalDatabasePath.exists()
            && externalDatabaseHash.isNotEmpty() && externalDbFile.hash() == externalDatabaseHash
        ) {
            Timber.d("Database has not changed.")
            return
        }

        Timber.i("Database changed.")

        close()
        copyDatabase(externalDbFile)
        openDatabase()

        // Save hash of the external db.
        externalDatabaseHash = externalDbFile.hash()

        _status.postValue("Database updated")
    }

    /**
     * Opens the internal database.
     */
    @Synchronized
    private fun openDatabase() {
        val internalDatabasePath = context.getDatabasePath(DATABASE_NAME)
        if (!internalDatabasePath.exists()) {
            Timber.i("Internal db file $internalDatabasePath does not exist.")
            return
        }

        val newSource = DatabaseSource(context, Models.DEFAULT, DATABASE_NAME, 1)
        if (BuildConfig.DEBUG) {
            newSource.setLoggingEnabled(true)
        }
        databaseSource = newSource
        store = KotlinEntityDataStore(newSource.configuration)

        // Call listeners.
        listeners.forEach { it.update() }
    }

    /**
     * Closes the database.
     */
    private fun close() {
        if (store != null) {
            store?.close()
            store = null
        }
        if (databaseSource != null) {
            databaseSource?.close()
            databaseSource = null
        }
    }

    /**
     * Copies the database selected in the preferences to the internal database.
     */
    private fun copyDatabase(externalDbFile: StorageFile) {
        Timber.d("copy database from $externalDbFile")

        // Internal database file.
        val dbPath = context.getDatabasePath(DATABASE_NAME)

        // Make sure we have a path to the file.
        dbPath?.parentFile?.mkdirs()

        // Copy data.
        externalDbFile.openInputStream().use { inputStream ->
            if (inputStream == null) {
                Timber.d("Cannot open input stream.")
                return
            }

            // Delete the old internal database, if it exists.
            dbPath.exists() && dbPath.delete()

            FileOutputStream(dbPath).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // Make the database usable by requery.
        initDatabase()

        Timber.d("copyDatabase done")
    }

    /**
     * Opens the database once to initialize the android_metadata table.
     * https://github.com/requery/requery/issues/856
     */
    private fun initDatabase() {
        val helper = object : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
            override fun onCreate(db: SQLiteDatabase?) {}
            override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

            fun initDatabase() {
                readableDatabase
                close()
            }
        }
        helper.initDatabase()
    }

    /**
     * Gets the URI to the external database file from the preferences.
     */
    private fun getExternalDatabaseFile(context: Context): StorageFile? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(EisbaerApplication.PREF_DATABASE_URI, null)
            ?.let { Uri.parse(it) }
            ?.let { StorageFile(it, context.contentResolver) }
    }
}