package de.theess.eisbaer.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.theess.eisbaer.R
import de.theess.eisbaer.data.Note
import kotlinx.android.synthetic.main.fragment_search.*
import timber.log.Timber

/**
 * Shows a list of notes, which can be filtered using the search view in the app bar.
 */
class SearchFragment : Fragment(), SearchView.OnQueryTextListener {
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Timber.d("onCreateOptionsMenu")
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.option_search, menu)

        // Initialize Search View
        val searchView = menu.findItem(R.id.item_search)?.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(this)

        // Hide the keyboard when the search view loses focus.
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                searchView.clearFocus()
        }

        // Make search view always expanded
        searchView.setIconifiedByDefault(false)
        searchView.maxWidth = Integer.MAX_VALUE

        // Show current query in the search view.
        searchView.setQuery(viewModel.query.value, true)

        // handle swipe-to-refresh
        swiperefresh.setOnRefreshListener {
            viewModel.refresh()
            swiperefresh.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        // Set query from navigation arguments.
        arguments?.getString("query")?.also { query ->
            Timber.d("args: $query")
            viewModel.query(query)
        }

        search_results_recycler?.layoutManager = LinearLayoutManager(activity)

        val adapter = SearchResultAdapter(this::searchItemClicked)
        search_results_recycler?.adapter = adapter

        // Observe query results.
        viewModel.results.observe(this, Observer { items -> adapter.items = items })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        viewModel.query(query ?: "")
        return true
    }

    private fun searchItemClicked(note: Note) {
        Timber.d("clicked: ${note.id}")
        val action = SearchFragmentDirections.actionSearchToNoteView(note.id)
        findNavController().navigate(action)
    }
}
