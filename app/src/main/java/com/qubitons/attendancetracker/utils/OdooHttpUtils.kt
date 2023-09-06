package com.qubitons.attendancetracker.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.qubitons.attendancetracker.dto.OdooParams
import com.qubitons.attendancetracker.dto.OdooRPCRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URL
import java.util.logging.Logger

class OdooHttpUtils {

    private var SERVER_URL: String = ""
    private var DATABASE: String = ""

    val LOG = Logger.getLogger(OdooHttpUtils::class.java.name)

    constructor(url: String, database: String) {
        this.SERVER_URL = "$url/jsonrpc"
        this.DATABASE = database
    }

    public fun performPostOperation(urlString: String, methodString: String ,paramsAny: Any) : String? {
        return try {
            val client = OkHttpClient.Builder()
                .build()

            val odooRPCRequest = OdooRPCRequest("2.0", methodString, paramsAny, 1)
            val body = ObjectMapper().writeValueAsString(odooRPCRequest).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(URL(urlString))
                .header("Content-Type", "application/json")
                .post(body)
                .build()
            LOG.info("Request sending" + ObjectMapper().writeValueAsString(odooRPCRequest))
            val response = client.newCall(request).execute()
            response.body?.string()
        }
        catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun performOdooCall(serviceString: String, methodString: String, args: MutableList<Any>): String? {
        val odooParams = OdooParams(serviceString, methodString, args)
        return performPostOperation(SERVER_URL, "call", odooParams)
    }

    public fun performOdooCallAndReturnMap(serviceString: String, methodString: String, vararg args: Any): java.util.HashMap<*, *>? {
        val argsList = args.toMutableList()
        argsList.add(0, DATABASE)
        val response = performOdooCall(serviceString, methodString,argsList)
        return ObjectMapper().readValue(response, HashMap::class.java);
    }
}