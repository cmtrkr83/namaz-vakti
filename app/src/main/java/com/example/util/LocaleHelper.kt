package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.StringRes
import com.example.R
import com.example.data.model.PrayerType
import com.example.data.model.WeatherCondition
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val shortBadge: String,
    val isRtl: Boolean = false
) {
    SYSTEM("system", "System", "AUTO", false),
    TURKISH("tr", "Türkçe", "TR", false),
    ENGLISH("en", "English", "EN", false),
    ARABIC("ar", "العربية", "AR", true),
    GERMAN("de", "Deutsch", "DE", false),
    FRENCH("fr", "Français", "FR", false);

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}

class LocalizedContextWrapper(
    base: Context,
    private val localizedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}

object LocaleHelper {

    fun resolveEffectiveLocale(languageCode: String): Locale {
        val selected = AppLanguage.fromCode(languageCode)
        return when (selected) {
            AppLanguage.SYSTEM -> {
                val systemLocale = Resources.getSystem().configuration.locales.get(0) ?: Locale.getDefault()
                when (systemLocale.language.lowercase(Locale.ROOT)) {
                    "tr", "en", "ar", "de", "fr" -> systemLocale
                    else -> Locale("tr", "TR")
                }
            }
            AppLanguage.TURKISH -> Locale("tr", "TR")
            AppLanguage.ENGLISH -> Locale("en", "US")
            AppLanguage.ARABIC -> Locale("ar", "SA")
            AppLanguage.GERMAN -> Locale("de", "DE")
            AppLanguage.FRENCH -> Locale("fr", "FR")
        }
    }

    fun isRtl(languageCode: String): Boolean {
        val selected = AppLanguage.fromCode(languageCode)
        return when (selected) {
            AppLanguage.SYSTEM -> resolveEffectiveLocale(languageCode).language.equals("ar", ignoreCase = true)
            else -> selected.isRtl
        }
    }

    fun wrapContext(base: Context, languageCode: String): Context {
        val locale = resolveEffectiveLocale(languageCode)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        val configContext = base.createConfigurationContext(config)
        return LocalizedContextWrapper(base, configContext.resources)
    }

    fun displayTitle(context: Context, language: AppLanguage): String {
        return if (language == AppLanguage.SYSTEM) {
            context.getString(R.string.language_system)
        } else {
            language.nativeName
        }
    }

    @StringRes
    fun prayerNameRes(type: PrayerType): Int = when (type) {
        PrayerType.IMSAK -> R.string.prayer_imsak
        PrayerType.GUNES -> R.string.prayer_gunes
        PrayerType.OGLE -> R.string.prayer_ogle
        PrayerType.IKINDI -> R.string.prayer_ikindi
        PrayerType.AKSAM -> R.string.prayer_aksam
        PrayerType.YATSI -> R.string.prayer_yatsi
    }

    @StringRes
    fun prayerSubtitleRes(type: PrayerType): Int = when (type) {
        PrayerType.IMSAK -> R.string.prayer_imsak_sub
        PrayerType.GUNES -> R.string.prayer_gunes_sub
        PrayerType.OGLE -> R.string.prayer_ogle_sub
        PrayerType.IKINDI -> R.string.prayer_ikindi_sub
        PrayerType.AKSAM -> R.string.prayer_aksam_sub
        PrayerType.YATSI -> R.string.prayer_yatsi_sub
    }

    @StringRes
    fun weatherConditionRes(condition: WeatherCondition): Int = when (condition) {
        WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> R.string.weather_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
        WeatherCondition.CLOUDY -> R.string.weather_cloudy
        WeatherCondition.FOGGY -> R.string.weather_foggy
        WeatherCondition.RAINY -> R.string.weather_rainy
        WeatherCondition.SNOWY -> R.string.weather_snowy
        WeatherCondition.THUNDERSTORM -> R.string.weather_thunderstorm
    }
}
