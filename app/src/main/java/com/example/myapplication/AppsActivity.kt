package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class AppsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val appList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        listView = findViewById(R.id.listApps)
        loadApps()

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, AppNotificationsActivity::class.java)
            intent.putExtra("pkg", appList[position])
            startActivity(intent)
        }
    }

    private fun loadApps() {
        val cursor = NotificationDB.getAll(this)
        val set = HashSet<String>()

        cursor.use {
            while (it.moveToNext()) {
                set.add(it.getString(it.getColumnIndexOrThrow("pkg")))
            }
        }
        appList.addAll(set.sorted())
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appList)
    }
}
