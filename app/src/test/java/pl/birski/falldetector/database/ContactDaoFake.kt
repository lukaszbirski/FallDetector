package pl.birski.falldetector.database

import pl.birski.falldetector.database.dao.ContactDao
import pl.birski.falldetector.database.model.ContactEntity

class ContactDaoFake(
    private val appDatabaseFake: AppDatabaseFake
) : ContactDao {

    override suspend fun insertContact(contact: ContactEntity) {
        appDatabaseFake.contacts.add(contact)
    }

    override suspend fun getAllContacts(): List<ContactEntity> {
        return appDatabaseFake.contacts
    }

    override suspend fun deleteContact(contact: ContactEntity) {
        appDatabaseFake.contacts.removeIf { it.id == contact.id }
    }
}
