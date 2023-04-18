package com.yellowtwigs.knockin.ui.dashboard

data class DashboardViewState(
    val allVipNumbers: String,
    val vipNumbersDaily: String,
    val vipNumbersWeekly: String,
    val vipNumbersMonthly: String,

    val allMessagingNumbers: String,
    val messagingNumbersDaily: String,
    val messagingNumbersWeekly: String,
    val messagingNumbersMonthly: String,
    val timeSaved: String,
)