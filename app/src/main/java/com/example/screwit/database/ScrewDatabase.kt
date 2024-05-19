package com.example.screwit.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.screwit.model.PlayerModel

@Database(entities = [PlayerModel::class], version = 1, exportSchema = false)
abstract class ScrewDatabase : RoomDatabase(){

    abstract fun playerDao() : PlayerDao

    companion object {
        @Volatile private var Instance: ScrewDatabase? = null
        fun getDatabase(context: Context): ScrewDatabase {
            return  Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScrewDatabase::class.java,
                    "player.db"
                ).build()
                Instance = instance
                instance
            }
        }
    }
}