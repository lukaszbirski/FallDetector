package pl.birski.falldetector.presentation.fragment.adapter

import pl.birski.falldetector.model.Contact

interface ContactAdapterListener {

    fun removeContact(contact: Contact)
}
