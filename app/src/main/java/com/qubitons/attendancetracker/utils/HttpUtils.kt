package com.qubitons.attendancetracker.utils

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URL
import java.util.logging.Logger

class HttpUtils {

    val LOG = Logger.getLogger(HttpUtils::class.java.name)


    public fun performGetOperation(clientCode: String) : String? {
        val urlString = "https://www.qubitons.com/clients/$clientCode"
        LOG.info("Sending request for client $urlString")
        return try {
            val client = OkHttpClient.Builder()
                .build()

            val request = Request.Builder()
                .url(URL(urlString))
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()
        }
        catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    public fun performGetCallAndReturnMap(clientCode: String): java.util.HashMap<*, *>? {
        val response = performGetOperation(clientCode)
        return ObjectMapper().readValue(response, HashMap::class.java);
    }
}