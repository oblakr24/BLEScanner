package com.rokoblak.blescanner.service

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.rokoblak.blescanner.service.PersistedStorage.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface PersistedStorage {
    fun prefsFlow(): Flow<Prefs>

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun clear()

    @kotlinx.serialization.Serializable
    data class Prefs(
        val darkMode: Boolean? // null -> no user selection, i.e. follow the system
    )
}

/**
 * Datastore-backed persisted data i.e. settings.
 * Just one boolean for now, but could be easily extended.
 */
@Singleton
class AppStorage @Inject constructor(
    @ApplicationContext appContext: Context,
    private val json: Json
) : PersistedStorage {

    private val store = PreferenceDataStoreFactory.create(
        produceFile = {
            appContext.preferencesDataStoreFile("prefs_store")
        }
    )

    override fun prefsFlow(): Flow<Prefs> {
        return kotlinx.coroutines.flow.flow {
            ensurePopulated()
            emitAll(flow().map {
                json.decodeFromString(Prefs.serializer(), it)
            })
        }.distinctUntilChanged()
    }

    override suspend fun updateDarkMode(enabled: Boolean) {
        update {
            copy(darkMode = enabled)
        }
    }

    override suspend fun clear() {
        store.edit {
            it.clear()
        }
    }

    private suspend fun ensurePopulated() {
        if (!isKeyStored()) {
            defaultSettings.store()
        }
    }

    private suspend fun Prefs.store() {
        store.edit {
            it[KEY_SETTINGS] = json.encodeToString(Prefs.serializer(), this)
        }
    }

    private suspend fun update(block: suspend Prefs.() -> Prefs) {
        ensurePopulated()
        val encoded = retrieve() ?: return
        val current = json.decodeFromString(Prefs.serializer(), encoded)
        val new = block(current)
        new.store()
    }

    private suspend fun isKeyStored() = store.data.firstOrNull()?.contains(KEY_SETTINGS) ?: false

    private suspend fun retrieve(): String? {
        return store.data.firstOrNull()?.get(KEY_SETTINGS)
    }

    private fun flow() = store.data.mapNotNull { it[KEY_SETTINGS] }

    companion object {
        private val KEY_SETTINGS = stringPreferencesKey("settings")

        val defaultSettings = Prefs(
            darkMode = null,
        )
    }
}
