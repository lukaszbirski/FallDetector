package pl.birski.falldetector.presentation.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.ContactItemBinding
import pl.birski.falldetector.model.Contact

class ContactAdapter(
    private val contacts: ArrayList<Contact>,
    private val context: Context
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
            personTextView.text = context.getString(
                R.string.template_person_name,
                contacts[position].name,
                contacts[position].surname
            )
            numberTextView.text = context.getString(
                R.string.template_phone_number,
                contacts[position].prefix,
                contacts[position].number
            )
        }
    }

    fun deleteItem(position: Int) {
        contacts.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getContact(position: Int) = contacts[position]
}
