package com.qubitons.attendancetracker.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ServerInfo @JsonCreator constructor(
    @JsonProperty("serverURL")  val serverURL: String,
    @JsonProperty("database") var database: String,
)
