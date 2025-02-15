package com.sonusid.ollama.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sonusid.ollama.db.entity.BaseUrl

@Dao
interface BaseUrlDao {

    @Query("SELECT * FROM base_url LIMIT 1")
    suspend fun getBaseUrl(): BaseUrl?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaseUrl(baseUrl: BaseUrl)

    @Query("UPDATE base_url SET url = :newUrl WHERE id = 1")
    suspend fun updateUrl(newUrl: String)
}
