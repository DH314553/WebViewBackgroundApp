package com.daisaku31469.webviewapp

import MyWorker
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.daisaku31469.webviewapp.Service.MyGestureListener
import com.daisaku31469.webviewapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector
    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        gestureDetector = GestureDetector(this, MyGestureListener())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Web Viewの初期設定
        webView = findViewById<View>(R.id.mainLayout) as WebView
        this.webView.webViewClient = WebViewClient() // WebViewを設定する
        this.webView.settings.javaScriptEnabled = true // JavaScriptを有効にする

        MyGestureListener.showWebView(this, windowManager, this.webView.url.toString())

//        if () {
//            webView.setOnTouchListener(this)
//        } else {
//            MyGestureListener.showWebView(context, windowManager)
//        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) { // 戻るボタンがタップされた時
            if (webView.canGoBack()) { // WebViewがNULLでなく、閲覧履歴があるなら
                webView.goBack() // 一つ前のウェブページを表示する
            }
            true
        } else {
            super.onKeyDown(keyCode, e)
        }
    }

//    override fun onResume() {
//        super.onResume() // バックグラウンドからフォアグランドに戻った時など
//        val url = webView.url // 現在のウェブページを
//        webView.loadUrl(url!!) // 再表示する
//    }


    private inner class ForegroundService : Service() {

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            // Foregroundにする処理（通知の表示など）
            // 通知チャンネルを作成（Android Oreo以上）
            val channel = NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)

            // 通知をクリックしたときのインテント
            val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

            // 通知の設定
            val notification = NotificationCompat.Builder(this, "my_channel_id")
                .setContentTitle("MyService")
                .setContentText("Service is running.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()

            // フォアグラウンドサービスを開始
            startForeground(1, notification)

            // WorkManagerを起動
            val workRequest = OneTimeWorkRequestBuilder<MyWorker>().build()
            WorkManager.getInstance(applicationContext).enqueue(workRequest)

            return START_STICKY
        }

        override fun onBind(intent: Intent?): IBinder? {
            TODO("Not yet implemented")
        }
    }
}