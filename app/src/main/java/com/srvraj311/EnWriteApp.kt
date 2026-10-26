package com.srvraj311

import android.app.Application
import com.srvraj311.data.AuthRepository
import com.srvraj311.data.NotesRepository
import com.srvraj311.data.SettingsStore

/** Holds the app-wide singletons (no DI framework needed for an app this size). */
class EnWriteApp : Application() {
    lateinit var settings: SettingsStore
        private set
    lateinit var auth: AuthRepository
        private set
    lateinit var notes: NotesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settings = SettingsStore(this)
        auth = AuthRepository(this)
        notes = NotesRepository(this, auth)
    }
}
