package pl.birski.falldetector.model.util

import org.junit.jupiter.api.Test
import pl.birski.falldetector.database.AppDatabaseFake
import pl.birski.falldetector.database.model.ContactEntity
import pl.birski.falldetector.model.Contact

class ContactMapperTest {

    private val contactsEntity = AppDatabaseFake().contacts

    // system in test
    private val mapper = ContactMapper()

    @Test
    fun mapListOfContactEntityToListOfContacts() {

        val contacts = mapper.mapToDomainModelList(contactsEntity)

        // confirm that list is not empty
        assert(contacts.isNotEmpty())

        // confirm they are actually Contact objects
        assert(contacts.get(index = 0) is Contact)
    }

    @Test
    fun mapContactEntityToContact() {

        val contacts = mapper.mapToDomainModel(contactsEntity[0])

        // confirm that mapped object is Contact type
        assert(contacts is Contact)
    }

    @Test
    fun mapContactToContactEntity() {

        val contacts = mapper.mapToDomainModel(contactsEntity[0])

        // confirm that mapped object is Contact type
        assert(contacts is Contact)

        val contactEntity = mapper.mapFromDomainModel(contacts)

        // confirm that mapped object is ContactEntity type
        assert(contactEntity is ContactEntity)
    }
}
