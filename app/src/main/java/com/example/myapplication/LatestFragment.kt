package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class LatestFragment : Fragment(R.layout.fragment_latest), Searchable, Filterable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var allNotifications = listOf<NotificationModel>()
    private var currentFilter = "All Time"
    private var customStartTime: Long? = null
    private var customEndTime: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvLatest)
        adapter = NotificationAdapter(emptyList()) { /* Click listener can be handled here if needed */ }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun loadNotifications() {
        val cursor = when (currentFilter) {
            "Last Hour" -> NotificationDB.getSince(requireContext(), System.currentTimeMillis() - 3600 * 1000)
            "Last 24 Hours" -> NotificationDB.getSince(requireContext(), System.currentTimeMillis() - 24 * 3600 * 1000)
            "Custom Range" -> NotificationDB.getBetween(requireContext(), customStartTime ?: 0, customEndTime ?: Long.MAX_VALUE)
            else -> NotificationDB.getAll(requireContext())
        }

        val notifications = mutableListOf<NotificationModel>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val pkg = it.getString(it.getColumnIndexOrThrow("pkg"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val text = it.getString(it.getColumnIndexOrThrow("text"))
                val time = it.getLong(it.getColumnIndexOrThrow("time"))
                val image = it.getBlob(it.getColumnIndexOrThrow("image"))
                val appIcon = it.getBlob(it.getColumnIndexOrThrow("app_icon"))

                notifications.add(NotificationModel(id, pkg, title, text, time, image, null, appIcon))
            }
        }
        allNotifications = notifications
        adapter.updateList(allNotifications)
    }

    override fun onSearchQuery(query: String) {
        val filteredList = allNotifications.filter {
            it.title.contains(query, ignoreCase = true) || it.text.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)
    }

    override fun showFilterDialog() {
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
        val calendar = Calendar.getInstance()
        // Start Date Picker
        val startDatePicker = DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val startCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                }
                customStartTime = startCalendar.timeInMillis

                // End Date Picker
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
}
