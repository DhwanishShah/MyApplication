package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        val appBar = findViewById<AppBarLayout>(R.id.app_bar_layout)

        setSupportActionBar(findViewById(R.id.toolbar))
        // **THE FIX**: Remove the title from the main toolbar
        supportActionBar?.title = ""

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top)
            insets
        }

        if (!isNotificationListenerEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        if (savedInstanceState == null) {
            loadFragment(LatestFragment())
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_latest -> loadFragment(LatestFragment())
                R.id.menu_apps -> loadFragment(AppsFragment())
                R.id.menu_favorites -> loadFragment(FavoritesFragment())
                R.id.menu_settings -> loadFragment(SettingsFragment())
            }
            invalidateOptionsMenu() 
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val filterItem = menu.findItem(R.id.action_filter)

        val isSearchable = currentFragment is Searchable
        val isFilterable = currentFragment is Filterable
        searchItem.isVisible = isSearchable
        filterItem.isVisible = isFilterable

        if (isSearchable) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    (currentFragment as? Searchable)?.onSearchQuery(newText.orEmpty())
                    return true
                }
            })
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter) {
            (currentFragment as? Filterable)?.showFilterDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: ""
        return enabledListeners.contains(packageName)
    }
}

interface Searchable {
    fun onSearchQuery(query: String)
}

interface Filterable {
    fun showFilterDialog()
}
