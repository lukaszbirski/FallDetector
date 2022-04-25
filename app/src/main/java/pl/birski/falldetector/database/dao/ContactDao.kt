package pl.birski.falldetector.database.dao

import androidx.room.Dao
import androidx.room.Insert
import pl.birski.falldetector.database.model.ContactEntity

@Dao
interface ContactDao {

    @Insert
    fun insertContact(contact: ContactEntity): Long
}
