package com.kylecorry.andromeda.exceptions

data class BugReportMessage(
    val title: String,
    val description: String,
    val emailAction: String,
    val ignoreAction: String,
    val emailAddress: String,
    val emailSubject: String,
    val generator: IBugReportGenerator
)