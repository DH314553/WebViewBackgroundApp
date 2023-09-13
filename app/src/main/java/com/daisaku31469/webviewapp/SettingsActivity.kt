package com.daisaku31469.webviewapp

import MyWorker
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.preference.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SettingsActivity : AppCompatActivity() {

//    private lateinit var webView: WebView

    @RequiresApi(Build.VERSION_CODES.S)
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
        setResult(0, intent)
        MyWorker.listPreference.findPreference<ListPreference>("reply")?.setOnPreferenceClickListener {
            // ここにクリック時の処理を書く
            // webviewの表示(バックグラウンド)
            // チャネルの作成（APIレベル26以上の場合必須）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val channel = NotificationChannel(
                    "my_channel",
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.createNotificationChannel(channel)
            }

            // 通知の作成と表示
            val builder = NotificationCompat.Builder(applicationContext, "my_channel")
                .setSmallIcon(R.drawable.baseline_message_24)  // このアイコンはあなたがプロジェクトに追加する必要があります
                .setContentTitle("My Notification")
                .setContentText("Hello World!")

            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(1, builder.build())
            // 条件が満たされたらブロードキャストを送信
            val intent = Intent("com.example.SHOW_WEBVIEW")
            applicationContext.sendBroadcast(intent)
            // WorkManagerを起動
            if (value == "ON") {
                MyWorker.workManager.enqueue(MyWorker.workRequest)
            } else {
                MyWorker.workManager.cancelAllWork()
            }
            true // 何らかの処理を行った後にtrueを返すことで、デフォルトの動作（設定画面が閉じるなど）を無効にする
        }
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
//        private lateinit var windowManager: WindowManager
//        private lateinit var context: Activity
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            // 初期設定やPreferenceのカスタマイズが必要ならここで行う
//            windowManager.removeView(webView.findViewById(R.id.mainLayout))
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // Preferenceが変更されたときの処理をここで行う
            val editPreference = findPreference<EditTextPreference>(key!!)
            val listPreference = findPreference<Preference>(key)
            val intent = Intent(this.requireActivity(), MainActivity::class.java)
            editPreference?.summaryProvider = null // 既存のSummaryProviderを削除
            listPreference?.summaryProvider = null
            if (editPreference is EditTextPreference) {
                value = sharedPreferences?.getString(key, "Default Value").toString()
                editPreference.summary = value
                // Shared Preferencesに保存
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.requireActivity())
                val editor = sharedPref.edit()
                intent.putExtra("url", value)
                editor.putString("webview_url", value)
                editor.apply()
                startActivityForResult(intent, 0)
            }
            if (listPreference is ListPreference) {
                val value = sharedPreferences?.getString(key, "Default Value")
                listPreference.summary = value
                if (value.equals("ON")) {
                    // WorkManagerを起動
                    WorkManager.getInstance(this.requireActivity()).enqueue(workRequest)
                } else {

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

    companion object {
        var value = ""
        val workRequest = OneTimeWorkRequestBuilder<MyWorker>().build()
        val workManager = WorkManager.getInstance(Activity())
    }
}