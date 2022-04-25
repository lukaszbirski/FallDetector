package pl.birski.falldetector.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.birski.falldetector.database.dao.ContactDao
import pl.birski.falldetector.database.model.ContactEntity

@Database(entities = [ContactEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        val DATABASE_NAME: String = "falldetector_db"
    }
}
