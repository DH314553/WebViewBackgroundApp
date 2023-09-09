package com.daisaku31469.webviewapp

import MyWorker
import android.app.Activity
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

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
        private lateinit var windowManager: WindowManager
        private lateinit var context: Activity
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            // 初期設定やPreferenceのカスタマイズが必要ならここで行う
//            windowManager.removeView(webView.findViewById(R.id.mainLayout))
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // Preferenceが変更されたときの処理をここで行う
            val preference = findPreference<Preference>(key!!)
            if (preference is EditTextPreference) {
                val value = sharedPreferences?.getString(key, "Default Value")
//                preference.summary = value
                MainActivity().showWebView(this.requireActivity(), windowManager, value!!)
            }
            if (preference is ListPreference) {
                val value = sharedPreferences?.getString(key, "Default Value")
                if (value.equals("ON")) {
                    // WorkManagerを起動
                    val workRequest = OneTimeWorkRequestBuilder<MyWorker>().build()
                    WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
                } else {
                    val workManager = WorkManager.getInstance(context.applicationContext)
                    workManager.cancelAllWork()
                }
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