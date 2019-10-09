package de.theess.eisbaer.ui.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import de.theess.eisbaer.R
import kotlinx.android.synthetic.main.fragment_note_view.*

class NoteViewFragment : Fragment() {

    private lateinit var viewModel: NoteViewViewModel
    private val args: NoteViewFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NoteViewViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.note(args.noteId)
            .observe(this, Observer { note ->
                note_detail_text.text = note.content
                (activity as AppCompatActivity).supportActionBar?.title = note.title
            })
    }
}
