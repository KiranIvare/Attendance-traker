package com.qubitons.attendancetracker.ui.home

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.qubitons.attendancetracker.GeoLocationManager
import com.qubitons.attendancetracker.R
import com.qubitons.attendancetracker.dto.EmployeeInfo
import com.qubitons.attendancetracker.utils.OdooHttpUtils
import java.util.Date
import java.util.logging.Logger


class LocationForegroundService : Service() {
    val LOG = Logger.getLogger(LocationForegroundService::class.java.name)

    private val CHANNEL_ID = "LocationForegroundService"
    private val NOTIFICATION_ID = 123
    private var lastRequestTime = Date()
    private var odooHttpUtils : OdooHttpUtils = OdooHttpUtils()

    private var employeeInfo : EmployeeInfo? = null

    var prefs: SharedPreferences? = null

    private lateinit var locationManager: GeoLocationManager
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {


                // Calculate time difference
                // in milliseconds

                // Calculate time difference
                // in milliseconds
                val currentDate = Date()
                val differenceInTime: Long = currentDate.time - lastRequestTime.time
                val differenceInSeconds = ((differenceInTime / 1000) % 60)
                if(differenceInSeconds > 30) {
                    lastRequestTime = currentDate
                    LOG.info("longitude: ${location.longitude} latitude: ${location.latitude}")
                    //Send event to backend for tracking employee

                    employeeInfo?.let {
                        val updateData = HashMap<String, Any>()
                        updateData["current_longitude"] = location.longitude
                        updateData["current_latitude"] = location.latitude
                        val response = odooHttpUtils.performOdooCallAndReturnMap("object", "execute",
                            it.userId, it.password, "hr.employee", "write", arrayOf(it.employeeId), updateData)
                        LOG.info("Response got from odoo server : $response")
                    }
                }

            }

        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = GeoLocationManager(this)
        prefs = getSharedPreferences("QUBITONS", MODE_PRIVATE)
        employeeInfo = getEmployeeInfo()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sender = intent?.getStringExtra("sender")
        if(sender.equals("fragment")) {
            startForeground(NOTIFICATION_ID, createNotification())
            locationManager.startLocationTracking(locationCallback)
            setTrackingTrueInPref()
            LOG.info("Location service has been started")
        } else {
            LOG.info("Sender is not fragment so not starting service")
        }
        // No need to start the service immediately upon fragment load
        // The service will be started when the "Check In" button is clicked
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationTracking(locationCallback)
        setTrackingFalseInPref()
        LOG.info("Location service has been stopped")
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Tracking your location...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setTrackingTrueInPref() {
        val employeeInfo = getEmployeeInfo();
        employeeInfo?.tracking = true
        if (employeeInfo != null) {
            saveEmployeeInfoInPrefs(employeeInfo)
        }
    }

    private fun setTrackingFalseInPref() {
        val employeeInfo = getEmployeeInfo();
        employeeInfo?.tracking = false
        if (employeeInfo != null) {
            saveEmployeeInfoInPrefs(employeeInfo)
        }
    }

    private fun getEmployeeInfo(): EmployeeInfo? {
        LOG.info("Prefs $prefs")
        val info = prefs?.getString("QUBTIONS_EMPLOYEE_INFO", "")
        return ObjectMapper().readValue(info, EmployeeInfo::class.java)
    }

    private fun saveEmployeeInfoInPrefs(employeeInfo: EmployeeInfo) {
        val prefsEditor: SharedPreferences.Editor? = prefs?.edit()
        prefsEditor?.putString("QUBTIONS_EMPLOYEE_INFO", ObjectMapper().writeValueAsString(employeeInfo))
        prefsEditor?.commit()
    }
}

