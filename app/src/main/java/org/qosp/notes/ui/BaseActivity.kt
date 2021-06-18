package org.qosp.notes.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.qosp.notes.preferences.ColorScheme
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.preferences.ThemeMode
import org.qosp.notes.preferences.get
import javax.inject.Inject

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            val (colorScheme, themeMode) = withContext(Dispatchers.IO) {
                val colorScheme = preferenceRepository.get<ColorScheme>().first().styleResource
                val themeMode = preferenceRepository.get<ThemeMode>().first().mode
                colorScheme to themeMode
            }

            theme.applyStyle(colorScheme, true)
            if (themeMode != AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.setDefaultNightMode(themeMode)
            }
        }
    }
}
