package com.dynamicisland.android.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import com.dynamicisland.android.R
import com.dynamicisland.android.ui.MainActivity
import com.dynamicisland.android.util.AppUtils
import com.dynamicisland.android.util.PreferencesManager

class OverlayService : Service() {
    
    companion object {
        private const val TAG = "OverlayService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "dynamic_island_channel"
        
        const val ACTION_START = "com.dynamicisland.android.ACTION_START"
        const val ACTION_STOP = "com.dynamicisland.android.ACTION_STOP"
        const val ACTION_SHOW_NOTIFICATION = "com.dynamicisland.android.ACTION_SHOW_NOTIFICATION"
        const val ACTION_HIDE = "com.dynamicisland.android.ACTION_HIDE"
        
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_ACTION_TITLES = "action_titles"
        
        private const val COLLAPSED_WIDTH_DP = 140
        private const val EXPANDED_WIDTH_DP = 340
        private const val COLLAPSED_HEIGHT_DP = 40
        private const val EXPANDED_HEIGHT_DP = 160
    }
    
    private lateinit var windowManager: WindowManager
    private lateinit var prefsManager: PreferencesManager
    private var overlayView: View? = null
    private var isExpanded = false
    private var isShowing = false
    
    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null
    
    private var blurBackgroundView: View? = null
    
