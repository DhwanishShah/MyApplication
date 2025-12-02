package com.pixlelabs.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsFragment : Fragment(R.layout.fragment_apps), Searchable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private lateinit var emptyView: ViewGroup
    private var allApps = listOf<App>()
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvApps)
        emptyView = view.findViewById(R.id.empty_view)
        val emptyTextView = emptyView.findViewById<TextView>(R.id.empty_text)
        emptyTextView.text = "When you receive notifications, the apps they came from will appear here."

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AppAdapter(emptyList(),
            listener = { app ->
                if (actionMode == null) {
                    val intent = Intent(requireContext(), AppNotificationsActivity::class.java)
                    intent.putExtra("pkg", app.packageName)
                    intent.putExtra("app_name", app.appName)
                    startActivity(intent)
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
        recyclerView.adapter = adapter

        // The logic to show/hide the empty view is handled in loadAppsFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        loadAppsFromDatabase()
    }

    private fun loadAppsFromDatabase() {
        if (!isAdded) return
        val pm = requireActivity().packageManager
        val cursor = NotificationDB.getDistinctApps(requireContext())
        val apps = mutableListOf<App>()

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val pkg = it.getString(it.getColumnIndexOrThrow("pkg"))
                    var appName = it.getString(it.getColumnIndexOrThrow("app_name"))

                    if (appName == null) {
                        try {
                            val appInfo = pm.getApplicationInfo(pkg, 0)
                            appName = pm.getApplicationLabel(appInfo).toString()
                        } catch (e: PackageManager.NameNotFoundException) {
                            appName = pkg
                        }
                    }

                    val appIconBytes = it.getBlob(it.getColumnIndexOrThrow("app_icon"))
                    val appIcon = if (appIconBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(appIconBytes, 0, appIconBytes.size)
                        BitmapDrawable(resources, bitmap)
                    } else {
                        requireContext().packageManager.defaultActivityIcon
                    }

                    apps.add(App(pkg, appName, appIcon))
                } while (it.moveToNext())
            }
        }

        allApps = apps
        adapter.updateData(allApps)
        emptyView.isVisible = allApps.isEmpty()
    }

    override fun onSearchQuery(query: String) {
        if (!isAdded) return
        val filteredList = allApps.filter {
            it.appName.contains(query, ignoreCase = true)
        }
        adapter.updateData(filteredList)
        emptyView.isVisible = filteredList.isEmpty()
    }

    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.app_contextual_action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (!isAdded) return false
            if (item.itemId == R.id.action_delete_notifications) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Notifications")
                    .setMessage("Are you sure you want to delete all notifications for the selected apps? This action cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        val selectedPackages = adapter.getSelectedItems().map { it.packageName }
                        NotificationDB.deleteByPackage(requireContext(), selectedPackages)
                        mode.finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
            if (isAdded) {
                loadAppsFromDatabase()
            }
        }
    }
}
