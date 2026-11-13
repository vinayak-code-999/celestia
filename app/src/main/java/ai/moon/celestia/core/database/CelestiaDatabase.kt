package ai.moon.celestia.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        SavedSummaryEntity::class,
        TranslationHistoryEntity::class,
        CustomPromptEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CelestiaDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun summaryDao(): SummaryDao
    abstract fun translationDao(): TranslationDao
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: CelestiaDatabase? = null

        fun getInstance(context: Context): CelestiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CelestiaDatabase::class.java,
                    "celestia_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
