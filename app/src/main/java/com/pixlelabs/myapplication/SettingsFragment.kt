package com.pixlelabs.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val darkModeOption = view.findViewById<TextView>(R.id.dark_mode_option)
        val selectAppsOption = view.findViewById<TextView>(R.id.select_apps_option)

        darkModeOption.setOnClickListener { showDarkModeDialog() }
        selectAppsOption.setOnClickListener { openAppSelectionScreen() }
    }

    private fun showDarkModeDialog() {
        val options = arrayOf("Light", "Dark", "System Default")
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val currentSelection = when (currentNightMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Dark Mode")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                val newNightMode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(newNightMode)
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSelectionScreen() {
        val intent = Intent(requireContext(), AppSelectionActivity::class.java)
        startActivity(intent)
    }
}
