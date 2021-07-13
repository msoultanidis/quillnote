package org.qosp.notes.ui.sync

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.qosp.notes.R
import org.qosp.notes.databinding.FragmentSyncSettingsBinding
import org.qosp.notes.preferences.*
import org.qosp.notes.ui.common.BaseFragment
import org.qosp.notes.ui.settings.SettingsViewModel
import org.qosp.notes.ui.settings.showPreferenceDialog
import org.qosp.notes.ui.sync.nextcloud.NextcloudAccountDialog
import org.qosp.notes.ui.sync.nextcloud.NextcloudServerDialog
import org.qosp.notes.ui.utils.collect
import org.qosp.notes.ui.utils.liftAppBarOnScroll
import org.qosp.notes.ui.utils.viewBinding

@AndroidEntryPoint
class SyncSettingsFragment : BaseFragment(R.layout.fragment_sync_settings) {
    private val binding by viewBinding(FragmentSyncSettingsBinding::bind)
    private val model: SettingsViewModel by activityViewModels()

    override val hasMenu = false
    override val toolbar: Toolbar
        get() = binding.layoutAppBar.toolbar
    override val toolbarTitle: String
        get() = getString(R.string.preferences_header_syncing)

    private var appPreferences = AppPreferences()

    private var nextcloudUrl = ""

    private val directoryLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        val path = it?.toString() ?: return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        model.putEncryptedStrings(
            PreferenceRepository.LOCAL_SYNC_DIRECTORY_PATH to path,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scrollView.liftAppBarOnScroll(
            binding.layoutAppBar.appBar,
            requireContext().resources.getDimension(R.dimen.app_bar_elevation)
        )

        binding.settingLocalSyncFolder.setOnClickListener {
            directoryLauncher.launch(null)
        }

        setProviderSettingsVisibility(appPreferences.cloudService)

        setupPreferenceObservers()
        setupSyncServiceListener()
        setupSyncModeListener()
        setupBackgroundSyncListener()
        setupNewNotesSyncableListener()

        setupNextcloudServerListener()
        setupNextcloudAccountListener()
        setupClearNextcloudCredentialsListener()
    }

    private fun setupPreferenceObservers() {
        model.appPreferences.collect(viewLifecycleOwner) {
            appPreferences = it

            with(appPreferences) {
                binding.settingSyncProvider.subText = getString(cloudService.nameResource)
                setProviderSettingsVisibility(cloudService)
                binding.settingSyncMode.subText = getString(syncMode.nameResource)
                binding.settingBackgroundSync.subText = getString(backgroundSync.nameResource)
                binding.settingNotesSyncableByDefault.subText = getString(newNotesSyncable.nameResource)
            }
        }

        // ENCRYPTED
        model.getEncryptedString(PreferenceRepository.NEXTCLOUD_INSTANCE_URL).collect(viewLifecycleOwner) {
            nextcloudUrl = it
            binding.settingNextcloudServer.subText = nextcloudUrl.ifEmpty { getString(R.string.preferences_nextcloud_set_server_url) }
        }

        model.loggedInUsername.collect(viewLifecycleOwner) {
            binding.settingNextcloudAccount.subText = if (it != null) {
                getString(R.string.indicator_nextcloud_currently_logged_in_as, it)
            } else {
                getString(R.string.preferences_nextcloud_set_your_credentials)
            }
        }
    }

    private fun setupNextcloudServerListener() = binding.settingNextcloudServer.setOnClickListener {
        NextcloudServerDialog.build(nextcloudUrl).show(childFragmentManager, null)
    }

    private fun setupNextcloudAccountListener() = binding.settingNextcloudAccount.setOnClickListener {
        NextcloudAccountDialog().show(childFragmentManager, null)
    }

    private fun setupSyncServiceListener() = binding.settingSyncProvider.setOnClickListener {
        showPreferenceDialog(R.string.preferences_cloud_service, appPreferences.cloudService) { selected ->
            model.setPreference(selected)
        }
    }

    private fun setupSyncModeListener() = binding.settingSyncMode.setOnClickListener {
        showPreferenceDialog(R.string.preferences_sync_when_on, appPreferences.syncMode) { selected ->
            model.setPreference(selected)
        }
    }

    private fun setupBackgroundSyncListener() = binding.settingBackgroundSync.setOnClickListener {
        showPreferenceDialog(R.string.preferences_background_sync, appPreferences.backgroundSync) { selected ->
            model.setPreference(selected)
        }
    }

    private fun setupNewNotesSyncableListener() = binding.settingNotesSyncableByDefault.setOnClickListener {
        showPreferenceDialog(R.string.preferences_new_notes_synchronizable, appPreferences.newNotesSyncable) { selected ->
            model.setPreference(selected)
        }
    }

    private fun setupClearNextcloudCredentialsListener() = binding.settingNextcloudClearCredentials.setOnClickListener {
        model.clearNextcloudCredentials()
    }
    private fun setProviderSettingsVisibility(currentProvider: CloudService) {
        binding.layoutNextcloudSettings.isVisible = currentProvider == CloudService.NEXTCLOUD
        binding.layoutGenericSettings.isVisible = currentProvider != CloudService.DISABLED
    }
}
