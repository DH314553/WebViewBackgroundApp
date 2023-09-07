package com.daisaku31469.webviewapp.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.daisaku31469.webviewapp.MainActivity

class WebViewBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.example.SHOW_WEBVIEW") {
            // WebViewを表示するActivityを起動
            val newIntent = Intent(context, MainActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(newIntent)
        }
    }
}
