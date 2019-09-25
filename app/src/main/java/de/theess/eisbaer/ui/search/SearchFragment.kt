package de.theess.eisbaer.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.theess.eisbaer.R
import timber.log.Timber

class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var viewModel: SearchViewModel

    private var searchView: SearchView? = null
    private var recycler: androidx.recyclerview.widget.RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated")

        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
//        viewModel.allItems.observe(this, Observer { items -> adapter?.items = items })

        searchView = view.findViewById(R.id.nav_search)

        recycler = view.findViewById(R.id.search_results_recycler)

        recycler?.layoutManager = LinearLayoutManager(activity)

        val adapter = SearchResultAdapter()
        recycler?.adapter = adapter

        val query = arguments?.get("query") as? String
        Timber.d("query: %s", query)
        if (query == null) {
            adapter.items = viewModel.getAll()
        } else {
            adapter.items = viewModel.query(query)
        }
    }

}
