package mn.application.mobilechargelocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class StartAppOnBoot : BroadcastReceiver() {
   override fun onReceive(context: Context?, intent: Intent?) {
      if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
         val i = Intent(context, MainActivity::class.java)
         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
         context?.startActivity(i)
      }
   }
}
