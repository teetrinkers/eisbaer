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
class SearchResultAdapter : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val details: TextView = view.details

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
        val item = items[position]
        holder.title.text = item.title
        holder.details.text = item.content
        holder.view.tag = item
    }

    override fun getItemCount(): Int = items.size
}
