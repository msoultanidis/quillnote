package org.qosp.notes.ui.settings

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.qosp.notes.R
import org.qosp.notes.preferences.Preference

inline fun <reified T> Fragment.showPreferenceDialog(
    titleRes: Int,
    selected: T,
    dismiss: Boolean = true,
    items: Array<String>? = null,
    crossinline onClick: (Int) -> Unit,
) where T : Enum<T>, T : Preference<T> {
    val enumValues = enumValues<T>()
    val selectedIndex = enumValues.indexOf(selected)
    val items = items ?: enumValues
        .map { requireContext().getString(it.nameResource) }
        .toTypedArray()

    MaterialAlertDialogBuilder(requireContext())
        .setTitle(requireContext().getString(titleRes))
        .setSingleChoiceItems(items, selectedIndex) { dialogInterface, which ->
            if (dismiss) dialogInterface.dismiss()
            onClick(which)
        }
        .setPositiveButton(getString(R.string.action_done)) { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        .show()
}
