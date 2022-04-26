package pl.birski.falldetector.usecase

import pl.birski.falldetector.database.AppDatabase
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.model.util.ContactMapper

class GetAllContactsUseCase(
    private val database: AppDatabase,
    private val mapper: ContactMapper
) {
    suspend fun execute(): List<Contact> {
        return mapper.mapToDomainModelList(database.contactDao().getAllContacts())
    }
}
