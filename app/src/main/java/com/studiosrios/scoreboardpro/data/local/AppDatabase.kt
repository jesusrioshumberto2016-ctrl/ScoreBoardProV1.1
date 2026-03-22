package com.studiosrios.scoreboardpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studiosrios.scoreboardpro.CampeonatoSalvo
import com.studiosrios.scoreboardpro.EquipeExemplo
import com.studiosrios.scoreboardpro.JogadorExemplo

@Database(
    entities = [JogadorExemplo::class, EquipeExemplo::class, CampeonatoSalvo::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jogadorDao(): JogadorDao
    abstract fun equipeDao(): EquipeDao
    abstract fun campeonatoDao(): CampeonatoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scoreboard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
