package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.FragmentContactsBinding
import pl.birski.falldetector.presentation.fragment.adapter.ContactAdapter
import pl.birski.falldetector.presentation.viewmodel.ContactsViewModel

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding

    private val viewModel: ContactsViewModel by viewModels()

    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var prefixEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var btnPositive: Button

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(
            chars: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            when (chars.hashCode()) {
                nameEditText.text.hashCode() -> viewModel.setContactData(
                    viewModel.getContact().copy(name = chars.toString())
                )

                surnameEditText.text.hashCode() -> viewModel.setContactData(
                    viewModel.getContact().copy(surname = chars.toString())
                )

                prefixEditText.text.hashCode() -> viewModel.setContactData(
                    viewModel.getContact().copy(prefix = chars.toString())
                )

                numberEditText.text.hashCode() -> viewModel.setContactData(
                    viewModel.getContact().copy(number = chars.toString())
                )
            }
            enableButton()
        }

        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) { }

        override fun afterTextChanged(s: Editable) { }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsBinding.inflate(inflater, container, false)

        viewModel.apply {

            contacts.observe(viewLifecycleOwner) { contacts ->

                binding.contactsRecycler.also {
                    it.layoutManager = LinearLayoutManager(requireContext())
                    it.setHasFixedSize(true)
                    it.adapter = ContactAdapter(contacts)
                }
                binding.contactsRecycler.visibility = View.VISIBLE
                binding.contactsRecycler.adapter?.notifyDataSetChanged()
                binding.contactsRecycler.scrollToPosition(0)
            }
        }

        binding.addContactButton.setOnClickListener {
            setAddContactDialog()
        }

        return binding.root
    }

    private fun setAddContactDialog() {

        val alertDialog = AlertDialog.Builder(requireContext()).create()

        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.add_contact_dialog, null)

        nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        surnameEditText = view.findViewById<EditText>(R.id.surnameEditText)
        prefixEditText = view.findViewById<EditText>(R.id.prefixEditText)
        numberEditText = view.findViewById<EditText>(R.id.phoneEditText)

        alertDialog.setTitle(R.string.contact_dialog_title_text)
        alertDialog.setView(view)

        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            requireContext().getText(R.string.contact_dialog_add_text)
        ) { dialog, _ ->
            viewModel.addContact()
            dialog.dismiss()
        }

        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            requireContext().getText(R.string.contact_dialog_cancel_text)
        ) { dialog, _ -> dialog.dismiss() }

        alertDialog.show()

        btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        enableButton()

        nameEditText.addTextChangedListener(textWatcher)
        surnameEditText.addTextChangedListener(textWatcher)
        prefixEditText.addTextChangedListener(textWatcher)
        numberEditText.addTextChangedListener(textWatcher)
    }

    private fun enableButton() {
        btnPositive.isEnabled = viewModel.enableButton()
    }
}
