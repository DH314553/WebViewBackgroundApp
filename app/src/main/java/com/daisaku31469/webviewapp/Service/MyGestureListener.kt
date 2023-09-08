package com.daisaku31469.webviewapp.Service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.daisaku31469.webviewapp.MainActivity
import com.daisaku31469.webviewapp.R


class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

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
                showWebView(context = MainActivity(), windowManager, webView.url.toString())
                Log.d("Swipe", "Left to Right")
            } else {
                Log.d("Swipe", "other Swipe")
            }
        }
        return true
    }

    companion object {
        private lateinit var windowManager: WindowManager
        @SuppressLint("StaticFieldLeak", "SetJavaScriptEnabled", "InflateParams")

        @RequiresApi(Build.VERSION_CODES.S)
        fun showWebView(context: Activity, windowManager: WindowManager, url: String) {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.activity_main, null)

            val defaultUrl = "https://google.com"
            val urlReplace = if (url == "") defaultUrl else url

            val webView: WebView = view.findViewById(R.id.mainLayout) // IDを正確に指定してください
            webView.webViewClient = WebViewClient()
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(defaultUrl)

            this.windowManager = requestOverlayPermission(context, 1001)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER
            // webViewは既に初期化されていると仮定
            if (webView.parent != null) {
                (webView.parent as ViewGroup).removeView(webView) //← 既存の親から削除
            }
            windowManager.addView(webView, params)
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
    }
}
