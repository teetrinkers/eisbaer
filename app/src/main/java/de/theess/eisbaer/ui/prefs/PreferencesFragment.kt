package de.theess.eisbaer.ui.prefs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.theess.eisbaer.EisbaerApplication
import de.theess.eisbaer.R
import de.theess.eisbaer.prefs.stringLiveData
import timber.log.Timber

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Database button
        val button: Preference = findPreference(getString(R.string.pref_database_button))!!

        // Database button: summary
        PreferenceManager.getDefaultSharedPreferences(activity)
            .stringLiveData(EisbaerApplication.PREF_DATABASE_URI).observe(this, Observer { value ->
                if (value != null) {
                    button.summary = value
                }
            })

        // database button: click listener for the
        button.setOnPreferenceClickListener {
            openFileChooser()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == DATABASE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                setDatabaseUri(uri)
            }
        }
    }

    fun openFileChooser() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            addCategory(Intent.CATEGORY_OPENABLE)

            type = "*/*"

            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivityForResult(intent, DATABASE_REQUEST_CODE)
    }

    private fun setDatabaseUri(uri: Uri) {
        Timber.d("Uri: $uri")

        activity?.contentResolver?.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        activity?.contentResolver?.openInputStream(uri)?.use {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            sharedPreferences.edit {
                putString(EisbaerApplication.PREF_DATABASE_URI, uri.toString())
            }
        }
    }

    companion object {
        private const val DATABASE_REQUEST_CODE: Int = 1
    }
}
