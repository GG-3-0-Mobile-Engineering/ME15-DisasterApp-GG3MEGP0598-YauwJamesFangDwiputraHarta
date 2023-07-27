package com.james.disasterapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.james.disasterapp.model.ReportData

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class DailyReminder : BroadcastReceiver() {
     var floodNotif : Int = 100

    init {
        val mRepository: Repository = Repository()
        mRepository.getNewrestFlood().observeForever(){
            if(it != null){
                when (it){
                    is ResultCustom.Loading -> true
                    is ResultCustom.Success -> {
                        if (it.data!!.isNotEmpty()){
                            val listFlood = ArrayList<Int>()


                            it.data.forEach {flood ->
                                listFlood.add(flood?.properties?.reportData?.floodDepth!!)
                            }

                            floodNotif = listFlood.max()

                        } else {
                            floodNotif = 120
                        }
                    }
                    is ResultCustom.Error ->  Log.d("log 123", "${floodNotif}")
                }
            }

        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        executeThread {

            showNotification(context, floodNotif)


//            floodNotif?.let {

//            }
        }
    }

    //TODO 12 : Implement daily reminder for every 06.00 a.m using AlarmManager
    fun setDailyReminder(context: Context) {
        val managerAlarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentToDaily = Intent(context, DailyReminder::class.java)
        val dateCalendar = Calendar.getInstance()
        dateCalendar.timeInMillis = System.currentTimeMillis()
        dateCalendar.set(Calendar.HOUR_OF_DAY, 6)
        dateCalendar.set(Calendar.MINUTE, 0)
        dateCalendar.set(Calendar.SECOND, 0)

        val intentNotifPending = PendingIntent.getBroadcast(context, ID_REPEATING, intentToDaily, PendingIntent.FLAG_IMMUTABLE)
        managerAlarm.setRepeating(
            AlarmManager.RTC_WAKEUP,
            dateCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intentNotifPending
        )
        Toast.makeText(context, "Daily Reminder at 06.00 a.m", Toast.LENGTH_SHORT).show()
    }

    fun cancelAlarm(context: Context) {
        val managerAlarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentToDaily = Intent(context, DailyReminder::class.java)
        val intentPending = PendingIntent.getBroadcast(context, ID_REPEATING, intentToDaily, PendingIntent.FLAG_IMMUTABLE)
        intentPending.cancel()
        managerAlarm.cancel(intentPending)
        Toast.makeText(context, "Cancel Daily Reminder", Toast.LENGTH_SHORT).show()
    }

    private fun showNotification(context: Context, floodDepth : Int) {
        //TODO 13 : Show today schedules in inbox style notification & open HomeActivity when notification tapped
        val notificationStyle = NotificationCompat.InboxStyle()
        val idRepeat = ID_REPEATING
        val timeString = context.resources.getString(R.string.notification_message_format)
        val notifsChannelName = NOTIFICATION_CHANNEL_NAME
        val notifsChannelID = NOTIFICATION_CHANNEL_ID
        val notifsId = NOTIFICATION_ID

//        content.forEach {
//            val courseData = String.format(timeString, it.startTime, it.endTime, it.courseName)
            notificationStyle.addLine("Banjir tertinggi saat ini yaitu $floodDepth cm")
//        }

        val intentToHomeActivity = Intent(context, MainActivity::class.java)
        intentToHomeActivity.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendIntent = TaskStackBuilder.create(context)
            .addParentStack(MainActivity::class.java)
            .addNextIntent(intentToHomeActivity)
            .getPendingIntent(idRepeat,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

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