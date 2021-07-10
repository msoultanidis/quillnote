package org.qosp.notes.preferences

import androidx.appcompat.app.AppCompatDelegate
import me.msoul.datastore.EnumPreference
import me.msoul.datastore.key
import org.qosp.notes.R
import java.util.concurrent.TimeUnit

enum class LayoutMode(override val nameResource: Int) : HasNameResource, EnumPreference by key("layout_mode") {
    GRID(R.string.preferences_layout_mode_grid) { override val isDefault = true },
    LIST(R.string.preferences_layout_mode_list),
}

enum class ThemeMode(override val nameResource: Int, val mode: Int) : HasNameResource, EnumPreference by key("theme_mode") {
    SYSTEM(R.string.preferences_theme_mode_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) { override val isDefault = true },
    DARK(R.string.preferences_theme_mode_dark, AppCompatDelegate.MODE_NIGHT_YES),
    LIGHT(R.string.preferences_theme_mode_light, AppCompatDelegate.MODE_NIGHT_NO),
}

enum class DarkThemeMode(override val nameResource: Int, val styleResource: Int?) : HasNameResource, EnumPreference by key("dark_theme_mode") {
    STANDARD(R.string.preferences_theme_dark_mode_standard, null) { override val isDefault = true },
    BLACK(R.string.preferences_theme_dark_mode_black, R.style.DarkBlack),
}

enum class ColorScheme(
    override val nameResource: Int,
    val styleResource: Int,
) : HasNameResource, EnumPreference by key("color_scheme") {
    BLUE(R.string.preferences_color_scheme_blue, R.style.Blue) { override val isDefault = true },
    GREEN(R.string.preferences_color_scheme_green, R.style.Green),
    PINK(R.string.preferences_color_scheme_pink, R.style.Pink),
    YELLOW(R.string.preferences_color_scheme_orange, R.style.Orange),
    RED(R.string.preferences_color_scheme_purple, R.style.Purple),
}

enum class SortMethod(override val nameResource: Int) : HasNameResource, EnumPreference by key("sort_method") {
    TITLE_ASC(R.string.preferences_sort_method_title_asc),
    TITLE_DESC(R.string.preferences_sort_method_title_desc),
    CREATION_ASC(R.string.preferences_sort_method_created_asc),
    CREATION_DESC(R.string.preferences_sort_method_created_desc),
    MODIFIED_ASC(R.string.preferences_sort_method_modified_asc),
    MODIFIED_DESC(R.string.preferences_sort_method_modified_desc) { override val isDefault = true },
}

enum class BackupStrategy(override val nameResource: Int) : HasNameResource, EnumPreference by key("backup_strategy") {
    INCLUDE_FILES(R.string.preferences_backup_strategy_include_files) { override val isDefault = true },
    KEEP_INFO(R.string.preferences_backup_strategy_keep_info),
    KEEP_NOTHING(R.string.preferences_backup_strategy_keep_nothing),
}

enum class NoteDeletionTime(
    override val nameResource: Int,
    val interval: Long,
) : HasNameResource, EnumPreference by key("note_deletion_time") {
    WEEK(R.string.preferences_note_deletion_time_week, TimeUnit.DAYS.toSeconds(7)) { override val isDefault = true },
    TWO_WEEKS(R.string.preferences_note_deletion_time_two_weeks, TimeUnit.DAYS.toSeconds(14)),
    MONTH(R.string.preferences_note_deletion_time_month, TimeUnit.DAYS.toSeconds(30)),
    INSTANTLY(R.string.preferences_note_deletion_time_instantly, 0L);

    fun toDays() = TimeUnit.SECONDS.toDays(this.interval)
}

enum class DateFormat(val patternResource: Int) : EnumPreference by key("date_format") {
    MMMM_d_yyyy(R.string.MMMM_d_yyyy) { override val isDefault = true },
    d_MMMM_yyyy(R.string.d_MMMM_yyyy),
    MM_d_yyyy(R.string.MM_d_yyyy),
    d_MM_yyyy(R.string.d_MM_yyyy),
}

enum class TimeFormat(val patternResource: Int) : EnumPreference by key("time_format") {
    HH_mm(R.string.HH_mm) { override val isDefault = true },
    hh_mm(R.string.hh_mm),
}

enum class OpenMediaIn(override val nameResource: Int) : HasNameResource, EnumPreference by key("open_media") {
    INTERNAL(R.string.preferences_open_media_in_internal) { override val isDefault = true },
    EXTERNAL(R.string.preferences_open_media_in_external),
}

enum class ShowDate(override val nameResource: Int) : HasNameResource, EnumPreference by key("show_date") {
    YES(R.string.yes) { override val isDefault = true },
    NO(R.string.no),
}

enum class GroupNotesWithoutNotebook(
    override val nameResource: Int,
) : HasNameResource, EnumPreference by key("group_notes_without_notebook") {
    YES(R.string.yes),
    NO(R.string.no) { override val isDefault = true },
}

enum class CloudService(override val nameResource: Int) : HasNameResource, EnumPreference by key("cloud_service") {
    DISABLED(R.string.preferences_cloud_service_disabled) { override val isDefault = true },
    NEXTCLOUD(R.string.preferences_cloud_service_nextcloud),
}

enum class SyncMode(override val nameResource: Int) : HasNameResource, EnumPreference by key("sync_mode") {
    WIFI(R.string.preferences_sync_on_wifi) { override val isDefault = true },
    ALWAYS(R.string.preferences_sync_on_wifi_data),
}

enum class BackgroundSync(override val nameResource: Int) : HasNameResource, EnumPreference by key("background_sync") {
    ENABLED(R.string.preferences_background_sync_enabled) { override val isDefault = true },
    DISABLED(R.string.preferences_background_sync_disabled),
}

enum class NewNotesSyncable(override val nameResource: Int) : HasNameResource, EnumPreference by key("new_notes_syncable") {
    YES(R.string.yes) { override val isDefault = true },
    NO(R.string.no),
}
