    package com.sonusid.ollama.db.dao

    import androidx.room.*
    import com.sonusid.ollama.db.entity.User
    import kotlinx.coroutines.flow.Flow

    @Dao
    interface UserDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUser(user: User)

        @Query("SELECT * FROM user_table")
        fun getAllUsers(): Flow<List<User>>

        @Delete
        suspend fun deleteUser(user: User)
    }