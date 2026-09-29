# 🕌 Namaz Vakti & İmsakiye (Diyanet Entegrasyonlu)

[![Buy Me A Coffee](https://img.shields.io/badge/Buy_Me_A_Coffee-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://www.buymeacoffee.com/cmtrkr)

Modern, minimalist ve manevi atmosferi yüksek Android Namaz Vakitleri, İmsakiye, Kıble Pusulası ve Zikirmatik uygulaması. %100 **Jetpack Compose (Material 3)**, **Kotlin Coroutines & Flow**, **Room Local Database** ve **Diyanet İşleri Başkanlığı API** altyapısıyla geliştirilmiştir.

![Platform](https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?style=flat-square&logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue?style=flat-square)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVVM-orange?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

---

## ✨ Öne Çıkan Özellikler

- 🕋 **Diyanet İşleri Başkanlığı Doğrulanmış Vakitleri**: Türkiye'deki tüm 81 il ve ilçelere ait anlık ve aylık namaz vakitleri.
- 📍 **Akıllı GPS & Konum Tespiti**: `FusedLocationProviderClient` ve Geocoder desteğiyle kullanıcının bulunduğu şehri tek dokunuşla otomatik tespit edip vakitleri getirir.
- 📱 **Minimalist Ana Ekran Widget'ı (AppWidget)**: 
  - Şu an kılınabilir namaz vaktini dinamik gösterir (Örn: *Öğle Namazı - Vakit: 13:00 - 16:18*).
  - Sıradaki namaz vaktine kalan süreyi canlı geri sayımla sunar.
  - Günün 6 vaktini kompakt alt çubukta listeler.
- 🌊 **Likit Cam (Liquid Frosted Glass) Yüzen Alt Menü**: Arka planın üzerinde süzülen 32.dp yuvarlak hatlı, ışık yansıması (specular rim) ve zümrüt cam efektli modern floating navigation bar.
- 🌿 **Dingin Zümrüt & İslami Yeşil Gradyan Tasarım**: Gözü yormayan, manevi huzur veren derin orman çamı ve kraliyet zümrüt degrade arka planı ile altın sarısı aksanlar.
- 🧭 **Dinamik Kıble Pusulası**: Cihazın manyetometre ve ivmeölçer sensörlerini kullanarak kullanıcının koordinatlarından Mekke Kabe doğrultusunu (açı ve yön göstergesiyle) hesaplar.
- 📅 **30 Günlük Aylık İmsakiye**: Ramazan ve tüm aylar için miladi/hicri takvim uyumlu, bugünü vurgulayan detaylı vakit tablosu.
- 📿 **Akıllı Zikirmatik / Tesbih**: Dokunsal titreşim geri bildirimi, hedef sayacı (33, 99 vb.), sıfırlama ve hazır zikir seçenekleri (Sübhanallah, Elhamdülillah, Allahu Ekber vb.).
- 🔔 **Akıllı Vakit Bildirimleri**: Namaz vaktinde veya vakitten önce (15, 30 dk) ezan sesi ya da bildirim tonuyla uyarı mekanizması.
- 📖 **Günün Ayet & Hadis Kartı**: Her gün yenilenen ilham verici manevi sözler.
- ⚡ **Offline-First Mimari**: Vakitler ilk çekildiğinde yerel Room SQLite veritabanına kaydedilir; internet bağlantısı olmasa dahi vakitler ve geri sayım sorunsuz çalışır.

---

## 🛠 Kullanılan Teknolojiler & Kütüphaneler

- **Dil**: [Kotlin](https://kotlinlang.org/)
- **Kullanıcı Arayüzü**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Mimari**: MVVM (Model-View-ViewModel) + Offline-First Repository Pattern
- **Asenkron Programlama**: Kotlin Coroutines & `StateFlow`
- **Veri Saklama**: [Room Database](https://developer.android.com/training/data-storage/room) (SQLite ORM) & `SharedPreferences`
- **Ağ & API**: [Retrofit 2](https://square.github.io/retrofit/) + [Moshi Converter](https://github.com/square/moshi) + OkHttp Logging Interceptor
- **Konum Servisleri**: Google Play Services Location (`FusedLocationProviderClient`)
- **Sensörler**: Android `SensorManager` (Manyetik Alan & İvmeölçer)
- **Widget**: Android `AppWidgetProvider` & `RemoteViews`
- **Test**: JUnit 4 & [Robolectric](https://robolectric.org/)

---

## 📂 Proje Dizin Yapısı

```
app/src/main/
├── java/com/example/
│   ├── MainActivity.kt                  # Ana Activity & Edge-to-Edge yapılandırması
│   ├── data/
│   │   ├── local/                       # Room Entity, DAO, Database & Şehir verileri
│   │   ├── model/                       # DTO & Domain modelleri (PrayerTime, Schedule)
│   │   ├── remote/                      # Retrofit API servisleri ve NetworkClient
│   │   └── repository/                  # Offline-first PrayerRepository
│   ├── ui/
│   │   ├── components/                  # Vakit kartları, Likit bar, İmsakiye tabloları
│   │   ├── screens/                     # Vakitler, İmsakiye, Kıble, Bildirim, Zikirmatik
│   │   ├── theme/                       # Zümrüt Yeşili Gradyan, Renkler & Tipografi
│   │   └── viewmodel/                   # PrayerViewModel & UI State yönetimi
│   ├── util/
│   │   ├── LocationProvider.kt          # GPS & Ters Geocode konum yöneticisi
│   │   ├── QiblaCalculator.kt           # Kıble açısı ve kerahat hesaplayıcı
│   │   ├── CompassSensorHelper.kt       # Pusula donanım dinleyicisi
│   │   └── PrayerNotificationHelper.kt  # AlarmManager & Bildirim kanalı yöneticisi
│   └── widget/
│       └── PrayerWidgetProvider.kt      # Ana ekran widget mantığı ve güncelleme akışı
└── res/
    ├── layout/widget_prayer_times.xml   # Minimalist Widget tasarımı
    ├── xml/prayer_widget_info.xml       # AppWidgetProvider meta verisi
    └── values/strings.xml
```

---

## 🚀 Projeyi Çalıştırma

### Gereksinimler
- Android Studio Ladybug / Koala veya üzeri
- JDK 17+
- Android SDK 34 (Android 14) minimum SDK 26 (Android 8.0)

### Adımlar
1. Depoyu klonlayın:
   ```bash
   git clone https://github.com/<KULLANICI_ADINIZ>/namaz-vakti.git
   cd namaz-vakti
   ```
2. Projeyi Android Studio ile açın: `File -> Open -> (Proje Klasörü)`
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. Bir cihaz veya emülatör seçip **Run (Shift + F10)** butonuna basın.

### Komut Satırı ile Derleme:
```bash
# Debug APK derleme:
./gradlew assembleDebug

# Testleri çalıştırma:
./gradlew testDebugUnitTest
```

---

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) altında lisanslanmıştır. Dilediğiniz gibi geliştirebilir, fork edebilir ve katkıda bulunabilirsiniz.
