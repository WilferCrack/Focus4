package com.example.focus4.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Recordatorio de Estudio"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Es momento de dedicar tiempo a tus metas académicas."
        val channelId = intent.getStringExtra("EXTRA_CHANNEL_ID") ?: NotificationHelper.CHANNEL_GENERAL

        NotificationHelper.sendTestNotification(
            context = context,
            title = title,
            message = message,
            channelId = channelId
        )
    }
}
