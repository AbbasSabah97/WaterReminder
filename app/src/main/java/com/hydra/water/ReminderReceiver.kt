package com.hydra.water

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_REMINDERS, true)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Hydration reminders", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_drop)
            .setContentTitle("💧 Time to drink water")
            .setContentText("Take a few sips and keep your hydration on track.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
        scheduleNext(context, prefs.getInt(KEY_INTERVAL, 60))
    }

    companion object {
        const val PREFS = "hydra_prefs"
        const val KEY_REMINDERS = "reminders_enabled"
        const val KEY_INTERVAL = "interval_minutes"
        private const val CHANNEL_ID = "hydration"
        private const val NOTIFICATION_ID = 1201

        fun scheduleNext(context: Context, minutes: Int) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(context, 1202, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val triggerAt = System.currentTimeMillis() + minutes.coerceIn(15, 240) * 60_000L
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }

        fun cancel(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(context, 1202, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.cancel(pi)
        }

        fun start(context: Context, minutes: Int) {
            cancel(context)
            scheduleNext(context, minutes)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences(ReminderReceiver.PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(ReminderReceiver.KEY_REMINDERS, true)) {
            ReminderReceiver.start(context, prefs.getInt(ReminderReceiver.KEY_INTERVAL, 60))
        }
    }
}
