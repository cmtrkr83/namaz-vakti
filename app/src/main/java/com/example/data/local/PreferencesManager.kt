package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("namaz_vakti_prefs", Context.MODE_PRIVATE)

    var cityId: String
        get() = prefs.getString("city_id", "539") ?: "539"
        set(value) = prefs.edit().putString("city_id", value).apply()

    var cityName: String
        get() = prefs.getString("city_name", "İSTANBUL") ?: "İSTANBUL"
        set(value) = prefs.edit().putString("city_name", value).apply()

    var districtId: String
        get() = prefs.getString("district_id", "9541") ?: "9541"
        set(value) = prefs.edit().putString("district_id", value).apply()

    var districtName: String
        get() = prefs.getString("district_name", "İSTANBUL") ?: "İSTANBUL"
        set(value) = prefs.edit().putString("district_name", value).apply()

    // Notifications
    fun isPrayerNotificationEnabled(prayerKey: String): Boolean {
        return prefs.getBoolean("notif_enabled_$prayerKey", true)
    }

    fun setPrayerNotificationEnabled(prayerKey: String, enabled: Boolean) {
        prefs.edit().putBoolean("notif_enabled_$prayerKey", enabled).apply()
    }

    // Offset minutes: 0 means "Tam Vaktinde", >0 means minutes before prayer
    var notificationOffsetMinutes: Int
        get() = prefs.getInt("notif_offset_minutes", 0)
        set(value) = prefs.edit().putInt("notif_offset_minutes", value).apply()

    // Sound mode: "ezan", "bip", "vibrate"
    var notificationSoundMode: String
        get() = prefs.getString("notif_sound_mode", "ezan") ?: "ezan"
        set(value) = prefs.edit().putString("notif_sound_mode", value).apply()

    var notificationRepeatDaily: Boolean
        get() = prefs.getBoolean("notif_repeat_daily", true)
        set(value) = prefs.edit().putBoolean("notif_repeat_daily", value).apply()

    // Tasbih
    var tasbihCount: Int
        get() = prefs.getInt("tasbih_count", 0)
        set(value) = prefs.edit().putInt("tasbih_count", value).apply()

    var tasbihTarget: Int
        get() = prefs.getInt("tasbih_target", 33)
        set(value) = prefs.edit().putInt("tasbih_target", value).apply()

    var tasbihDhikrIndex: Int
        get() = prefs.getInt("tasbih_dhikr_index", 0)
        set(value) = prefs.edit().putInt("tasbih_dhikr_index", value).apply()
}
