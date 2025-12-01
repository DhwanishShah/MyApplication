package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppsFragment : Fragment(R.layout.fragment_apps) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadApps()
    }

    private fun loadApps() {
        val cursor = NotificationDB.getAll(requireContext())
        val packagesWithNotifications = mutableSetOf<String>()

        cursor.use {
            while (it.moveToNext()) {
                packagesWithNotifications.add(it.getString(it.getColumnIndexOrThrow("pkg")))
            }
        }

        val pm = requireActivity().packageManager
        val apps = mutableListOf<App>()

        for (packageName in packagesWithNotifications) {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val appIcon = pm.getApplicationIcon(appInfo)
                apps.add(App(packageName, appName, appIcon))
            } catch (e: PackageManager.NameNotFoundException) {
                // App might have been uninstalled
            }
        }

        adapter = AppAdapter(apps) { app ->
            val intent = Intent(requireContext(), AppNotificationsActivity::class.java)
            intent.putExtra("pkg", app.packageName)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }
}
