@file:Suppress("unused")

package de.theess.eisbaer.prefs

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

/**
 * Use SharedPreferences as a LiveData.
 *
 * Based on https://gist.github.com/rharter/1df1cd72ce4e9d1801bd2d49f2a96810.
 */
abstract class SharedPreferenceLiveData<T>(
    val sharedPrefs: SharedPreferences,
    private val key: String,
    private val defValue: T?
) : LiveData<T>() {

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == this.key) {
                value = getValueFromPreferences(key, defValue)
            }
        }

    abstract fun getValueFromPreferences(key: String, defValue: T?): T?

    override fun onActive() {
        super.onActive()
        value = getValueFromPreferences(key, defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}

class SharedPreferenceStringLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    defValue: String?
) :
    SharedPreferenceLiveData<String>(sharedPrefs, key, defValue) {
    override fun getValueFromPreferences(key: String, defValue: String?): String? =
        sharedPrefs.getString(key, defValue)
}

fun SharedPreferences.stringLiveData(
    key: String
): SharedPreferenceLiveData<String> {
    return SharedPreferenceStringLiveData(this, key, null)
}