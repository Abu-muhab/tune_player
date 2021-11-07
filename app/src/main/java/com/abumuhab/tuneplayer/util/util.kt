package com.abumuhab.tuneplayer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat

fun createNotificationChannel(context: Context, name: String, description: String) {
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(name, name, importance).apply {
        this.description = description
    }
    val notificationManager: NotificationManager =
        ContextCompat.getSystemService(
            context.applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun <T> findItemPositionInList(list: List<T>, condition: (T) -> Boolean): Int {
    for ((position, element) in list.withIndex()) {
        if (condition(element)) {
            return position
        }
    }
    return -1
}