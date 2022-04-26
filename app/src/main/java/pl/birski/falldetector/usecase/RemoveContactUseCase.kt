package pl.birski.falldetector.usecase

import pl.birski.falldetector.database.AppDatabase
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.model.util.ContactMapper

class RemoveContactUseCase(
    private val database: AppDatabase,
    private val mapper: ContactMapper
) {
    suspend fun execute(contact: Contact) {
        return database.contactDao().deleteContact(mapper.mapFromDomainModel(contact))
    }
}