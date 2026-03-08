package com.sonusid.ollama.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sonusid.ollama.db.entity.BaseUrl

@Dao
interface BaseUrlDao {
    @Query("SELECT * FROM base_url")
    suspend fun getAll(): List<BaseUrl>

    @Query("SELECT * FROM base_url WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): BaseUrl?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(baseUrl: BaseUrl): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(baseUrls: List<BaseUrl>): List<Long>

    @Update
    suspend fun update(baseUrl: BaseUrl)

    @Delete
    suspend fun delete(baseUrl: BaseUrl)

    @Query("DELETE FROM base_url WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM base_url")
    suspend fun clear()

    @Query("UPDATE base_url SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE base_url SET isActive = 1 WHERE id = :id")
    suspend fun activateById(id: Int)

    @Transaction
    suspend fun setActive(id: Int) {
        clearActive()
        activateById(id)
    }

    @Transaction
    suspend fun replaceBaseUrls(baseUrls: List<BaseUrl>) {
        clear()
        val insertedIds = insertAll(baseUrls.map { it.copy(isActive = false) })
        val activeIndex = baseUrls.indexOfFirst { it.isActive }
        val activeId = when {
            activeIndex in insertedIds.indices -> insertedIds[activeIndex].toInt()
            insertedIds.isNotEmpty() -> insertedIds.first().toInt()
            else -> null
        }
        activeId?.let { activateById(it) }
    }
}
