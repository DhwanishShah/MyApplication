package com.pixlelabs.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val recyclerView = findViewById<RecyclerView>(R.id.rvAppSelection)
        val selectAllSwitch = findViewById<SwitchCompat>(R.id.select_all_switch)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val sortedApps = allApps.sortedBy { it.loadLabel(pm).toString().lowercase() }

        val adapter = SettingsAdapter(this, sortedApps)
        recyclerView.adapter = adapter

        selectAllSwitch.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE).edit()
            for (app in sortedApps) {
                prefs.putBoolean(app.packageName, isChecked)
            }
            prefs.apply()
            adapter.notifyDataSetChanged()
        }
    }
}
