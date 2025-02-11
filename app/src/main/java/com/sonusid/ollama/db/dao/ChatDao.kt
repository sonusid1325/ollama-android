    package com.sonusid.ollama.db.dao

    import androidx.room.*
    import com.sonusid.ollama.db.entity.Chat
    import kotlinx.coroutines.flow.Flow

    @Dao
    interface ChatDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUser(chat: Chat)

        @Query("SELECT * FROM user_table")
        fun getAllUsers(): Flow<List<Chat>>

        @Delete
        suspend fun deleteUser(chat: Chat)
    }