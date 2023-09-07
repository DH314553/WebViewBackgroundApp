package com.daisaku31469.webviewapp.Service

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.WebView

class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

    private lateinit var context: Context
    private lateinit var windowManager: WindowManager

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
                showWebView(context, windowManager)
                Log.d("Swipe", "Left to Right")
            }
        }
        return true
    }

    companion object {
        fun showWebView(context: Context, windowManager: WindowManager) {
            val webView = WebView(context)
            webView.loadUrl("https://www.google.com")

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER

            windowManager.addView(webView, params)
        }
    }
}
