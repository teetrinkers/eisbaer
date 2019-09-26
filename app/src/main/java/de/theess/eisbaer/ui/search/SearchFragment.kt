package de.theess.eisbaer.ui.search

import kotlinx.android.synthetic.main.fragment_search.*

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.theess.eisbaer.R
import timber.log.Timber

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchResultAdapter

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

//        viewModel.allItems.observe(this, Observer { items -> adapter?.items = items })


        search_results_recycler?.layoutManager = LinearLayoutManager(activity)

        adapter = SearchResultAdapter()
        search_results_recycler?.adapter = adapter

        showAllItems()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            query(query)
        }
        return true
    }

    private fun showAllItems() {
        adapter.items = viewModel.getAll()
    }

    private fun query(query: String) {
        Timber.d("query: $query")
        adapter.items = viewModel.query(query)
    }
}
