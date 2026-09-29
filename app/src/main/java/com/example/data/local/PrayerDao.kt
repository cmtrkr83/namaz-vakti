package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_times WHERE districtId = :districtId ORDER BY id ASC")
    fun getPrayerTimesForDistrict(districtId: String): Flow<List<PrayerTimeEntity>>

    @Query("SELECT * FROM prayer_times WHERE districtId = :districtId AND miladiTarihKisa = :dateKisa LIMIT 1")
    suspend fun getPrayerTimeForDate(districtId: String, dateKisa: String): PrayerTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(times: List<PrayerTimeEntity>)

    @Query("DELETE FROM prayer_times WHERE districtId = :districtId")
    suspend fun deleteDistrictTimes(districtId: String)

    @Query("DELETE FROM prayer_times")
    suspend fun clearAll()
}
