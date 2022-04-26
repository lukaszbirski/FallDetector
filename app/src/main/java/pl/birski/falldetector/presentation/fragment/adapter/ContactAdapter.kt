package pl.birski.falldetector.presentation.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.birski.falldetector.databinding.ContactItemBinding
import pl.birski.falldetector.model.Contact

class ContactAdapter(
    private val contacts: List<Contact>,
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    override fun getItemCount() = contacts.size

    inner class ContactViewHolder(
        val binging: ContactItemBinding
    ) : RecyclerView.ViewHolder(binging.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactViewHolder(
            ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.binging.apply {
            personTextView.text = "${contacts[position].name} ${contacts[position].surname}"
            numberTextView.text = "${contacts[position].prefix} ${contacts[position].number}"
        }
    }
}
