package com.qubitons.attendancetracker.dto

data class OdooRPCRequest(
    val jsonrpc: String,
    val method: String,
    val params: Any,
    val id: Long
)
