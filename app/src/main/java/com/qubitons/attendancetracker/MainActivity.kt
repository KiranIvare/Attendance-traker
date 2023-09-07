package com.qubitons.attendancetracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.databind.ObjectMapper
import com.qubitons.attendancetracker.databinding.ActivityMainBinding
import com.qubitons.attendancetracker.dto.EmployeeInfo
import com.qubitons.attendancetracker.dto.ServerInfo
import com.qubitons.attendancetracker.ui.home.LocationForegroundService
import com.qubitons.attendancetracker.utils.HttpUtils
import com.qubitons.attendancetracker.utils.OdooHttpUtils
import com.qubitons.attendancetracker.utils.PrefUtils
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {

    val LOG = Logger.getLogger(MainActivity::class.java.name)

    private lateinit var binding: ActivityMainBinding

    lateinit var username : EditText
    lateinit var password: EditText
    lateinit var loginButton: Button

    private var odooHttpUtils : OdooHttpUtils? = null
    private var prefUtils : PrefUtils? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        checkForopenAttendanceActivity()
        super.onCreate(savedInstanceState)
        prefUtils = PrefUtils()
   /*     var serverInfo = prefUtils?.getServerInfo(getSharedPreferences("QUBITONS", MODE_PRIVATE))
        if (serverInfo != null) {
            odooHttpUtils = OdooHttpUtils(serverInfo.serverURL, serverInfo.database)
        }*/

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitbtnclientcode.setOnClickListener(View.OnClickListener {
            val clientCode = binding.clientCode.text.toString()
            val serverInfoTmp = serverInfo(clientCode) !!
            if (serverInfoTmp != null) {
                odooHttpUtils = OdooHttpUtils(serverInfoTmp.serverURL, serverInfoTmp.database)
            }
            prefUtils?.setServerInfo(getSharedPreferences("QUBITONS", MODE_PRIVATE), serverInfoTmp)
        })

        binding.loginButton.setOnClickListener(View.OnClickListener {
            Handler(Looper.getMainLooper()).post {
                    val password = binding.password.text.toString()
                    val response = odooHttpUtils?.performOdooCallAndReturnMap("common", "login", binding.username.text.toString() , binding.password.text.toString())
                    LOG.info("Response got odoo $response")
                    val result = response?.get("result")
                    if (result !is Boolean){
                        val userId = result
                        val search_args = arrayOf(arrayOf("user_id", "=" , userId))
                        val employeeResp = userId?.let { it1 ->
                            odooHttpUtils?.performOdooCallAndReturnMap("object", "execute",
                                it1, password, "hr.employee", "search_read", search_args)
                        }
                        val employeeList = employeeResp?.get("result") as List<Any>
                        val employeeMap = employeeList?.get(0) as Map<*, *>
                        val employeeId = employeeMap?.get("id")
                        //LOG.info("Employee Reseponse got $employee_list")
                        if(employeeId != null) {
                            LOG.info("Employee Informion Emp Id : $employeeId UserId : $userId Password $password")
                            val employeeInfo = EmployeeInfo(userId, employeeId, password, false)
                            saveEmployeeInfoInPrefs(employeeInfo)
                            openAttendanceActivity()
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Login Failed! Employee ID not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
                    }
                }

            ////////////////////////////////////////////////////////////////////////////////////////////////
            val foregroundServiceIntent = Intent(this, LocationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(foregroundServiceIntent)
            } else {
                startService(foregroundServiceIntent)
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////
        })
    }

    private fun serverInfo(clientCode: String) : ServerInfo? {
        val httpUtils = HttpUtils()
        var data : java.util.HashMap<*, *>? = httpUtils.performGetCallAndReturnMap(clientCode)
        LOG.info("Data got for client $data")
        val status = data?.get("status");
        if (status != null) {
            if(status.equals("success")) {
                var response: HashMap<String, Int> = data?.get("message") as HashMap<String, Int>
                return ServerInfo(
                    response["client_url"] as String, response["client_database"] as String)
            }
        }
        return null
    }


    private fun checkForopenAttendanceActivity() {
        val mPrefs = getSharedPreferences("QUBITONS", MODE_PRIVATE)
        val info = mPrefs.getString("QUBTIONS_EMPLOYEE_INFO", "")
        if (info != null) {
            if(info.isBlank()) {
                LOG.info("Employee Info got blank $info")
            } else {
                val infoExtract = ObjectMapper().readValue(info, EmployeeInfo::class.java)
                openAttendanceActivity()
                LOG.info("Employee Info got $info")
            }
        }
    }

    private fun saveEmployeeInfoInPrefs(employeeInfo: EmployeeInfo) {
        val mPrefs = getSharedPreferences("QUBITONS", MODE_PRIVATE)
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        prefsEditor.putString("QUBTIONS_EMPLOYEE_INFO", ObjectMapper().writeValueAsString(employeeInfo))
        prefsEditor.commit()
    }

    private fun openAttendanceActivity() {
        val intent = Intent(this, AttendanceActivity::class.java)
        // start your next activity
        startActivity(intent)
    }
}