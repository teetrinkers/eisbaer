package de.theess.eisbaer.data

data class Note(val id: String, val title: String, val content: String) {
    override fun toString(): String = title
}