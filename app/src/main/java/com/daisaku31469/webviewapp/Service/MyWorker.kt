import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.ListPreference
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.daisaku31469.webviewapp.MainActivity
import com.daisaku31469.webviewapp.R
import com.daisaku31469.webviewapp.SettingsActivity

class MyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private lateinit var context: Context
    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView
    private val listPreference: SettingsActivity.SettingsFragment = SettingsActivity.SettingsFragment()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun doWork(): Result {
        // WorkManagerのインスタンスを取得
        val workManager = WorkManager.getInstance(context)
        listPreference.findPreference<ListPreference>("reply")?.setOnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean) {
                //webviewの表示(バックグラウンド)
                MainActivity().showWebView(context as Activity, windowManager, webView.url.toString())
                // チャネルの作成（APIレベル26以上の場合必須）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val channel = NotificationChannel(
                        "my_channel",
                        "My Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    val manager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    manager?.createNotificationChannel(channel)
                }

                // 通知の作成と表示
                val builder = NotificationCompat.Builder(applicationContext, "my_channel")
                    .setSmallIcon(R.drawable.baseline_message_24)  // このアイコンはあなたがプロジェクトに追加する必要があります
                    .setContentTitle("My Notification")
                    .setContentText("Hello World!")

                val manager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.notify(1, builder.build())
                // 条件が満たされたらブロードキャストを送信
                val intent = Intent("com.example.SHOW_WEBVIEW")
                applicationContext.sendBroadcast(intent)
                // WorkManagerを起動
                val workRequest = OneTimeWorkRequestBuilder<MyWorker>().build()
                workManager.enqueue(workRequest)
            } else {
                // WorkManagerをキャンセル
                workManager.cancelAllWork()
            }
            true
        }
        return Result.success()
    }
}
