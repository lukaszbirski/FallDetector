package pl.birski.falldetector.model.util

import pl.birski.falldetector.database.model.ContactEntity
import pl.birski.falldetector.model.Contact

class ContactMapper : DomainMapper<ContactEntity, Contact> {

    override fun mapToDomainModel(entity: ContactEntity): Contact {
        return Contact(
            name = entity.name,
            surname = entity.surname,
            prefix = entity.prefix,
            number = entity.number
        )
    }

    override fun mapToDomainModelList(initial: List<ContactEntity>): List<Contact> {
        return initial.map { mapToDomainModel(it) }
    }

    override fun mapFromDomainModel(domainModel: Contact): ContactEntity {
        return ContactEntity(
            id = null,
            name = domainModel.name,
            surname = domainModel.surname,
            prefix = domainModel.prefix,
            number = domainModel.number
        )
    }
}
