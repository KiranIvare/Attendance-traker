package com.qubitons.attendancetracker.utils

import android.content.SharedPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.qubitons.attendancetracker.dto.ServerInfo
import java.util.logging.Logger

class PrefUtils {

    val LOG = Logger.getLogger(PrefUtils::class.java.name)

    public fun getServerInfo(prefs: SharedPreferences): ServerInfo? {
        val info = prefs?.getString("QUBTIONS_SERVER_INFO", "")
        LOG.info("Getting info $info")
        if (info != null) {
            if(info.isNotBlank())
                return ObjectMapper().readValue(info, ServerInfo::class.java)
        }
        return null
    }

    fun setServerInfo(sharedPreferences: SharedPreferences?, serverInfo: ServerInfo) {
        LOG.info("Saving info $serverInfo")
        val prefsEditor: SharedPreferences.Editor? = sharedPreferences?.edit()
        prefsEditor?.putString("QUBTIONS_SERVER_INFO", ObjectMapper().writeValueAsString(serverInfo))
        prefsEditor?.commit()
    }

}