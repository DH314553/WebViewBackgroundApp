package com.daisaku31469.webviewapp

import MyWorker
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.daisaku31469.webviewapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector
    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView
//    private lateinit var params: WindowManager.LayoutParams
//    private lateinit var view: CoordinatorLayout
//    private lateinit var event: MotionEvent
    private val requestCode = 1001

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        gestureDetector = GestureDetector(this, MyGestureListener())

        // Web Viewの初期設定
        webView = findViewById<View>(R.id.mainLayout) as WebView

        webView.settings.javaScriptEnabled = true // JavaScriptを有効にする

        this.windowManager = requestOverlayPermission(this, 1001)
        showWebView(this, windowManager, webView.url.toString())

        webView.setOnTouchListener { _, motionEvent ->
            gestureDetector.onGenericMotionEvent(motionEvent)
            showWebView(this, windowManager, webView.url.toString())
            false
        }

        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(MyWorker.workRequest.id)
            .observe(this) { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        // ジョブが実行中の場合
                        showJobWebView()
                        println("The job is running.")
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        // ジョブが成功した場合
                        // 成功をユーザーに通知する、データを更新するなど
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val channel = NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_DEFAULT)
                            notificationManager.createNotificationChannel(channel)
                        }

                        val notification = NotificationCompat.Builder(this, "channelId")
                            .setContentTitle("ジョブ成功")
                            .setContentText("ジョブが成功しました")
                            .setSmallIcon(R.drawable.baseline_message_24)
                            .build()

                        notificationManager.notify(1, notification)
                        println("The job succeeded.")
                    }
                    WorkInfo.State.FAILED -> {
                        // ジョブが失敗した場合
                        try {
                            showJobWebView()
                        } catch (e: Exception) {
                            e.stackTrace
                            println("The job failed.")
                        }
                        // エラーメッセージを表示する、リトライするなど
                    }
                    WorkInfo.State.CANCELLED -> {
                        // ジョブがキャンセルされた場合
                        // キャンセル状態をユーザーに通知するなど
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        // Android Oreo以上のバージョンでの通知チャンネルの設定
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val channel = NotificationChannel("cancelChannelId", "CancelChannelName", NotificationManager.IMPORTANCE_DEFAULT)
                            notificationManager.createNotificationChannel(channel)
                        }

                        // 通知を構築
                        val notification = NotificationCompat.Builder(this, "cancelChannelId")
                            .setContentTitle("ジョブキャンセル")
                            .setContentText("ジョブがキャンセルされました")
                            .setSmallIcon(R.drawable.baseline_message_24)
                            .build()

                        // 通知を表示
                        notificationManager.notify(2, notification)
                        println("The jobcannceled.")
                    }
//                    WorkInfo.State.ENQUEUED -> {
//                        // ジョブがキューに追加された（まだ実行されていない）場合
//                        // 特に何もしないか、状態をロギングするなど
//                        println("The job is enqueued.")
//                    }
//                    WorkInfo.State.BLOCKED -> {
//                        // ジョブが他のジョブによってブロックされている場合
//                        // 特に何もしないか、状態をロギングするなど
//                        println("The job is blocked.")
//                    }
                    else -> {
                        // その他の状態
                        println("The job is in an unknown state.")
                    }
                }

            }
//        if () {
//            webView.setOnTouchListener(this)
//        } else {
//            MyGestureListener.showWebView(context, windowManager)
//        }
    }

    fun showJobWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // ローディングが始まった時にProgressBarを表示
                webView.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // ローディングが終わったらProgressBarを非表示にする
                webView.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "InflateParams", "ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    fun showWebView(context: Activity, windowManager: WindowManager, url: String?) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.activity_main, null) as CoordinatorLayout
        val webView: WebView = view.findViewById(R.id.mainLayout) ?: return  // IDを正確に指定する
        val defaultUrl = "https://google.com"
        val urlToLoad = url?.takeIf { it.isEmpty() } ?: defaultUrl

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(urlToLoad)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER


        if (webView.parent == null || webView.parent != windowManager) {
            // webViewは既に初期化されていると仮定
            (webView.parent as? ViewGroup)?.removeView(webView)  // 既存の親からWebViewを削除
            windowManager.addView(webView, params)  // WindowManagerにWebViewを追加
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f

            webView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 初期位置の記録
                        initialX = params.x
                        initialY = params.y

                        // タッチ開始時の座標を記録
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 現在のタッチ位置から初期タッチ位置を引いて、動きを計算
                        val offsetX = event.rawX - initialTouchX
                        val offsetY = event.rawY - initialTouchY

                        // 初期位置に動きを加えて、新しい位置を設定
                        params.x = initialX + offsetX.toInt()
                        params.y = initialY + offsetY.toInt()

                        // 位置情報を更新
                        windowManager.updateViewLayout(webView, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        // 移動終了、必要であればここで何か処理
                        true
                    }
                    else -> {
                        // それ以外のアクションは無視
                        false
                    }
                }
            }
        } else {
            windowManager.removeView(webView)  // WindowManagerからWebViewを削除

            // 元の親ビューにWebViewを追加
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
            if (ActivityCompat.checkSelfPermission(Activity(), Settings.ACTION_MANAGE_OVERLAY_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Activity(), arrayOf(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), resultCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // ユーザーが許可した場合の処理
            } else {
                // ユーザーが許可しなかった場合の処理
                Toast.makeText(Activity(), "失敗しました", requestCode).show()
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


    inner class ForegroundService : Service() {

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
                if (deltaX > 100) {
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

        // ユーザーがタッチダウンしたときに呼ばれる
        override fun onDown(e: MotionEvent): Boolean {
            Log.d("Gesture", "onDown")
            return true // trueを返すことで、この後の動きも受け取る
        }

        // タッチダウンしてからもうすぐで何かが起こるときに呼ばれる
        override fun onShowPress(e: MotionEvent) {
            Log.d("Gesture", "onShowPress")
        }

        // 単純なタップ（押してすぐに離れた）時に呼ばれる
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.d("Gesture", "onSingleTapUp")
            return false
        }

        // ユーザーがタッチダウンしてから動かしているときに呼ばれる
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            Log.d("Gesture", "onScroll")
            return true
        }

        // 長押しを検出したときに呼ばれる
        override fun onLongPress(e: MotionEvent) {
            Log.d("Gesture", "onLongPress")
        }
    }
}
