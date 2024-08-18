package org.qosp.notes.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.preferences.ThemeMode
import javax.inject.Inject

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        runBlocking {
            val (colorScheme, themeMode, darkThemeModeStyle) = withContext(Dispatchers.IO) {
                preferenceRepository
                    .getAll()
                    .map {
                        Triple(
                            it.colorScheme.styleResource,
                            it.themeMode.mode,
                            it.darkThemeMode.styleResource
                        )
                    }
                    .first()
            }

            theme.applyStyle(colorScheme, true)

            if (themeMode != AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.setDefaultNightMode(themeMode)
            }

            val isAutoDark =
                themeMode == ThemeMode.SYSTEM.mode && (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            // Check which theme should be used (light, dark, black) and set navbar color accordingly
            // if dark
            if (themeMode == ThemeMode.DARK.mode || isAutoDark) {
                // if black
                darkThemeModeStyle?.let {
                    theme.applyStyle(darkThemeModeStyle, true)
                }
            }

        }
    }
}
