package com.daisaku31469.webviewapp

import MyWorker
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.daisaku31469.webviewapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector
    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var view: CoordinatorLayout
    private lateinit var event: MotionEvent
    private val requestCode = 1001

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        gestureDetector = GestureDetector(this, MyGestureListener())
        this.windowManager = requestOverlayPermission(this, 1001)

        // Web Viewの初期設定
        webView = findViewById<View>(R.id.mainLayout) as WebView
        webView.webViewClient = WebViewClient() // WebViewを設定する
        webView.settings.javaScriptEnabled = true // JavaScriptを有効にする

        showWebView(this, windowManager, webView.url.toString())

        webView.setOnTouchListener { _, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
            showWebView(this, windowManager, webView.url.toString())
            true
        }
//        if () {
//            webView.setOnTouchListener(this)
//        } else {
//            MyGestureListener.showWebView(context, windowManager)
//        }
    }

    @SuppressLint("SetJavaScriptEnabled", "InflateParams")
    @RequiresApi(Build.VERSION_CODES.S)
    fun showWebView(context: Activity, windowManager: WindowManager, url: String) {

        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.activity_main, null) as CoordinatorLayout
        val webView: WebView = view.findViewById(R.id.mainLayout) // IDを正確に指定する
        val defaultUrl = "https://google.com"
        val urlReplace = if (url == "") defaultUrl else url


        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(defaultUrl)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER


        if (webView.parent == null || webView.parent != windowManager) {
            // webViewは既に初期化されていると仮定
            if (webView.parent != null) {
                (webView.parent as ViewGroup).removeView(webView)  // 既存の親からWebViewを削除
            }


            windowManager.addView(webView, params)  // WindowManagerにWebViewを追加
        } else {
            windowManager.removeView(webView)  // WindowManagerからWebViewを削除

            // 元の親ビューにWebViewを追加（ここではLinearLayoutを例としています）
            val linearLayout = view.findViewById<CoordinatorLayout>(R.id.layout)
            linearLayout?.addView(webView)
        }
    }

    fun requestOverlayPermission(context: Activity, OVERLAY_PERMISSION_REQ_CODE: Int): WindowManager {
        val packageName = context.opPackageName
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            context.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }
        return context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onGenericMotionEvent(event!!)
    }

    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.xml.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        when (item.itemId) {
            R.id.settings -> {
                startActivityIfNeeded(intent, requestCode)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resultCode) {
            if (resultCode == Activity.RESULT_OK) {
                showWebView(this, windowManager, webView.url.toString())
            }
        }
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

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        private lateinit var windowManager: WindowManager
        private lateinit var webView: WebView

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // スワイプ検出（例：左から右へのスワイプ）
            val deltaX = e2.x - e1!!.x
            val deltaY = e2.y - e1.y



            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (deltaX > 0) {
                    // ここで左から右へのスワイプが検出されました
                    // 何かの処理を行います
                    // WebView表示処理（System Alert Windowが必要）
                    showWebView(MainActivity(), windowManager, webView.url.toString())
                    Log.d("Swipe", "Left to Right")
                } else {
                    Log.d("Swipe", "other Swipe")
                }
            }
            return true
        }

        // 他の GestureDetector.OnGestureListener のメソッドも実装
        override fun onDown(e: MotionEvent): Boolean = false
        override fun onShowPress(e: MotionEvent) {}
        override fun onSingleTapUp(e: MotionEvent): Boolean = false
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean = false

        override fun onLongPress(e: MotionEvent) {
        }
    }
}
