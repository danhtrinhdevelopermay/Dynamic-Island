package com.dynamicisland.android.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dynamicisland.android.R
import com.dynamicisland.android.databinding.ActivityMainBinding
import com.dynamicisland.android.service.OverlayService
import com.dynamicisland.android.util.AppUtils
import com.dynamicisland.android.util.PreferencesManager

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TEST_CHANNEL_ID = "test_notification_channel"
        private const val PERMISSION_REQUEST_CODE = 1001
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionStatus()
    }
    
    private val notificationListenerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionStatus()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefsManager = PreferencesManager(this)
        
        setupClickListeners()
        createTestNotificationChannel()
    }
    
    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateServiceStatus()
    }
    
    private fun setupClickListeners() {
        binding.notificationPermissionCard.setOnClickListener {
            requestNotificationListenerPermission()
        }
        
        binding.overlayPermissionCard.setOnClickListener {
            requestOverlayPermission()
        }
        
        binding.toggleButton.setOnClickListener {
            toggleService()
        }
        
        binding.testButton.setOnClickListener {
            sendTestNotification()
        }
    }
    
    private fun updatePermissionStatus() {
        val isNotificationEnabled = AppUtils.isNotificationListenerEnabled(this)
        val isOverlayEnabled = AppUtils.canDrawOverlays(this)
        
        if (isNotificationEnabled) {
            binding.notificationStatus.text = "Đã cấp quyền"
            binding.notificationStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active))
            binding.notificationCheckmark.visibility = View.VISIBLE
        } else {
            binding.notificationStatus.text = "Chưa cấp quyền - Nhấn để cấp"
            binding.notificationStatus.setTextColor(ContextCompat.getColor(this, R.color.status_inactive))
            binding.notificationCheckmark.visibility = View.GONE
        }
        
        if (isOverlayEnabled) {
            binding.overlayStatus.text = "Đã cấp quyền"
            binding.overlayStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active))
            binding.overlayCheckmark.visibility = View.VISIBLE
        } else {
            binding.overlayStatus.text = "Chưa cấp quyền - Nhấn để cấp"
            binding.overlayStatus.setTextColor(ContextCompat.getColor(this, R.color.status_inactive))
            binding.overlayCheckmark.visibility = View.GONE
        }
        
        binding.toggleButton.isEnabled = isNotificationEnabled && isOverlayEnabled
        
        if (!isNotificationEnabled || !isOverlayEnabled) {
            binding.statusText.text = "Vui lòng cấp đủ quyền để sử dụng"
        }
    }
    
    private fun updateServiceStatus() {
        val isEnabled = prefsManager.isServiceEnabled
        
        if (isEnabled) {
            binding.toggleButton.text = getString(R.string.disable_service)
            binding.statusText.text = getString(R.string.service_running)
            binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.status_active))
        } else {
            binding.toggleButton.text = getString(R.string.enable_service)
            binding.statusText.text = getString(R.string.tap_to_enable)
            binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.dynamic_island_text_secondary))
        }
    }
    
    private fun requestNotificationListenerPermission() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notification_permission_title)
            .setMessage(R.string.notification_permission_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                notificationListenerLauncher.launch(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun requestOverlayPermission() {
        if (!AppUtils.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.overlay_permission_title)
                .setMessage(R.string.overlay_permission_message)
                .setPositiveButton(R.string.grant_permission) { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    overlayPermissionLauncher.launch(intent)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
    
    private fun toggleService() {
        val isNotificationEnabled = AppUtils.isNotificationListenerEnabled(this)
        val isOverlayEnabled = AppUtils.canDrawOverlays(this)
        
        if (!isNotificationEnabled || !isOverlayEnabled) {
            Toast.makeText(this, "Vui lòng cấp đủ quyền trước", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newState = !prefsManager.isServiceEnabled
        prefsManager.isServiceEnabled = newState
        
        val serviceIntent = Intent(this, OverlayService::class.java).apply {
            action = if (newState) OverlayService.ACTION_START else OverlayService.ACTION_STOP
        }
        
        if (newState) {
            startForegroundService(serviceIntent)
            Toast.makeText(this, getString(R.string.service_running), Toast.LENGTH_SHORT).show()
        } else {
            startService(serviceIntent)
            Toast.makeText(this, getString(R.string.service_stopped), Toast.LENGTH_SHORT).show()
        }
        
        updateServiceStatus()
    }
    
    private fun createTestNotificationChannel() {
        val channel = NotificationChannel(
            TEST_CHANNEL_ID,
            "Test Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for test notifications"
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun sendTestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
                return
            }
        }
        
        if (!prefsManager.isServiceEnabled) {
            Toast.makeText(this, "Vui lòng bật Dynamic Island trước", Toast.LENGTH_SHORT).show()
            return
        }
        
        val notification = NotificationCompat.Builder(this, TEST_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Thông báo test")
            .setContentText("Đây là thông báo test cho Dynamic Island!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(this).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
            Toast.makeText(this, "Đã gửi thông báo test!", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Cần quyền gửi thông báo", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendTestNotification()
            } else {
                Toast.makeText(this, "Cần quyền gửi thông báo để test", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
