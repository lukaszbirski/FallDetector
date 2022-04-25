package pl.birski.falldetector.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import pl.birski.falldetector.model.Contact

@HiltViewModel
class ContactsViewModel
@Inject
constructor() : ViewModel() {

    private var contact = Contact()

    fun setContactData(contact: Contact) {
        this.contact = contact
    }

    fun getContact() = contact

    fun addContact() {
        addPlusToPrefix()
    }

    private fun addPlusToPrefix() {
        contact = contact.copy(prefix = "+${contact.prefix}")
    }

    fun enableButton() =
        contact.name.isNotBlank() &&
            contact.surname.isNotBlank() &&
            contact.prefix.isNotBlank() &&
            contact.number.isNotBlank()
}