    private val notificationRemovedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DynamicIslandService.ACTION_NOTIFICATION_REMOVED) {
                hideOverlay()
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefsManager = PreferencesManager(this)
        
        createNotificationChannel()
        
        val filter = IntentFilter(DynamicIslandService.ACTION_NOTIFICATION_REMOVED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationRemovedReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationRemovedReceiver, filter)
        }
    }
    
    private var isForegroundStarted = false
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isForegroundStarted) {
            startForegroundServiceNotification()
            isForegroundStarted = true
        }
        
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "OverlayService started")
            }
            ACTION_STOP -> {
                stopSelf()
            }
            ACTION_SHOW_NOTIFICATION -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return START_STICKY
                val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
                val title = intent.getStringExtra(EXTRA_TITLE)
                val content = intent.getStringExtra(EXTRA_CONTENT)
                val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
                val actionTitles = intent.getStringArrayExtra(EXTRA_ACTION_TITLES) ?: emptyArray()
                
                Log.d(TAG, "Showing notification from: $packageName")
                showNotification(packageName, appName, title, content, timestamp, actionTitles.toList())
            }
            ACTION_HIDE -> {
                hideOverlay()
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        isForegroundStarted = false
        hideOverlay()
        try {
            unregisterReceiver(notificationRemovedReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun startForegroundServiceNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_notification_title))
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
    
    private fun showNotification(
        packageName: String,
        appName: String,
        title: String?,
        content: String?,
        timestamp: Long,
        actionTitles: List<String>
    ) {
        if (!AppUtils.canDrawOverlays(this)) {
            Log.e(TAG, "Cannot draw overlays")
            return
        }
        
        hideRunnable?.let { handler.removeCallbacks(it) }
        
        if (overlayView == null) {
            createOverlayView()
        }
        
        updateOverlayContent(packageName, appName, title, content, timestamp, actionTitles)
        
        if (!isShowing) {
            showOverlay()
        }
        
        applyBlurBackground(false)
        
        scheduleHide()
    }
    
    private fun createOverlayView() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.dynamic_island_layout, null)
        
        blurBackgroundView = overlayView?.findViewById(R.id.blurBackground)
        
        val layoutParams = WindowManager.LayoutParams(
            dpToPx(COLLAPSED_WIDTH_DP),
            dpToPx(COLLAPSED_HEIGHT_DP),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dpToPx(12)
        }
        
        setupTouchListener()
        
        try {
            windowManager.addView(overlayView, layoutParams)
            isShowing = true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding overlay view", e)
        }
    }
    
    private fun applyBlurBackground(isExpanded: Boolean) {
        blurBackgroundView?.apply {
            val backgroundRes = if (isExpanded) {
                R.drawable.dynamic_island_blur_expanded
            } else {
                R.drawable.dynamic_island_blur_background
            }
            setBackgroundResource(backgroundRes)
        }
        
        overlayView?.findViewById<CardView>(R.id.dynamicIslandCard)?.apply {
            cardElevation = if (isExpanded) 16f else 12f
        }
    }
    
    private fun setupTouchListener() {
        overlayView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    hideRunnable?.let { handler.removeCallbacks(it) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    toggleExpanded()
                    scheduleHide()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun updateOverlayContent(
        packageName: String,
        appName: String,
        title: String?,
        content: String?,
        timestamp: Long,
        actionTitles: List<String>
    ) {
        overlayView?.apply {
            val appIcon = AppUtils.getAppIcon(context, packageName)
            
            findViewById<ImageView>(R.id.appIcon)?.setImageDrawable(appIcon)
            findViewById<TextView>(R.id.appName)?.text = title ?: appName
            
            findViewById<ImageView>(R.id.expandedAppIcon)?.setImageDrawable(appIcon)
            findViewById<TextView>(R.id.expandedAppName)?.text = appName.uppercase()
            findViewById<TextView>(R.id.notificationTitle)?.text = title ?: appName
            findViewById<TextView>(R.id.notificationContent)?.text = content ?: ""
            findViewById<TextView>(R.id.notificationTime)?.text = AppUtils.formatRelativeTime(timestamp)
            
            val actionContainer = findViewById<LinearLayout>(R.id.actionButtonsContainer)
            val actionButton1 = findViewById<TextView>(R.id.actionButton1)
            val actionButton2 = findViewById<TextView>(R.id.actionButton2)
            
            if (actionTitles.isNotEmpty()) {
                actionContainer?.visibility = View.VISIBLE
                
                if (actionTitles.size >= 1) {
                    actionButton1?.apply {
                        visibility = View.VISIBLE
                        text = actionTitles[0]
                    }
                }
                
                if (actionTitles.size >= 2) {
                    actionButton2?.apply {
                        visibility = View.VISIBLE
                        text = actionTitles[1]
                    }
                }
            } else {
                actionContainer?.visibility = View.GONE
            }
        }
    }
    
    private fun showOverlay() {
        overlayView?.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
        isShowing = true
    }
    
    private fun hideOverlay() {
        hideRunnable?.let { handler.removeCallbacks(it) }
        
        overlayView?.animate()
            ?.alpha(0f)
            ?.scaleX(0.8f)
            ?.scaleY(0.8f)
            ?.setDuration(250)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    removeOverlayView()
                }
            })
            ?.start()
    }
    
    private fun removeOverlayView() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view", e)
            }
        }
        blurBackgroundView = null
        overlayView = null
        isShowing = false
        isExpanded = false
    }
    
    private fun toggleExpanded() {
        if (isExpanded) {
            collapseOverlay()
        } else {
            expandOverlay()
        }
        isExpanded = !isExpanded
    }
    
    private fun expandOverlay() {
        overlayView?.apply {
            findViewById<LinearLayout>(R.id.collapsedView)?.visibility = View.GONE
            findViewById<LinearLayout>(R.id.expandedView)?.visibility = View.VISIBLE
            
            applyBlurBackground(true)
            
            val params = layoutParams as WindowManager.LayoutParams
            
            val widthAnimator = ValueAnimator.ofInt(params.width, dpToPx(EXPANDED_WIDTH_DP))
            val heightAnimator = ValueAnimator.ofInt(params.height, dpToPx(EXPANDED_HEIGHT_DP))
            
            widthAnimator.addUpdateListener { animator ->
                params.width = animator.animatedValue as Int
                try {
                    windowManager.updateViewLayout(this, params)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating view layout", e)
                }
            }
            
            heightAnimator.addUpdateListener { animator ->
                params.height = animator.animatedValue as Int
                try {
                    windowManager.updateViewLayout(this, params)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating view layout", e)
                }
            }
            
            AnimatorSet().apply {
                playTogether(widthAnimator, heightAnimator)
                duration = 350
                interpolator = OvershootInterpolator(0.8f)
                start()
            }
        }
    }
    
    private fun collapseOverlay() {
        overlayView?.apply {
            val params = layoutParams as WindowManager.LayoutParams
            
            applyBlurBackground(false)
            
            val widthAnimator = ValueAnimator.ofInt(params.width, dpToPx(COLLAPSED_WIDTH_DP))
            val heightAnimator = ValueAnimator.ofInt(params.height, dpToPx(COLLAPSED_HEIGHT_DP))
            
            widthAnimator.addUpdateListener { animator ->
                params.width = animator.animatedValue as Int
                try {
                    windowManager.updateViewLayout(this, params)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating view layout", e)
                }
            }
            
            heightAnimator.addUpdateListener { animator ->
                params.height = animator.animatedValue as Int
                try {
                    windowManager.updateViewLayout(this, params)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating view layout", e)
                }
            }
            
            AnimatorSet().apply {
                playTogether(widthAnimator, heightAnimator)
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        findViewById<LinearLayout>(R.id.collapsedView)?.visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.expandedView)?.visibility = View.GONE
                    }
                })
                start()
            }
        }
    }
    
    private fun scheduleHide() {
        hideRunnable = Runnable {
            if (isExpanded) {
                collapseOverlay()
                isExpanded = false
                scheduleHide()
            } else {
                hideOverlay()
            }
        }
        handler.postDelayed(hideRunnable!!, prefsManager.displayDuration)
    }
    
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
