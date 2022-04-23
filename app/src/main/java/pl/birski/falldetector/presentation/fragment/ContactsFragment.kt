package pl.birski.falldetector.presentation.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.FragmentContactsBinding
import pl.birski.falldetector.presentation.viewmodel.ContactsViewModel

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding

    private val viewModel: ContactsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsBinding.inflate(inflater, container, false)

        binding.addContactButton.setOnClickListener {
            setAddContactDialog()
        }

        return binding.root
    }

    private fun setAddContactDialog() {
        this.let {
            AlertDialog.Builder(requireContext()).apply {
                setView(
                    LayoutInflater.from(requireContext()).inflate(R.layout.add_contact_dialog, null)
                )
                setTitle(R.string.contact_dialog_title_text)
                setCancelable(false)
                setPositiveButton(
                    R.string.contact_dialog_add_text,
                    DialogInterface.OnClickListener { dialog, id ->
                    }
                )
                setNegativeButton(
                    R.string.contact_dialog_cancel_text,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                    }
                )
            }
                .let {
                    it.create()
                    it.setCancelable(false)
                    it.show()
                }
        }
    }
}
