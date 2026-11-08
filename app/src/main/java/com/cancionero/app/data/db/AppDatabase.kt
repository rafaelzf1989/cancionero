package com.cancionero.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cancionero.app.data.model.Culto
import com.cancionero.app.data.model.CultoMusician
import com.cancionero.app.data.model.Moment
import com.cancionero.app.data.model.Musician
import com.cancionero.app.data.model.Setlist
import com.cancionero.app.data.model.SetlistSong
import com.cancionero.app.data.model.Song
import java.util.Calendar

@Database(
    entities = [
        Song::class, Setlist::class, SetlistSong::class, Culto::class,
        Moment::class, Musician::class, CultoMusician::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun setlistDao(): SetlistDao
    abstract fun cultoDao(): CultoDao
    abstract fun momentDao(): MomentDao
    abstract fun musicianDao(): MusicianDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN moment TEXT")
                db.execSQL("ALTER TABLE songs ADD COLUMN tags TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE setlists ADD COLUMN isTemplate INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `moments` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`position` INTEGER NOT NULL)"
                )
                val defaults = listOf(
                    "Entrada", "Adoración", "Alabanza", "Ofrenda",
                    "Comunión", "Especial", "Despedida"
                )
                defaults.forEachIndexed { idx, name ->
                    db.execSQL(
                        "INSERT INTO moments (name, position) VALUES (?, ?)",
                        arrayOf(name, idx)
                    )
                }
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cultos_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`dateMillis` INTEGER NOT NULL, " +
                        "`setlistId` INTEGER)"
                )
                val cursor = db.query(
                    "SELECT id, name, dayOfWeek, time, setlistId FROM cultos"
                )
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val name = cursor.getString(1)
                    val dow = cursor.getInt(2)
                    val time = cursor.getString(3) ?: "19:00"
                    val setlistId = if (cursor.isNull(4)) null else cursor.getLong(4)
                    val dateMillis = computeNextOccurrence(dow, time)
                    if (setlistId != null) {
                        db.execSQL(
                            "INSERT INTO cultos_new (id, name, dateMillis, setlistId) VALUES (?, ?, ?, ?)",
                            arrayOf<Any>(id, name, dateMillis, setlistId)
                        )
                    } else {
                        db.execSQL(
                            "INSERT INTO cultos_new (id, name, dateMillis, setlistId) VALUES (?, ?, ?, NULL)",
                            arrayOf<Any>(id, name, dateMillis)
                        )
                    }
                }
                cursor.close()
                db.execSQL("DROP TABLE cultos")
                db.execSQL("ALTER TABLE cultos_new RENAME TO cultos")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cultos ADD COLUMN leader TEXT")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `musicians` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`role` TEXT NOT NULL, " +
                        "`phone` TEXT, " +
                        "`notes` TEXT)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `culto_musicians` (" +
                        "`cultoId` INTEGER NOT NULL, " +
                        "`musicianId` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`cultoId`, `musicianId`), " +
                        "FOREIGN KEY(`cultoId`) REFERENCES `cultos`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`musicianId`) REFERENCES `musicians`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_culto_musicians_cultoId` " +
                        "ON `culto_musicians` (`cultoId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_culto_musicians_musicianId` " +
                        "ON `culto_musicians` (`musicianId`)"
                )
            }
        }

        private fun computeNextOccurrence(dayOfWeek: Int, time: String): Long {
            val cal = Calendar.getInstance()
            val targetDow = if (dayOfWeek == 7) 1 else dayOfWeek + 1
            val parts = time.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 19
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, m)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val currentDow = cal.get(Calendar.DAY_OF_WEEK)
            val daysToAdd = (targetDow - currentDow + 7) % 7
            cal.add(Calendar.DAY_OF_MONTH, daysToAdd)
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_MONTH, 7)
            }
            return cal.timeInMillis
        }

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cancionero.db"
                ).addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                    MIGRATION_4_5, MIGRATION_5_6
                ).build().also { INSTANCE = it }
            }
        }
    }
}