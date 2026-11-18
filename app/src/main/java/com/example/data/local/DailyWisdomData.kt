package com.example.data.local

import android.content.Context
import com.example.R

data class DailyWisdom(
    val title: String,
    val arabic: String?,
    val text: String,
    val source: String
)

private data class WisdomResourceEntry(
    val titleRes: Int,
    val arabic: String,
    val textRes: Int,
    val sourceRes: Int
)

object DailyWisdomData {
    private val entries = listOf(
        WisdomResourceEntry(
            titleRes = R.string.wisdom_title_ayah,
            arabic = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَوْقُوتًا",
            textRes = R.string.wisdom_text_1,
            sourceRes = R.string.wisdom_source_1
        ),
        WisdomResourceEntry(
            titleRes = R.string.wisdom_title_hadith,
            arabic = "أَحَبُّ الْأَعْمَالِ إِلَى اللَّهِ الصَّلَاةُ لِوَقْتِهَا",
            textRes = R.string.wisdom_text_2,
            sourceRes = R.string.wisdom_source_2
        ),
        WisdomResourceEntry(
            titleRes = R.string.wisdom_title_ayah,
            arabic = "وَأَقِمِ الصَّلَاةَ طَرَفَيِ النَّهَارِ وَزُلَفًا مِّنَ اللَّيْلِ",
            textRes = R.string.wisdom_text_3,
            sourceRes = R.string.wisdom_source_3
        ),
        WisdomResourceEntry(
            titleRes = R.string.wisdom_title_hadith,
            arabic = "مِفْتَاحُ الْجَنَّةِ الصَّلَاةُ",
            textRes = R.string.wisdom_text_4,
            sourceRes = R.string.wisdom_source_4
        ),
        WisdomResourceEntry(
            titleRes = R.string.wisdom_title_ayah,
            arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            textRes = R.string.wisdom_text_5,
            sourceRes = R.string.wisdom_source_5
        ),
        WisdomResourceEntry(
            titleRes = R.string.wisdom_title_hadith,
            arabic = "الصَّلَوَاتُ الْخَمْسُ كَفَّارَاتٌ لِمَا بَيْنَهُنَّ",
            textRes = R.string.wisdom_text_6,
            sourceRes = R.string.wisdom_source_6
        ),
        WisdomResourceEntry(
            titleRes = R.string.wisdom_title_ayah,
            arabic = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            textRes = R.string.wisdom_text_7,
            sourceRes = R.string.wisdom_source_7
        )
    )

    fun getTodayWisdom(context: Context? = null): DailyWisdom {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val entry = entries[dayOfYear % entries.size]
        return if (context != null) {
            DailyWisdom(
                title = context.getString(entry.titleRes),
                arabic = entry.arabic,
                text = context.getString(entry.textRes),
                source = context.getString(entry.sourceRes)
            )
        } else {
            DailyWisdom(
                title = "Günün Ayet-i Kerimesi",
                arabic = entry.arabic,
                text = "Şüphesiz namaz, müminler üzerine belirli vakitlerde farz kılınmıştır.",
                source = "Nisâ Suresi, 103. Ayet"
            )
        }
    }
}
