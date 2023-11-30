package com.yellowtwigs.knockin.ui.statistics.dashboard

data class DashboardViewState(
    val icon: Int,
    val adviceMessage: String,
    val notificationsTitle: String,
    val numberOfNotificationsUnprocessed: String,
    val numberOfNotificationsVip: String,
    val list: List<PieChartDataViewState>
) {}

data class PieChartDataViewState(
    val number: Int,
    val platform: String,
    val color: Int,
)