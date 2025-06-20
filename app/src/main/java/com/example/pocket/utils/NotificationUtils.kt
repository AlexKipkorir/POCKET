package com.example.pocket.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

fun createBillReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "bill_reminder_channel",
            "Bill Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for due bill reminders"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}