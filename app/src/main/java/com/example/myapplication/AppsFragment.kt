package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsFragment : Fragment(R.layout.fragment_apps), Searchable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private var allApps = listOf<App>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AppAdapter(emptyList()) { app ->
            val intent = Intent(requireContext(), AppNotificationsActivity::class.java)
            intent.putExtra("pkg", app.packageName)
            // **THE FIX**: Pass the correct app name to the next screen.
            intent.putExtra("app_name", app.appName)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadAppsFromDatabase()
    }

    private fun loadAppsFromDatabase() {
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
    }

    override fun onSearchQuery(query: String) {
        val filteredList = allApps.filter {
            it.appName.contains(query, ignoreCase = true)
        }
        adapter.updateData(filteredList)
    }
}
