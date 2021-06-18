package org.qosp.notes.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.qosp.notes.R
import org.qosp.notes.databinding.FragmentSettingsBinding
import org.qosp.notes.preferences.*
import org.qosp.notes.ui.MainActivity
import org.qosp.notes.ui.common.BaseFragment
import org.qosp.notes.ui.utils.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class SettingsFragment : BaseFragment(resId = R.layout.fragment_settings) {
    private val binding by viewBinding(FragmentSettingsBinding::bind)
    private val model: SettingsViewModel by viewModels()

    private var colorScheme = ColorScheme.default()
    private var themeMode = ThemeMode.default()
    private var layoutMode = LayoutMode.default()
    private var sortMethod = SortMethod.default()
    private var backupStrategy = BackupStrategy.default()
    private var openMediaIn = OpenMediaIn.default()
    private var noteDeletionTime = NoteDeletionTime.default()
    private var dateFormat = DateFormat.default()
    private var timeFormat = TimeFormat.default()
    private var cloudService = CloudService.default()

    override val hasMenu = false
    override val toolbar: Toolbar
        get() = binding.layoutAppBar.toolbar
    override val toolbarTitle: String
        get() = getString(R.string.nav_settings)

    private val loadBackupLauncher = registerForActivityResult(RestoreNotesContract) { uri ->
        if (uri == null) return@registerForActivityResult
        (activity as MainActivity).restoreNotes(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPreferenceObservers()
        setupColorSchemeListener()
        setupThemeModeListener()
        setupLayoutModeListener()
        setupSortMethodListener()
        setupOpenMediaInListener()
        setupNoteDeletionTimeListener()
        setupBackupStrategyListener()
        setupDateFormatListener()
        setupTimeFormatListener()
        setupSyncSettingsListener()

        binding.scrollView.liftAppBarOnScroll(
            binding.layoutAppBar.appBar,
            requireContext().resources.getDimension(R.dimen.app_bar_elevation)
        )

        binding.settingRestoreNotes.setOnClickListener { loadBackupLauncher.launch() }

        binding.settingBackupNotes.setOnClickListener {
            activityModel.notesToBackup = null
            exportNotesLauncher.launch()
        }
    }

    private fun setupPreferenceObservers() {
        model.cloudService.collect(viewLifecycleOwner) {
            cloudService = it
            binding.settingGoToSyncSettings.subText = when (cloudService) {
                CloudService.DISABLED -> getString(R.string.preferences_currently_not_syncing)
                else -> getString(R.string.preferences_currently_syncing_with, getString(cloudService.nameResource))
            }
        }

        model.colorScheme.collect(viewLifecycleOwner) {
            colorScheme = it
            binding.settingColorScheme.subText = getString(colorScheme.nameResource)
        }

        model.themeMode.collect(viewLifecycleOwner) {
            themeMode = it
            binding.settingThemeMode.subText = getString(themeMode.nameResource)
        }

        model.layoutMode.collect(viewLifecycleOwner) {
            layoutMode = it
            binding.settingLayoutMode.subText = getString(layoutMode.nameResource)
            binding.settingLayoutMode.setIcon(
                when (layoutMode) {
                    LayoutMode.GRID -> R.drawable.ic_grid
                    LayoutMode.LIST -> R.drawable.ic_list
                }
            )
        }

        model.sortMethod.collect(viewLifecycleOwner) {
            sortMethod = it
            binding.settingSortMethod.subText = getString(sortMethod.nameResource)
        }

        model.backupStrategy.collect(viewLifecycleOwner) {
            backupStrategy = it
            binding.settingBackupStrategy.subText = getString(backupStrategy.nameResource)
        }

        model.openMediaIn.collect(viewLifecycleOwner) {
            openMediaIn = it
            binding.settingOpenMedia.subText = getString(openMediaIn.nameResource)
        }

        model.noteDeletionTime.collect(viewLifecycleOwner) {
            noteDeletionTime = it
            binding.settingNoteDeletion.subText = getString(noteDeletionTime.nameResource)
        }

        model.dateFormat.collect(viewLifecycleOwner) {
            dateFormat = it
            with(DateTimeFormatter.ofPattern(getString(dateFormat.patternResource))) {
                binding.settingDateFormat.subText = format(LocalDate.now())
            }
        }

        model.timeFormat.collect(viewLifecycleOwner) {
            timeFormat = it
            with(DateTimeFormatter.ofPattern(getString(timeFormat.patternResource))) {
                binding.settingTimeFormat.subText = format(LocalTime.now())
            }
        }
    }

    private fun setupSyncSettingsListener() = binding.settingGoToSyncSettings.setOnClickListener {
        findNavController().navigateSafely(SettingsFragmentDirections.actionMainSettingsToSync())
    }

    private fun setupColorSchemeListener() = binding.settingColorScheme.setOnClickListener {
        showPreferenceDialog(R.string.preferences_color_scheme, colorScheme) { which ->
            lifecycleScope.launch {
                model.setPreferenceSuspending(ColorScheme.values()[which])
                activity?.recreate()
            }
        }
    }

    private fun setupThemeModeListener() = binding.settingThemeMode.setOnClickListener {
        showPreferenceDialog(R.string.preferences_theme_mode, themeMode) { which ->
            lifecycleScope.launch {
                model.setPreference(ThemeMode.values()[which])
                val mode = when (ThemeMode.values()[which]) {
                    ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                if (mode != AppCompatDelegate.getDefaultNightMode()) AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }

    private fun setupLayoutModeListener() = binding.settingLayoutMode.setOnClickListener {
        showPreferenceDialog(R.string.preferences_layout_mode, layoutMode) { which ->
            model.setPreference(LayoutMode.values()[which])
        }
    }

    private fun setupSortMethodListener() = binding.settingSortMethod.setOnClickListener {
        showPreferenceDialog(R.string.preferences_sort_method, sortMethod) { which ->
            model.setPreference(SortMethod.values()[which])
        }
    }

    private fun setupBackupStrategyListener() = binding.settingBackupStrategy.setOnClickListener {
        showPreferenceDialog(R.string.preferences_backup_strategy, backupStrategy) { which ->
            model.setPreference(BackupStrategy.values()[which])
        }
    }

    private fun setupOpenMediaInListener() = binding.settingOpenMedia.setOnClickListener {
        showPreferenceDialog(R.string.preferences_open_media_in, openMediaIn) { which ->
            model.setPreference(OpenMediaIn.values()[which])
        }
    }

    private fun setupNoteDeletionTimeListener() = binding.settingNoteDeletion.setOnClickListener {
        showPreferenceDialog(R.string.preferences_note_deletion_time, noteDeletionTime) { which ->
            model.setPreference(NoteDeletionTime.values()[which])
        }
    }

    private fun setupTimeFormatListener() = binding.settingTimeFormat.setOnClickListener {
        val localTime = LocalTime.now()
        val items = TimeFormat.values()
            .map {
                DateTimeFormatter.ofPattern(getString(it.patternResource)).format(localTime)
            }
            .toTypedArray()

        showPreferenceDialog(R.string.preferences_time_format, timeFormat, items = items) { which ->
            model.setPreference(TimeFormat.values()[which])
        }
    }

    private fun setupDateFormatListener() = binding.settingDateFormat.setOnClickListener {
        val localDate = LocalDate.now()
        val items = DateFormat.values()
            .map {
                DateTimeFormatter.ofPattern(getString(it.patternResource)).format(localDate)
            }
            .toTypedArray()

        showPreferenceDialog(R.string.preferences_date_format, dateFormat, items = items) { which ->
            model.setPreference(DateFormat.values()[which])
        }
    }
}
