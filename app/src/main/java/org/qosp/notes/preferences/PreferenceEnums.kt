package org.qosp.notes.preferences

import androidx.appcompat.app.AppCompatDelegate
import org.qosp.notes.R
import java.util.concurrent.TimeUnit

enum class LayoutMode(override val nameResource: Int) : Preference<LayoutMode> by key("layout_mode") {
    GRID(R.string.preferences_layout_mode_grid),
    LIST(R.string.preferences_layout_mode_list);

    companion object {
        fun default() = GRID
    }
}

enum class ThemeMode(override val nameResource: Int, val mode: Int) : Preference<ThemeMode> by key("theme_mode") {
    SYSTEM(R.string.preferences_theme_mode_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    DARK(R.string.preferences_theme_mode_dark, AppCompatDelegate.MODE_NIGHT_YES),
    LIGHT(R.string.preferences_theme_mode_light, AppCompatDelegate.MODE_NIGHT_NO);

    companion object {
        fun default() = SYSTEM
    }
}

enum class ColorScheme(override val nameResource: Int, val styleResource: Int) :
    Preference<ColorScheme> by key("color_scheme") {
    BLUE(R.string.preferences_color_scheme_blue, R.style.Blue),
    GREEN(R.string.preferences_color_scheme_green, R.style.Green),
    PINK(R.string.preferences_color_scheme_pink, R.style.Pink),
    YELLOW(R.string.preferences_color_scheme_orange, R.style.Orange),
    RED(R.string.preferences_color_scheme_purple, R.style.Purple);

    companion object {
        fun default() = BLUE
    }
}

enum class SortMethod(override val nameResource: Int) : Preference<SortMethod> by key("sort_method") {
    TITLE_ASC(R.string.preferences_sort_method_title_asc),
    TITLE_DESC(R.string.preferences_sort_method_title_desc),
    CREATION_ASC(R.string.preferences_sort_method_created_asc),
    CREATION_DESC(R.string.preferences_sort_method_created_desc),
    MODIFIED_ASC(R.string.preferences_sort_method_modified_asc),
    MODIFIED_DESC(R.string.preferences_sort_method_modified_desc);

    companion object {
        fun default() = MODIFIED_DESC
    }
}

enum class BackupStrategy(override val nameResource: Int) : Preference<BackupStrategy> by key("backup_strategy") {
    INCLUDE_FILES(R.string.preferences_backup_strategy_include_files),
    KEEP_INFO(R.string.preferences_backup_strategy_keep_info),
    KEEP_NOTHING(R.string.preferences_backup_strategy_keep_nothing);

    companion object {
        fun default() = INCLUDE_FILES
    }
}

enum class NoteDeletionTime(
    override val nameResource: Int,
    val interval: Long,
) : Preference<NoteDeletionTime> by key("note_deletion_time") {

    WEEK(R.string.preferences_note_deletion_time_week, TimeUnit.DAYS.toSeconds(7)),
    TWO_WEEKS(R.string.preferences_note_deletion_time_two_weeks, TimeUnit.DAYS.toSeconds(14)),
    MONTH(R.string.preferences_note_deletion_time_month, TimeUnit.DAYS.toSeconds(30)),
    INSTANTLY(R.string.preferences_note_deletion_time_instantly, 0L);

    companion object {
        fun default() = WEEK
    }
}

enum class DateFormat(val patternResource: Int) : Preference<DateFormat> by key("date_format") {
    MMMM_d_yyyy(R.string.MMMM_d_yyyy),
    d_MMMM_yyyy(R.string.d_MMMM_yyyy),
    MM_d_yyyy(R.string.MM_d_yyyy),
    d_MM_yyyy(R.string.d_MM_yyyy);

    companion object {
        fun default() = MMMM_d_yyyy
    }
}

enum class TimeFormat(val patternResource: Int) : Preference<TimeFormat> by key("time_format") {
    HH_mm(R.string.HH_mm),
    hh_mm(R.string.hh_mm);

    companion object {
        fun default() = HH_mm
    }
}

enum class OpenMediaIn(override val nameResource: Int) : Preference<OpenMediaIn> by key("open_media") {
    INTERNAL(R.string.preferences_open_media_in_internal),
    EXTERNAL(R.string.preferences_open_media_in_external);

    companion object {
        fun default() = INTERNAL
    }
}

enum class CloudService(override val nameResource: Int) : Preference<CloudService> by key("cloud_service") {
    DISABLED(R.string.preferences_cloud_service_disabled),
    NEXTCLOUD(R.string.preferences_cloud_service_nextcloud);

    companion object {
        fun default() = DISABLED
    }
}

enum class SyncMode(override val nameResource: Int) : Preference<SyncMode> by key("sync_mode") {
    WIFI(R.string.preferences_sync_on_wifi),
    ALWAYS(R.string.preferences_sync_on_wifi_data);

    companion object {
        fun default() = WIFI
    }
}

enum class BackgroundSync(override val nameResource: Int) : Preference<BackgroundSync> by key("background_sync") {
    ENABLED(R.string.preferences_background_sync_enabled),
    DISABLED(R.string.preferences_background_sync_disabled);

    companion object {
        fun default() = ENABLED
    }
}

enum class NewNotesSyncable(override val nameResource: Int) : Preference<NewNotesSyncable> by key("new_notes_syncable") {
    YES(R.string.yes),
    NO(R.string.no);

    companion object {
        fun default() = YES
    }
}
