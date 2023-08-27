package com.qubitons.attendancetracker.dto

data class OdooParams(
    val service: String,
    val method: String,
    val args: List<Any>
)
