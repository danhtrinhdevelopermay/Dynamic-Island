package com.dynamicisland.android.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "dynamic_island_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_AUTO_EXPAND = "auto_expand"
        private const val KEY_DISPLAY_DURATION = "display_duration"
        private const val KEY_EXCLUDED_APPS = "excluded_apps"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        
        private const val DEFAULT_DISPLAY_DURATION = 4000L
    }
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_SERVICE_ENABLED, value) }
    
    var isAutoExpand: Boolean
        get() = prefs.getBoolean(KEY_AUTO_EXPAND, true)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_EXPAND, value) }
    
    var displayDuration: Long
        get() = prefs.getLong(KEY_DISPLAY_DURATION, DEFAULT_DISPLAY_DURATION)
        set(value) = prefs.edit { putLong(KEY_DISPLAY_DURATION, value) }
    
    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_VIBRATION_ENABLED, value) }
    
    var excludedApps: Set<String>
        get() = prefs.getStringSet(KEY_EXCLUDED_APPS, emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet(KEY_EXCLUDED_APPS, value) }
    
    fun isAppExcluded(packageName: String): Boolean {
        return excludedApps.contains(packageName)
    }
    
    fun addExcludedApp(packageName: String) {
        excludedApps = excludedApps + packageName
    }
    
    fun removeExcludedApp(packageName: String) {
        excludedApps = excludedApps - packageName
    }
}
