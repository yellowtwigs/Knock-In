package com.yellowtwigs.knockin.ui.statistics.daily_statistics

data class DailyStatisticsViewState(
    val icon: Int,
    val numberOfNotificationsTotal: String,
    val numberOfNotificationsVip: String,
    val adviceMessage: String,
) {
}