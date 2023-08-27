package com.qubitons.attendancetracker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.navigation.NavigationView
import com.qubitons.attendancetracker.databinding.ActivityAttendanceBinding
import com.qubitons.attendancetracker.dto.EmployeeInfo
import com.qubitons.attendancetracker.ui.home.LocationForegroundService
import java.util.logging.Logger

class AttendanceActivity : AppCompatActivity() {

    val LOG = Logger.getLogger(AttendanceActivity::class.java.name)
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var employeeInfo: EmployeeInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        employeeInfo = checkForEmployeeInfo()!!
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        /*binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun checkForEmployeeInfo(): EmployeeInfo? {
        val mPrefs = getSharedPreferences("QUBITONS", MODE_PRIVATE)
        val info = mPrefs.getString("QUBTIONS_EMPLOYEE_INFO", "")
        if (info != null) {
            if(info.isBlank()) {
                LOG.info("Employee Info got blank $info")
                Toast.makeText(this, "Login Failed! Employee ID not found", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                LOG.info("Employee Info got $info")
                return ObjectMapper().readValue(info, EmployeeInfo::class.java)
            }
        }
        return null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // Handling the click events of the menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Switching on the item id of the menu item
        when (item.itemId) {
            R.id.action_settings -> {
                // Code to be executed when the add button is clicked
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent);
                finish()
                clearEmployeeInfo()
                stopLocationService()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun stopLocationService() {
        // Stop the LocationForegroundService
        val serviceIntent = Intent(applicationContext, LocationForegroundService::class.java)
        applicationContext.stopService(serviceIntent)
    }

    private fun clearEmployeeInfo() {
        val mPrefs = getSharedPreferences("QUBITONS", MODE_PRIVATE)
        mPrefs.edit().clear().commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}