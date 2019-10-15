package de.theess.eisbaer.ui.search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.theess.eisbaer.R


import de.theess.eisbaer.data.Note

import kotlinx.android.synthetic.main.search_result_card.view.*

/**
 */
class SearchResultAdapter(private val clickListener: (Note) -> Unit) :
    RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.title
        private val details: TextView = view.details

        fun bind(note: Note, clickListener: (Note) -> Unit) {
            title.text = note.title
            details.text = note.subtitle
            view.setOnClickListener { clickListener(note) }
        }

        override fun toString(): String {
            return super.toString() + " '" + title.text + "'"
        }
    }

    var items: List<Note> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = items[position]
        holder.bind(note, clickListener)
    }

    override fun getItemCount(): Int = items.size
}
