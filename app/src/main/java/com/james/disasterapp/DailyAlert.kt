package com.james.disasterapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import java.util.*
import kotlin.collections.ArrayList

class DailyAlert : BroadcastReceiver() {
   var floodNotif: Int = 192
    override fun onReceive(context: Context, intent: Intent) {
        executeThread {
            val mRepository: Repository = Repository()
            showNotification(context, floodNotif)
            mRepository.getNearestFlood().observeForever() {
                if (it != null) {
                    when (it) {
                        is ResultCustom.Loading -> true
                        is ResultCustom.Success -> {
                            if (it.data!!.isNotEmpty()) {
                                val listFlood = ArrayList<Int>()
                                it.data.forEach { flood ->
                                    listFlood.add(flood?.properties?.reportData?.floodDepth!!)
                                }
                                floodNotif = listFlood.max()
                            }
                        }
                        is ResultCustom.Error -> true
                    }
                }

            }
        }
    }

    fun setDailyReminder(context: Context) {
        val managerAlarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentToDaily = Intent(context, DailyAlert::class.java)
        val dateCalendar = Calendar.getInstance()
        dateCalendar.timeInMillis = System.currentTimeMillis()
        dateCalendar.set(Calendar.HOUR_OF_DAY, 6)
        dateCalendar.set(Calendar.MINUTE, 0)
        dateCalendar.set(Calendar.SECOND, 0)

        val intentNotifPending = PendingIntent.getBroadcast(
            context,
            ID_REPEATING,
            intentToDaily,
            PendingIntent.FLAG_IMMUTABLE
        )
        managerAlarm.setRepeating(
            AlarmManager.RTC_WAKEUP,
            dateCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intentNotifPending
        )
        Toast.makeText(context, "Flood Depth alert at 06.00 a.m", Toast.LENGTH_SHORT).show()
    }

    fun cancelAlarm(context: Context) {
        val managerAlarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentToDaily = Intent(context, DailyAlert::class.java)
        val intentPending = PendingIntent.getBroadcast(
            context,
            ID_REPEATING,
            intentToDaily,
            PendingIntent.FLAG_IMMUTABLE
        )
        intentPending.cancel()
        managerAlarm.cancel(intentPending)
        Toast.makeText(context, "Cancel Flood Alert", Toast.LENGTH_SHORT).show()
    }

    private fun showNotification(context: Context, floodDepth: Int) {
        val notificationStyle = NotificationCompat.InboxStyle()
        val idRepeat = ID_REPEATING
        val timeString = context.resources.getString(R.string.notification_message_format)
        val notifsChannelName = NOTIFICATION_CHANNEL_NAME
        val notifsChannelID = NOTIFICATION_CHANNEL_ID
        val notifsId = NOTIFICATION_ID

        notificationStyle.addLine("Banjir tertinggi saat ini yaitu $floodDepth cm")

        val intentToHomeActivity = Intent(context, MainActivity::class.java)
        intentToHomeActivity.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendIntent = TaskStackBuilder.create(context)
            .addParentStack(MainActivity::class.java)
            .addNextIntent(intentToHomeActivity)
            .getPendingIntent(
                idRepeat,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val managerNotifs =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundNotifs = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notifsBuilder = NotificationCompat.Builder(context, notifsChannelID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.round_flood_24)
            .setContentIntent(pendIntent)
            .setContentTitle(context.resources.getString(R.string.today_schedule))
            .setStyle(notificationStyle)
            .setContentText(context.resources.getString(R.string.notification_message_format))
            .setSound(soundNotifs)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notifsChannelID,
                notifsChannelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notifsBuilder.setChannelId(notifsChannelID)
            managerNotifs.createNotificationChannel(notificationChannel)
        }

        managerNotifs.notify(notifsId, notifsBuilder.build())
    }
}