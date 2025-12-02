package com.pixlelabs.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import java.util.Calendar

class LatestFragment : Fragment(R.layout.fragment_latest), Searchable, Filterable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var emptyView: ViewGroup
    private val viewModel: LatestViewModel by viewModels()
    private var currentFilter = "All Time"
    private var customStartTime: Long? = null
    private var customEndTime: Long? = null
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvLatest)
        emptyView = view.findViewById(R.id.empty_view)

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

        loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun loadNotifications() {
        if (!isAdded) return

        val queryInfo = when (currentFilter) {
            "Last Hour" -> Pair("SELECT * FROM notifications WHERE time >= ? ORDER BY time DESC", arrayOf((System.currentTimeMillis() - 3600 * 1000).toString()))
            "Last 24 Hours" -> Pair("SELECT * FROM notifications WHERE time >= ? ORDER BY time DESC", arrayOf((System.currentTimeMillis() - 24 * 3600 * 1000).toString()))
            "Custom Range" -> Pair("SELECT * FROM notifications WHERE time BETWEEN ? AND ? ORDER BY time DESC", arrayOf((customStartTime ?: 0).toString(), (customEndTime ?: Long.MAX_VALUE).toString()))
            else -> Pair("SELECT * FROM notifications ORDER BY time DESC", null)
        }

        lifecycleScope.launch {
            viewModel.getNotifications(requireContext(), queryInfo.first, queryInfo.second).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onSearchQuery(query: String) {
        if (!isAdded) return
        val searchQuery = if (query.isEmpty()) {
            "SELECT * FROM notifications ORDER BY time DESC"
        } else {
            "SELECT * FROM notifications WHERE title LIKE ? OR text LIKE ? ORDER BY time DESC"
        }
        val args = if (query.isEmpty()) null else arrayOf("%$query%", "%$query%")

        lifecycleScope.launch {
            viewModel.getNotifications(requireContext(), searchQuery, args).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun showFilterDialog() {
        if (!isAdded) return
        val options = arrayOf("Last Hour", "Last 24 Hours", "All Time", "Custom Range...")
        val currentSelection = options.indexOf(currentFilter)

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Time")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                if (options[which] == "Custom Range...") {
                    showCustomDateRangePicker()
                } else {
                    currentFilter = options[which]
                    loadNotifications()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showCustomDateRangePicker() {
        if (!isAdded) return
        val calendar = Calendar.getInstance()
        val startDatePicker = DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val startCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                }
                customStartTime = startCalendar.timeInMillis

                val endDatePicker = DatePickerDialog(requireContext(),
                    { _, endYear, endMonth, endDayOfMonth ->
                        val endCalendar = Calendar.getInstance().apply {
                            set(endYear, endMonth, endDayOfMonth, 23, 59, 59)
                        }
                        customEndTime = endCalendar.timeInMillis
                        currentFilter = "Custom Range"
                        loadNotifications()
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                endDatePicker.setTitle("Select End Date")
                endDatePicker.show()

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        startDatePicker.setTitle("Select Start Date")
        startDatePicker.show()
    }

    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.contextual_action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode:ActionMode, menu: Menu): Boolean {
            return false
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
                    NotificationDB.setFavorite(requireContext(), notification.id, true)
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
                loadNotifications()
            }
        }
    }
}
