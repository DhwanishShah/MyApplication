package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LatestFragment : Fragment(R.layout.fragment_latest), Searchable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var allNotifications = listOf<NotificationModel>()

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
        val cursor = NotificationDB.getAll(requireContext())
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
}
