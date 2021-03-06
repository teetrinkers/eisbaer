package de.theess.eisbaer.ui.note

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import de.theess.eisbaer.R
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.fragment_note_view.*
import org.commonmark.node.SoftLineBreak
import timber.log.Timber


/**
 * Shows the text content of a note.
 */
class NoteViewFragment : Fragment() {

    private val viewModel: NoteViewViewModel by viewModels()
    private val args: NoteViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        note_detail_text.movementMethod = LinkMovementMethod.getInstance()

        Timber.d("line height: ${note_detail_text.lineHeight}")

        // Configure Markwon
        val primaryColor = ContextCompat.getColor(view.context, R.color.colorPrimary)
        val markwon = Markwon.builder(view.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS))
            .usePlugin(TaskListPlugin.create(view.context))
            // theme
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.listItemColor(primaryColor)
                    builder.bulletWidth(note_detail_text.lineHeight / 4)
                    builder.blockQuoteColor(primaryColor)
                    builder.blockQuoteWidth(4)
                    builder.headingBreakHeight(0)
                    builder.headingTextSizeMultipliers(floatArrayOf(1.35f, 1.25f, 1.1f, 1f, 1f, 1f))
                    builder.thematicBreakHeight(2)
                    builder.linkColor(primaryColor)
                }
            })
            // soft line breaks
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(visitorBuilder: MarkwonVisitor.Builder) {
                    visitorBuilder.on(SoftLineBreak::class.java) { visitor, _ -> visitor.forceNewLine(); }
                }
            })
            .build()

        // Set action bar title
        viewModel.note(args.noteId)
            .observe(this, Observer { note ->
                markwon.setMarkdown(note_detail_text, note.content)
                (activity as AppCompatActivity).supportActionBar?.title = note.title
            })
    }
}
