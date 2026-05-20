package com.example.habita.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preferences WHERE userId = :userId LIMIT 1")
    fun getPreferences(userId: String): Flow<Preference?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preference: Preference)

    @Update
    suspend fun updatePreferences(preference: Preference)
}