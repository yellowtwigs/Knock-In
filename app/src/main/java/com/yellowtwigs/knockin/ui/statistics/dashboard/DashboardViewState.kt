package com.yellowtwigs.knockin.ui.statistics.dashboard

data class DashboardViewState(
    val notificationsTitle: String, val list: List<PieChartDataViewState>
) {}

data class PieChartDataViewState(
    val number: Int,
    val platform: String,
    val color: Int,
) {}

//data class DashboardViewState(
//    val allVipNumbers: Int,
//    val vipNumbersDaily: Int,
//    val vipNumbersWeekly: Int,
//    val vipNumbersMonthly: Int,
//
//    val allMessagingNumbers: Int,
//    val messagingNumbersDaily: Int,
//    val messagingNumbersWeekly: Int,
//    val messagingNumbersMonthly: Int,
//    val timeSaved: String,
//
//    val messagingNumbersSms: Int,
//    val messagingNumbersWhatsapp: Int,
//    val messagingNumbersMessenger: Int,
//    val messagingNumbersMail: Int,
//    val messagingNumbersTelegram: Int,
//    val messagingNumbersSignal: Int,
//)