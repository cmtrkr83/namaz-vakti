package com.example.data.local

data class DailyWisdom(
    val title: String,
    val arabic: String?,
    val text: String,
    val source: String
)

object DailyWisdomData {
    private val wisdomList = listOf(
        DailyWisdom(
            title = "Günün Ayet-i Kerimesi",
            arabic = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَوْقُوتًا",
            text = "Şüphesiz namaz, müminler üzerine belirli vakitlerde farz kılınmıştır.",
            source = "Nisâ Suresi, 103. Ayet"
        ),
        DailyWisdom(
            title = "Günün Hadis-i Şerifi",
            arabic = "أَحَبُّ الْأَعْمَالِ إِلَى اللَّهِ الصَّلَاةُ لِوَقْتِهَا",
            text = "Allah katında amellerin en sevimlisi, vaktinde kılınan namazdır.",
            source = "Buhârî, Mevâkît 5; Müslim, Îmân 137"
        ),
        DailyWisdom(
            title = "Günün Ayet-i Kerimesi",
            arabic = "وَأَقِمِ الصَّلَاةَ طَرَفَيِ النَّهَارِ وَزُلَفًا مِّنَ اللَّيْلِ",
            text = "Gündüzün iki ucunda ve gecenin gündüze yakın zamanlarında namaz kıl. Muhakkak ki iyilikler kötülükleri yok eder.",
            source = "Hûd Suresi, 114. Ayet"
        ),
        DailyWisdom(
            title = "Günün Hadis-i Şerifi",
            arabic = "مِفْتَاحُ الْجَنَّةِ الصَّلَاةُ",
            text = "Cennetin anahtarı namaz, namazın anahtarı ise abdesttir.",
            source = "Tirmizî, Tahâret 1"
        ),
        DailyWisdom(
            title = "Günün Ayet-i Kerimesi",
            arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            text = "Bilesiniz ki, kalpler ancak Allah'ı anmakla huzur bulur.",
            source = "Ra'd Suresi, 28. Ayet"
        ),
        DailyWisdom(
            title = "Günün Hadis-i Şerifi",
            arabic = "الصَّلَوَاتُ الْخَمْسُ كَفَّارَاتٌ لِمَا بَيْنَهُنَّ",
            text = "Büyük günahlardan kaçınıldığı müddetçe, beş vakit namaz aralarındaki günahlara kefarettir.",
            source = "Müslim, Tahâret 16"
        ),
        DailyWisdom(
            title = "Günün Ayet-i Kerimesi",
            arabic = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            text = "Sabır ve namazla Allah'tan yardım isteyin. Şüphesiz namaz, Allah'a saygı duyanlardan başkasına ağır gelir.",
            source = "Bakara Suresi, 45. Ayet"
        )
    )

    fun getTodayWisdom(): DailyWisdom {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return wisdomList[dayOfYear % wisdomList.size]
    }
}
