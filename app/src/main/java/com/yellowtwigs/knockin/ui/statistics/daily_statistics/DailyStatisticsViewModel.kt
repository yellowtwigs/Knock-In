package com.yellowtwigs.knockin.ui.statistics.daily_statistics

import android.app.Application
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.get_number.GetNumberOfContactsFlowUseCase
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.ui.notifications.history.NotificationParams
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsListViewState
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DailyStatisticsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val getNumberOfContactsUseCase: GetNumberOfContactsFlowUseCase,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application
) : ViewModel() {

    val dailyStatisticsViewStateLiveData: LiveData<DailyStatisticsViewState> = liveData(coroutineDispatcherProvider.io) {
        combine(
            notificationsRepository.getAllNotifications().asFlow(),
            getNumberOfContactsUseCase.invoke(),
        ) { list, numberOfContacts ->

            val notifications = arrayListOf<NotificationsListViewState>()
            list.map { notification ->
                addNotificationInList(notifications, notification)
            }

            val notificationsAfterDistinct = notifications.distinctBy {
                NotificationParams(
                    contactName = it.contactName,
                    description = it.description,
                    platform = it.platform,
                    date = it.date,
                    idContact = it.idContact,
                    priority = it.priority,
                    phoneNumber = it.phoneNumber,
                    mail = it.mail,
                    isSystem = it.isSystem
                )
            }

            val allVipNumbers = notificationsAfterDistinct.filter { notification ->
                notification.priority == 2
            }.size

            val allMessagingNumbers = notificationsAfterDistinct.size

            val numberOfContactsVIP = numberOfContacts.numberOfVips
            val numberOfContactsStandard = numberOfContacts.numberOfStandard
            val numberOfContactsSilent = numberOfContacts.numberOfSilent

            val nonVipNotificationsNumbers = allMessagingNumbers.minus(allVipNumbers)
            val numberOfContactsTotal = numberOfContactsStandard.plus(numberOfContactsVIP).plus(numberOfContactsSilent)
            val isAllOtherContactsSilent = numberOfContactsTotal.minus(numberOfContactsVIP) == numberOfContactsSilent

            val adviceMessage = if (numberOfContactsVIP == 0 && numberOfContactsSilent == 0) {
                application.getString(R.string.strong_red_advice)
            } else if (numberOfContactsVIP < 5 && numberOfContactsSilent == 0) {
                application.getString(R.string.orange_advice)
            } else if (numberOfContactsVIP < 5 && nonVipNotificationsNumbers >= 5) {
                application.getString(R.string.yellow_advice)
            } else if (numberOfContactsVIP == 5 && nonVipNotificationsNumbers >= 5) {
                application.getString(R.string.light_green_advice)
            } else if (numberOfContactsVIP > 1 && isAllOtherContactsSilent) {
                application.getString(R.string.strong_green_advice)
            } else {
                application.getString(R.string.yellow_advice)
            }

            emit(DailyStatisticsViewState(adviceMessage = adviceMessage, icon = 0))
        }.collect()
    }

    private fun addNotificationInList(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        if (compareIfNotificationDateIsToday(notification.timestamp)) {
            val phoneNumber = if (notification.phoneNumber.contains(":")) {
                notification.phoneNumber.split(":")[1]
            } else {
                notification.phoneNumber
            }

            notifications.add(
                NotificationsListViewState(
                    notification.id,
                    notification.title,
                    notification.contactName,
                    notification.description,
                    notification.platform,
                    notification.timestamp,
                    SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(notification.timestamp)),
                    notification.idContact,
                    notification.priority,
                    phoneNumber,
                    notification.mail,
                    notification.isSystem,
                    1,
                    AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout),
                    null
                )
            )
        }
    }

    private fun compareIfNotificationDateIsToday(timestamp: Long): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.now().format(formatter)

            val notificationDate = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))

            val dateToday = date?.split("-")
            val notificationDateToday = notificationDate.split("-")

            val todayYear = dateToday?.get(0)?.toInt()
            val todayMonth = dateToday?.get(1)?.toInt()
            val todayDay = dateToday?.get(2)?.split(" ")?.get(0)

            val notificationYear = notificationDateToday[0].toInt()
            val notificationMonth = notificationDateToday[1].toInt()
            val notificationDay = notificationDateToday[2].split(" ")[0]

            return if (notificationYear != todayYear || notificationMonth != todayMonth) {
                false
            } else {
                notificationDay == todayDay
            }
        } catch (e: Exception) {
            Log.i("GetLocalDateTime", "Exception : $e")
        }
        return false
    }
}