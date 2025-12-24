package com.example.ideajar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class DeadlineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteContent = intent.getStringExtra("note_content") ?: "Deadline Reached"
        val noteId = intent.getIntExtra("note_id", 0)
        android.util.Log.d("IdeaJar", "Alarm Received! NoteID: $noteId")

        // Create Channel (High Importance)
        val channelId = "idea_jar_channel_v3"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            val soundUri = android.net.Uri.parse("android.resource://" + context.packageName + "/" + R.raw.confirm_tap)

            val channel = NotificationChannel(
                channelId,
                "Deadlines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent Idea Deadlines"
                enableVibration(true)
                enableLights(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Full Screen Intent (or just open app)
        val fullScreenIntent = Intent(context, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this resource exists, default usually does
            .setContentTitle("IDEA DEADLINE EXPIRED")
            .setContentText(noteContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(Notification.DEFAULT_ALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(noteId, notification)
    }
}
