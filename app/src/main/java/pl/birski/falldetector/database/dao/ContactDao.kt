package pl.birski.falldetector.database.dao

import androidx.room.* // ktlint-disable no-wildcard-imports
import pl.birski.falldetector.database.model.ContactEntity

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<ContactEntity>

    @Delete
    suspend fun deleteContact(contact: ContactEntity)
}
