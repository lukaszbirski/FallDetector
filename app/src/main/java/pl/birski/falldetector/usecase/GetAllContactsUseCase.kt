package pl.birski.falldetector.usecase

import pl.birski.falldetector.database.dao.ContactDao
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.model.util.ContactMapper

class GetAllContactsUseCase(
    private val contactDao: ContactDao,
    private val mapper: ContactMapper
) {
    suspend fun execute(): List<Contact> {
        return mapper.mapToDomainModelList(contactDao.getAllContacts())
    }
}
