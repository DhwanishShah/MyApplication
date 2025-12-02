package com.example.myapplication

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvFavorites)

        adapter = NotificationAdapter(emptyList(),
            listener = { notification ->
                if (actionMode == null) {
                    try {
                        val intent = requireContext().packageManager.getLaunchIntentForPackage(notification.pkg)
                        if (intent != null) {
                            startActivity(intent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            selectionListener = { count ->
                if (count > 0) {
                    if (actionMode == null) {
                        actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(ActionModeCallback())
                    }
                    actionMode?.title = "$count selected"
                } else {
                    actionMode?.finish()
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        if (!isAdded) return
        val cursor = NotificationDB.getFavorites(requireContext())
        val favorites = mutableListOf<NotificationModel>()

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val pkg = it.getString(it.getColumnIndexOrThrow("pkg"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val text = it.getString(it.getColumnIndexOrThrow("text"))
                val time = it.getLong(it.getColumnIndexOrThrow("time"))
                val image = it.getBlob(it.getColumnIndexOrThrow("image"))
                val appIcon = it.getBlob(it.getColumnIndexOrThrow("app_icon"))
                val isFavorite = it.getInt(it.getColumnIndexOrThrow("is_favorite")) == 1

                favorites.add(NotificationModel(id, pkg, title, text, time, image, null, appIcon, isFavorite))
            }
        }
        adapter.updateList(favorites)
    }

    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.contextual_action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            if (!isAdded) return false
            val favoriteItem = menu.findItem(R.id.action_favorite)
            favoriteItem.setIcon(android.R.drawable.btn_star_big_off)
            favoriteItem.setTitle("Unfavorite")
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (!isAdded) return false
            if (item.itemId == R.id.action_delete) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Notifications")
                    .setMessage("Are you sure you want to delete the selected notifications? This action cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        val selectedIds = adapter.getSelectedItems().map { it.id }
                        NotificationDB.delete(requireContext(), selectedIds)
                        mode.finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return true
            } else if (item.itemId == R.id.action_favorite) {
                val selectedIds = adapter.getSelectedItems().map { it.id }
                for (id in selectedIds) {
                    NotificationDB.setFavorite(requireContext(), id, false)
                }
                mode.finish()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
            if (isAdded) {
                loadFavorites()
            }
        }
    }
}
