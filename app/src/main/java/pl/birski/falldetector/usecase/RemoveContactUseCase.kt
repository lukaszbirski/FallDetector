package pl.birski.falldetector.usecase

import pl.birski.falldetector.database.dao.ContactDao
import pl.birski.falldetector.model.Contact
import pl.birski.falldetector.model.util.ContactMapper

class RemoveContactUseCase(
    private val contactDao: ContactDao,
    private val mapper: ContactMapper
) {
    suspend fun execute(contact: Contact) {
        return contactDao.deleteContact(mapper.mapFromDomainModel(contact))
    }
}
