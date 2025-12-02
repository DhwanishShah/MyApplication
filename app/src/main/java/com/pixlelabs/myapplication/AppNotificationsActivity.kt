package com.pixlelabs.myapplication

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppNotificationsActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter
    private var actionMode: ActionMode? = null
    private val viewModel: LatestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_app_notifications)

        val pkg = intent.getStringExtra("pkg") ?: return
        val appName = intent.getStringExtra("app_name") ?: pkg

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val recycler = findViewById<RecyclerView>(R.id.recyclerAppNoti)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        title = appName

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = NotificationAdapter(
            listener = { notification ->
                if (actionMode == null) {
                    try {
                        val launchIntent = packageManager.getLaunchIntentForPackage(notification.pkg)
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            selectionListener = { count ->
                if (count > 0) {
                    if (actionMode == null) {
                        actionMode = startSupportActionMode(ActionModeCallback())
                    }
                    actionMode?.title = "$count selected"
                } else {
                    actionMode?.finish()
                }
            }
        )
        recycler.adapter = adapter

        loadNotifications(pkg)
    }

    private fun loadNotifications(pkg: String) {
        val query = "SELECT * FROM notifications WHERE pkg = ? ORDER BY time DESC"
        val args = arrayOf(pkg)
        lifecycleScope.launch {
            viewModel.getNotifications(this@AppNotificationsActivity, query, args).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.contextual_action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val selectedItems = adapter.getSelectedItems()
            if (item.itemId == R.id.action_delete) {
                AlertDialog.Builder(this@AppNotificationsActivity)
                    .setTitle("Delete Notifications")
                    .setMessage("Are you sure you want to delete the selected notifications? This action cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        NotificationDB.delete(this@AppNotificationsActivity, selectedItems.map { it.id })
                        mode.finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return true
            } else if (item.itemId == R.id.action_favorite) {
                for (notification in selectedItems) {
                    NotificationDB.setFavorite(this@AppNotificationsActivity, notification.id, true)
                }
                mode.finish()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
            loadNotifications(intent.getStringExtra("pkg") ?: "")
        }
    }
}
