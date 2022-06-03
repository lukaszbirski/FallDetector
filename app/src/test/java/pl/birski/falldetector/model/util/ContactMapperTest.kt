package pl.birski.falldetector.model.util

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.birski.falldetector.database.AppDatabaseFake
import pl.birski.falldetector.database.model.ContactEntity
import pl.birski.falldetector.model.Contact

class ContactMapperTest {

    private val contactsEntity = AppDatabaseFake().contacts

    // system in test
    private lateinit var mapper: ContactMapper

    @BeforeEach
    fun setup() {
        mapper = ContactMapper()
    }

    @Test
    fun `map list of contactEntity to list of contacts`() {

        val contacts = mapper.mapToDomainModelList(contactsEntity)

        // confirm that list is not empty
        assert(contacts.isNotEmpty())

        // confirm they are actually Contact objects
        assert(contacts.get(index = 0) is Contact)
    }

    @Test
    fun `map contactEntity to contact`() {

        val contacts = mapper.mapToDomainModel(contactsEntity[0])

        // confirm that mapped object is Contact type
        assert(contacts is Contact)
    }

    @Test
    fun `map contact to contact entity`() {

        val contacts = mapper.mapToDomainModel(contactsEntity[0])

        // confirm that mapped object is Contact type
        assert(contacts is Contact)

        val contactEntity = mapper.mapFromDomainModel(contacts)

        // confirm that mapped object is ContactEntity type
        assert(contactEntity is ContactEntity)
    }
}
