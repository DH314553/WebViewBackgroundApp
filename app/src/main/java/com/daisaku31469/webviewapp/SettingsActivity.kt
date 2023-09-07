package com.daisaku31469.webviewapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            // 初期設定やPreferenceのカスタマイズが必要ならここで行う
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)

            val myPreference = findPreference<EditTextPreference>("signature")
            val myListPreference = findPreference<ListPreference>("reply")
            myPreference?.summary = sharedPreferences.getString("signature", "Default Value")
            myListPreference?.value = sharedPreferences.getString("reply", "Default Value")
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // Preferenceが変更されたときの処理をここで行う
            val preference = findPreference<Preference>(key!!)
            if (preference is EditTextPreference) {
                val value = sharedPreferences?.getString(key, "Default Value")
                preference.summary = value
            }
            if (preference is ListPreference) {
                val value = sharedPreferences?.getString(key, "Default Value")
                preference.value = value
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}