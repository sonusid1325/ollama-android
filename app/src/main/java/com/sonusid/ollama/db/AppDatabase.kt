package com.sonusid.ollama.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sonusid.ollama.db.dao.BaseUrlDao
import com.sonusid.ollama.db.dao.ModelPreferenceDao
import com.sonusid.ollama.db.entity.BaseUrl
import com.sonusid.ollama.db.entity.SelectedModel

@Database(entities = [BaseUrl::class, SelectedModel::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun baseUrlDao(): BaseUrlDao
    abstract fun modelPreferenceDao(): ModelPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `base_url_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `url` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO `base_url_new` (`url`, `isActive`)
                    SELECT `url`, 1 FROM `base_url` LIMIT 1
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE `base_url`")
                database.execSQL("ALTER TABLE `base_url_new` RENAME TO `base_url`")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `selected_model` (
                        `baseUrl` TEXT NOT NULL,
                        `modelName` TEXT NOT NULL,
                        PRIMARY KEY(`baseUrl`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
