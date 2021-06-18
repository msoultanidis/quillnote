package org.qosp.notes.ui.sync

import android.os.Bundle
import android.view.View
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

    private var syncService = CloudService.default()
    private var syncMode = SyncMode.default()
    private var backgroundSync = BackgroundSync.default()
    private var newNotesSyncable = NewNotesSyncable.default()

    private var nextcloudUrl = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scrollView.liftAppBarOnScroll(
            binding.layoutAppBar.appBar,
            requireContext().resources.getDimension(R.dimen.app_bar_elevation)
        )

        setProviderSettingsVisibility(syncService)

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
        model.cloudService.collect(viewLifecycleOwner) {
            syncService = it
            binding.settingSyncProvider.subText = getString(syncService.nameResource)
            setProviderSettingsVisibility(syncService)
        }

        model.syncMode.collect(viewLifecycleOwner) {
            syncMode = it
            binding.settingSyncMode.subText = getString(syncMode.nameResource)
        }

        model.backgroundSync.collect(viewLifecycleOwner) {
            backgroundSync = it
            binding.settingBackgroundSync.subText = getString(backgroundSync.nameResource)
        }

        model.newNotesSyncable.collect(viewLifecycleOwner) {
            newNotesSyncable = it
            binding.settingNotesSyncableByDefault.subText = getString(newNotesSyncable.nameResource)
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
        showPreferenceDialog(R.string.preferences_cloud_service, syncService) { which ->
            model.setPreference(CloudService.values()[which])
        }
    }

    private fun setupSyncModeListener() = binding.settingSyncMode.setOnClickListener {
        showPreferenceDialog(R.string.preferences_sync_when_on, syncMode) { which ->
            model.setPreference(SyncMode.values()[which])
        }
    }

    private fun setupBackgroundSyncListener() = binding.settingBackgroundSync.setOnClickListener {
        showPreferenceDialog(R.string.preferences_background_sync, backgroundSync) { which ->
            model.setPreference(BackgroundSync.values()[which])
        }
    }

    private fun setupNewNotesSyncableListener() = binding.settingNotesSyncableByDefault.setOnClickListener {
        showPreferenceDialog(R.string.preferences_new_notes_synchronizable, newNotesSyncable) { which ->
            model.setPreference(NewNotesSyncable.values()[which])
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
