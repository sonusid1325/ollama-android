package com.sonusid.ollama.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sonusid.ollama.db.entity.SelectedModel

@Dao
interface ModelPreferenceDao {
    @Query("SELECT * FROM selected_model WHERE baseUrl = :baseUrl LIMIT 1")
    suspend fun getByBaseUrl(baseUrl: String): SelectedModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: SelectedModel)

    @Query("DELETE FROM selected_model WHERE baseUrl = :baseUrl")
    suspend fun deleteByBaseUrl(baseUrl: String)

    @Query("SELECT baseUrl FROM selected_model")
    suspend fun getAllBaseUrls(): List<String>

    @Query("DELETE FROM selected_model WHERE baseUrl NOT IN (:baseUrls)")
    suspend fun deleteAllExcept(baseUrls: List<String>)

    @Query("DELETE FROM selected_model")
    suspend fun clearAll()
}
