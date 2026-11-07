package com.cancionero.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import com.cancionero.app.data.model.Song

@Database(
    entities = [Song::class, Setlist::class, SetlistSong::class, Culto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun setlistDao(): SetlistDao
    abstract fun cultoDao(): CultoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cancionero.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
