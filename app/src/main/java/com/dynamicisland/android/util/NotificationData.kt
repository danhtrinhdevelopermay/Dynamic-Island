package com.dynamicisland.android.util

import android.app.Notification
import android.graphics.drawable.Drawable
import android.service.notification.StatusBarNotification

data class NotificationData(
    val id: Int,
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val title: String?,
    val content: String?,
    val bigText: String?,
    val timestamp: Long,
    val actions: List<NotificationAction>,
    val originalNotification: Notification?,
    val statusBarNotification: StatusBarNotification?
) {
    companion object {
        fun fromStatusBarNotification(
            sbn: StatusBarNotification,
            appName: String,
            appIcon: Drawable?
        ): NotificationData {
            val notification = sbn.notification
            val extras = notification.extras
            
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            
            val actions = notification.actions?.map { action ->
                NotificationAction(
                    title = action.title?.toString() ?: "",
                    actionIntent = action.actionIntent
                )
            } ?: emptyList()
            
            return NotificationData(
                id = sbn.id,
                packageName = sbn.packageName,
                appName = appName,
                appIcon = appIcon,
                title = title,
                content = text,
                bigText = bigText,
                timestamp = sbn.postTime,
                actions = actions,
                originalNotification = notification,
                statusBarNotification = sbn
            )
        }
    }
}

data class NotificationAction(
    val title: String,
    val actionIntent: android.app.PendingIntent?
)
