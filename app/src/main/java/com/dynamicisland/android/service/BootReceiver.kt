package com.dynamicisland.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dynamicisland.android.util.AppUtils
import com.dynamicisland.android.util.PreferencesManager

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Boot completed, checking service status")
            
            val prefsManager = PreferencesManager(context)
            
            if (prefsManager.isServiceEnabled && 
                AppUtils.isNotificationListenerEnabled(context) &&
                AppUtils.canDrawOverlays(context)) {
                
                Log.d(TAG, "Starting overlay service on boot")
                
                val serviceIntent = Intent(context, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_START
                }
                
                try {
                    context.startForegroundService(serviceIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start service on boot", e)
                }
            }
        }
    }
}
