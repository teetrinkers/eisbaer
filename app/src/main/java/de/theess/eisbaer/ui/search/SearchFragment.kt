package de.theess.eisbaer.ui.search

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
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
    private var adapter: SearchResultAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewModel.allItems.observe(this, Observer { items -> adapter?.items = items })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated")

        searchView = view.findViewById(R.id.nav_search)

        recycler = view.findViewById(R.id.search_results_recycler)

        recycler?.layoutManager = LinearLayoutManager(activity)

        adapter = SearchResultAdapter()
        recycler?.adapter = adapter
    }

}
