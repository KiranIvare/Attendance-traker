package com.qubitons.attendancetracker.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class EmployeeInfo @JsonCreator constructor(
    @JsonProperty("userId")  val userId: Any,
    @JsonProperty("employeeId")  val employeeId: Any,
    @JsonProperty("password")  val password: String
)
