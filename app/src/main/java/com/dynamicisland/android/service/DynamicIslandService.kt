package com.dynamicisland.android.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dynamicisland.android.util.AppUtils
import com.dynamicisland.android.util.NotificationData
import com.dynamicisland.android.util.PreferencesManager

class DynamicIslandService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "DynamicIslandService"
        
        const val ACTION_NOTIFICATION_POSTED = "com.dynamicisland.android.NOTIFICATION_POSTED"
        const val ACTION_NOTIFICATION_REMOVED = "com.dynamicisland.android.NOTIFICATION_REMOVED"
        const val EXTRA_NOTIFICATION_DATA = "notification_data"
        
        private val EXCLUDED_PACKAGES = setOf(
            "com.android.systemui",
            "com.android.vending",
            "android",
            "com.google.android.gms"
        )
    }
    
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DynamicIslandService created")
        prefsManager = PreferencesManager(this)
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        
        if (prefsManager.isServiceEnabled) {
            startOverlayService()
        }
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        stopOverlayService()
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        
        if (!shouldProcessNotification(sbn)) {
            return
        }
        
        Log.d(TAG, "Notification posted from: ${sbn.packageName}")
        
        val appName = AppUtils.getAppName(this, sbn.packageName)
        val appIcon = AppUtils.getAppIcon(this, sbn.packageName)
        
        val notificationData = NotificationData.fromStatusBarNotification(
            sbn = sbn,
            appName = appName,
            appIcon = appIcon
        )
        
        sendNotificationToOverlay(notificationData)
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        Log.d(TAG, "Notification removed from: ${sbn.packageName}")
        
        val intent = Intent(ACTION_NOTIFICATION_REMOVED).apply {
            setPackage(packageName)
            putExtra("notification_id", sbn.id)
            putExtra("package_name", sbn.packageName)
        }
        sendBroadcast(intent)
    }
    
    private fun shouldProcessNotification(sbn: StatusBarNotification): Boolean {
        if (!prefsManager.isServiceEnabled) {
            return false
        }
        
        if (sbn.packageName in EXCLUDED_PACKAGES) {
            return false
        }
        
        if (prefsManager.isAppExcluded(sbn.packageName)) {
            return false
        }
        
        if (!sbn.isClearable) {
            return false
        }
        
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)
        
        if (title.isNullOrBlank() && text.isNullOrBlank()) {
            return false
        }
        
        return true
    }
    
    private fun sendNotificationToOverlay(data: NotificationData) {
        if (!AppUtils.canDrawOverlays(this)) {
            Log.w(TAG, "Cannot draw overlays, skipping notification display")
            return
        }
        
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_SHOW_NOTIFICATION
            putExtra(OverlayService.EXTRA_PACKAGE_NAME, data.packageName)
            putExtra(OverlayService.EXTRA_APP_NAME, data.appName)
            putExtra(OverlayService.EXTRA_TITLE, data.title)
            putExtra(OverlayService.EXTRA_CONTENT, data.content ?: data.bigText)
            putExtra(OverlayService.EXTRA_TIMESTAMP, data.timestamp)
            
            val actionTitles = data.actions.map { it.title }.toTypedArray()
            putExtra(OverlayService.EXTRA_ACTION_TITLES, actionTitles)
        }
        
        try {
            startForegroundService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start overlay service", e)
        }
    }
    
    private fun startOverlayService() {
        if (!AppUtils.canDrawOverlays(this)) {
            Log.w(TAG, "Cannot draw overlays, not starting overlay service")
            return
        }
        
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START
        }
        try {
            startForegroundService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start overlay service", e)
        }
    }
    
    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_STOP
        }
        try {
            startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop overlay service", e)
        }
    }
}
