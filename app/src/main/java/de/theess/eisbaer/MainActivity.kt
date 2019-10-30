package de.theess.eisbaer

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import de.theess.eisbaer.data.TagRepository
import de.theess.eisbaer.ui.note.NoteViewFragmentDirections
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = drawer_layout
        val navView: NavigationView = nav_view
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_search
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Add tags from the database to the drawer menu
        TagRepository.getInstance(application as EisbaerApplication).getAll()
            .observe(this, Observer { tags ->
                navView.menu.removeGroup(R.id.drawer_menu_group_tags)
                tags.map { it.title }
                    .forEach { tag ->
                        addTagMenuItem(navView.menu, tag)
                    }
            })
    }

    /**
     * Adds a menu item which when clicked will navigate to the search fragment with the tag name
     * given as the query.
     */
    private fun addTagMenuItem(menu: Menu, tagName: String) {
        val menuItem = menu.add(R.id.drawer_menu_group_tags, Menu.NONE, 0, tagName)
        val action = NoteViewFragmentDirections.actionGlobalNavSearch("#" + tagName)
        menuItem.setOnMenuItemClickListener {
            navController.navigate(action)
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Timber.d("onCreateOptionsMenu")
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
                || super.onOptionsItemSelected(item)
    }
}
