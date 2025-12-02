package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class AppNotificationsActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter
    private var actionMode: ActionMode? = null

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

        adapter = NotificationAdapter(emptyList(),
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
        val cursor = NotificationDB.getByPackage(this, pkg)
        val list = mutableListOf<NotificationModel>()

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val notificationTitle = it.getString(it.getColumnIndexOrThrow("title"))
                val text = it.getString(it.getColumnIndexOrThrow("text"))
                val time = it.getLong(it.getColumnIndexOrThrow("time"))
                val image = it.getBlob(it.getColumnIndexOrThrow("image"))
                val appIcon = it.getBlob(it.getColumnIndexOrThrow("app_icon"))
                val isFavorite = it.getInt(it.getColumnIndexOrThrow("is_favorite")) == 1

                list.add(
                    NotificationModel(
                        id = id,
                        pkg = pkg,
                        title = notificationTitle,
                        text = text,
                        time = time,
                        image = image,
                        pendingIntent = null,
                        appIcon = appIcon,
                        isFavorite = isFavorite
                    )
                )
            }
        }

        adapter.updateList(list.sortedByDescending { it.time })
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
            if (item.itemId == R.id.action_delete) {
                AlertDialog.Builder(this@AppNotificationsActivity)
                    .setTitle("Delete Notifications")
                    .setMessage("Are you sure you want to delete the selected notifications? This action cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        val selectedIds = adapter.getSelectedItems().map { it.id }
                        NotificationDB.delete(this@AppNotificationsActivity, selectedIds)
                        mode.finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return true
            } else if (item.itemId == R.id.action_favorite) {
                val selectedIds = adapter.getSelectedItems().map { it.id }
                for (id in selectedIds) {
                    NotificationDB.setFavorite(this@AppNotificationsActivity, id, true)
                }
                mode.finish()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
            // The activity context is always available, so no need for a check here.
            loadNotifications(intent.getStringExtra("pkg") ?: "")
        }
    }
}
