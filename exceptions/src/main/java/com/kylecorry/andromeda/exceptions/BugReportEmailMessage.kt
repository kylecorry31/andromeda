package com.kylecorry.andromeda.exceptions

/**
 * @param title the title of the dialog shown to the user
 * @param description the description of the dialog shown to the user
 * @param emailAction the text for the button to email
 * @param ignoreAction the text for the button to ignore the exception
 * @param emailAddress the email address to send the exception log to
 * @param emailSubject the subject of the email
 */
data class BugReportEmailMessage(
    val title: String,
    val description: String,
    val emailAction: String,
    val ignoreAction: String,
    val emailAddress: String,
    val emailSubject: String
)