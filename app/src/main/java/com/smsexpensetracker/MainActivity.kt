package com.smsexpensetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    
    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 100
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        private val NOTIFICATION_PERMISSIONS = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupBottomNavigation()
        checkAndRequestPermissions()
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(TransactionsFragment())
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> {
                    loadFragment(TransactionsFragment())
                    true
                }
                R.id.nav_reports -> {
                    loadFragment(ReportsFragment())
                    true
                }
                R.id.nav_sms_debug -> {
                    loadFragment(SmsDebugFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    private fun checkAndRequestPermissions() {
        val smsPermissionsNeeded = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        // Check notification permissions for Android 13+ (API 33+)
        val notificationPermissionsNeeded = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            NOTIFICATION_PERMISSIONS.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
        } else {
            emptyList()
        }
        
        // Request SMS permissions first (most critical)
        if (smsPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                smsPermissionsNeeded.toTypedArray(),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else if (notificationPermissionsNeeded.isNotEmpty()) {
            // Request notification permissions if SMS permissions are already granted
            ActivityCompat.requestPermissions(
                this,
                notificationPermissionsNeeded.toTypedArray(),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                val deniedPermissions = permissions.filterIndexed { index, _ ->
                    grantResults[index] != PackageManager.PERMISSION_GRANTED
                }
                
                if (deniedPermissions.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        "❌ SMS permissions are REQUIRED for SMS tracking to work!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "✅ SMS permissions granted! App will now monitor SMS messages.",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // After SMS permissions granted, check for notification permissions
                    checkAndRequestPermissions()
                }
            }
            
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                val deniedPermissions = permissions.filterIndexed { index, _ ->
                    grantResults[index] != PackageManager.PERMISSION_GRANTED
                }
                
                if (deniedPermissions.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        "⚠️ Notifications disabled - you won't see SMS capture alerts",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "✅ Notification permissions granted!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}