package de.theess.eisbaer.ui.search

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.theess.eisbaer.R
import de.theess.eisbaer.data.Note
import kotlinx.android.synthetic.main.fragment_search.*
import timber.log.Timber

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Timber.d("onCreateOptionsMenu")
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.option_search, menu)

        // Initialize Search View
        val searchItem = menu.findItem(R.id.item_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        // Show current query in the search view.
        val currentQuery = viewModel.query.value
        if (currentQuery != null && currentQuery.isNotEmpty())
            searchItem.expandActionView()
        searchView.setQuery(currentQuery, true)
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

        search_results_recycler?.layoutManager = LinearLayoutManager(activity)

        val adapter = SearchResultAdapter(this::searchItemClicked)
        search_results_recycler?.adapter = adapter

        viewModel.results.observe(this, Observer { items -> adapter.items = items })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        viewModel.query(query ?: "")
        return true
    }

    private fun searchItemClicked(note : Note) {
        Timber.d("clicked: $note")
        val action = SearchFragmentDirections.actionSearchToNoteView(note.id)
        findNavController().navigate(action)
    }
}
