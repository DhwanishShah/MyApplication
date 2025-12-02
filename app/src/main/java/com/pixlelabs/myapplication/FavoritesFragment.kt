package com.pixlelabs.myapplication

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var emptyView: ViewGroup
    private val viewModel: LatestViewModel by viewModels()
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvFavorites)
        emptyView = view.findViewById(R.id.empty_view)
        val emptyTextView = emptyView.findViewById<TextView>(R.id.empty_text)
        emptyTextView.text = "Your favorite notifications will appear here."

        adapter = NotificationAdapter(
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

        // Show/hide empty view based on adapter item count
        adapter.addLoadStateListener {
            if (isAdded) { // Ensure fragment is attached
                emptyView.isVisible = adapter.itemCount == 0
            }
        }

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        if (!isAdded) return
        lifecycleScope.launch {
            viewModel.getFavorites(requireContext()).collectLatest { pagingData ->
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
            if (!isAdded) return false
            val favoriteItem = menu.findItem(R.id.action_favorite)
            favoriteItem.setIcon(android.R.drawable.btn_star_big_off)
            favoriteItem.setTitle("Unfavorite")
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (!isAdded) return false
            val selectedItems = adapter.getSelectedItems()

            if (item.itemId == R.id.action_delete) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Notifications")
                    .setMessage("Are you sure you want to delete the selected notifications? This action cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        NotificationDB.delete(requireContext(), selectedItems.map { it.id })
                        mode.finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return true
            } else if (item.itemId == R.id.action_favorite) {
                for (notification in selectedItems) {
                    NotificationDB.setFavorite(requireContext(), notification.id, false)
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
