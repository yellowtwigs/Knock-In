package com.yellowtwigs.knockin.ui.contacts.list

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.DeleteContactUseCase
import com.yellowtwigs.knockin.domain.contact.get_number.GetNumberOfContactsUseCase
import com.yellowtwigs.knockin.domain.contact.id.get.GetCurrentContactIdChannelUseCase
import com.yellowtwigs.knockin.domain.contact.id.set.SetCurrentContactIdUseCase
import com.yellowtwigs.knockin.domain.contact.list.GetAllContactsUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.ui.notifications.history.NotificationParams
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsListViewState
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import com.yellowtwigs.knockin.utils.EquatableCallback
import com.yellowtwigs.knockin.utils.Event
import com.yellowtwigs.knockin.utils.NotificationsGesture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ContactsListViewModel @Inject constructor(
    private val getAllContactsUseCase: GetAllContactsUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val getCurrentContactIdChannelUseCase: GetCurrentContactIdChannelUseCase,
    private val setCurrentContactIdUseCase: SetCurrentContactIdUseCase,
    private val notificationsRepository: NotificationsRepository,
    private val getNumberOfContactsUseCase: GetNumberOfContactsUseCase,
    private val coroutineDispatcher: CoroutineDispatcherProvider,
    private val application: Application,
) : ViewModel() {

    private val USER_POINT = "USER_POINT"
    private val USER_POINT_TIME = "USER_POINT_TIME"
    val mutableLiveDataPass50 = MutableLiveData(false)
    val mutableLiveDataPass200 = MutableLiveData(false)

    fun recurrentWork() {
        CoroutineScope(coroutineDispatcher.default).launch {
            try {
                val notifications = arrayListOf<NotificationsListViewState>()
                notificationsRepository.getAllNotificationsList().map { notification ->
                    addNotificationInListDaily(notifications, notification)
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

                val allVipNotifications = notificationsAfterDistinct.filter { notification ->
                    notification.priority == 2
                }.size

                val allMessagingNumbers = notificationsAfterDistinct.size
                val numberOfContacts = getNumberOfContactsUseCase.invoke()
                val numberOfContactsVIP = numberOfContacts.numberOfVips
                val numberOfContactsStandard = numberOfContacts.numberOfStandard
                val numberOfContactsSilent = numberOfContacts.numberOfSilent

                val nonVipNotificationsNumbers = allMessagingNumbers.minus(allVipNotifications)
                val numberOfContactsTotal = numberOfContactsStandard.plus(numberOfContactsVIP).plus(numberOfContactsSilent)
                val isAllOtherContactsSilent = numberOfContactsTotal.minus(numberOfContactsVIP) == numberOfContactsSilent

                val points: Int
                val adviceMessage: String

                // 6

                if (numberOfContactsVIP == 0 && numberOfContactsSilent == 0) {
                    points = 0
                    adviceMessage = application.getString(R.string.strong_red_advice)
                } else if (numberOfContactsVIP > 1 && isAllOtherContactsSilent) {
                    points = 12
                    adviceMessage = application.getString(R.string.strong_green_advice)
                } else if (numberOfContactsVIP == 5 && nonVipNotificationsNumbers >= 5) {
                    points = 7
                    adviceMessage = application.getString(R.string.light_green_advice)
                } else if (numberOfContactsVIP in 1..5 && numberOfContactsSilent == 0 && nonVipNotificationsNumbers < 5) {
                    points = 2
                    adviceMessage = application.getString(R.string.orange_advice)
                } else if (numberOfContactsVIP < 5 && nonVipNotificationsNumbers >= 5) {
                    points = 4
                    adviceMessage = application.getString(R.string.yellow_advice)
                } else {
                    points = 2
                    adviceMessage = application.getString(R.string.orange_advice)
                }

                val pointsAlreadyHad = getStatisticsPoints()

                val isUnder50Before = pointsAlreadyHad < 50
                val isUnder200Before = pointsAlreadyHad < 200
                val isUnder50After = pointsAlreadyHad + points > 50
                val isUnder200After = pointsAlreadyHad + points > 200

                mutableLiveDataPass50.postValue(isUnder50Before && isUnder50After)
                mutableLiveDataPass200.postValue(isUnder200Before && isUnder200After)

                Log.i("GetNotification", "pointCalculationUseCase.getStatisticsPoints() : ${getStatisticsPoints()}")

                setStatisticsPoints(points)
                buildNotificationWorker(adviceMessage, points, getStatisticsPoints())

                Log.i("GetNotification", "pointCalculationUseCase.getStatisticsPoints() : ${getStatisticsPoints()}")
            } catch (e: Exception) {
                Log.i("GetNotification", "Exception : $e")
            }
        }
    }

    private fun getStatisticsPoints(): Int {
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(USER_POINT, 0)
    }

    private fun setStatisticsPoints(pointsToAdd: Int) {
        val sharedPreferencesPoints: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        val points = sharedPreferencesPoints.getInt(USER_POINT, 0)
        val editorPoints = sharedPreferencesPoints.edit()
        editorPoints.putInt(USER_POINT, points + pointsToAdd)
        editorPoints.apply()

        val sharedPreferencesTime: SharedPreferences = application.getSharedPreferences(USER_POINT_TIME, Context.MODE_PRIVATE)
        val editor = sharedPreferencesTime.edit()
        editor.putLong(USER_POINT_TIME, localDateTimeToTimestamp(LocalDateTime.now()))
        editor.apply()
    }

    private fun localDateTimeToTimestamp(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    private fun buildNotificationWorker(adviceMessage: String, points: Int, statisticsPoints: Int) {
        val notificationManager = NotificationManagerCompat.from(application)
        val CHANNEL_ID = "CHANNEL_ID"
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, application.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val intent: Intent = DailyStatisticsActivity.navigate(application)
        val pendingIntent: PendingIntent? =
            TaskStackBuilder.create(application).addNextIntentWithParentStack(intent).getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        notificationManager.notify(
            1,
            NotificationCompat.Builder(application, CHANNEL_ID).setSmallIcon(R.drawable.ic_knockin_logo).setContentTitle(adviceMessage)
                .setContentText(application.getString(R.string.today_points_calculation, points, statisticsPoints))
                .setStyle(NotificationCompat.BigTextStyle()).setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).setAutoCancel(true).build()
        )
    }

    private fun addNotificationInListDaily(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == NotificationsGesture.KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        if (notification.platform != Telephony.Sms.getDefaultSmsPackage(application) && notification.platform != NotificationsGesture.WHATSAPP_PACKAGE && notification.platform != NotificationsGesture.FACEBOOK_PACKAGE && notification.platform != NotificationsGesture.MESSENGER_PACKAGE && notification.platform != NotificationsGesture.GMAIL_PACKAGE && notification.platform != NotificationsGesture.OUTLOOK_PACKAGE && notification.platform != NotificationsGesture.SIGNAL_PACKAGE && notification.platform != NotificationsGesture.TELEGRAM_PACKAGE) {
        } else {
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
                        systemPriority,
                        AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout),
                        null
                    )
                )
            }
        }
    }

    private val filteringMutableStateFlow = MutableStateFlow(R.id.empty_filter)
    private val sortingMutableStateFlow = MutableStateFlow(R.id.sort_by_priority)
    private val searchBarTextFlow = MutableStateFlow("")

    sealed class MainViewAction {
        sealed class Navigate : MainViewAction() {
            data class ContactDetails(val currentId: Int) : Navigate()
        }
    }

    val mainViewAction: LiveData<Event<MainViewAction>> = liveData {
        getCurrentContactIdChannelUseCase.invoke().collect { id ->
            id?.let { idNotNull ->
                emit(Event(MainViewAction.Navigate.ContactDetails(idNotNull)))
            }
        }
    }

    val contactsListViewStateLiveData = liveData(coroutineDispatcher.io) {
        combine(
            getAllContactsUseCase.contactsListViewStateLiveData.asFlow(), sortingMutableStateFlow, filteringMutableStateFlow, searchBarTextFlow
        ) { contacts: List<ContactDB>, sort: Int, filterId: Int, input: String ->
            emit(filterWithInput(input, sortContactsList(sort, filterId, contacts.map { transformContactDbToContactsListViewState(it) })))
        }.collect()
    }

    private fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        val fullName = if (contact.firstName.isEmpty() || contact.firstName.isBlank() || contact.firstName == " ") {
            contact.lastName.uppercase()
        } else if (contact.lastName.isEmpty() || contact.lastName.isBlank() || contact.lastName == " ") {
            contact.firstName.uppercase()
        } else {
            "${contact.firstName.uppercase()} ${contact.firstName.uppercase()}"
        }

        return ContactsListViewState(id = contact.id,
            fullName = fullName,
            firstName = contact.firstName,
            lastName = contact.lastName,
            profilePicture = contact.profilePicture,
            profilePicture64 = contact.profilePicture64,
            firstPhoneNumber = ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, true),
            secondPhoneNumber = ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, false),
            listOfMails = contact.listOfMails,
            priority = contact.priority,
            isFavorite = contact.isFavorite == 1,
            messengerId = contact.messengerId,
            hasWhatsapp = contact.listOfMessagingApps.contains("com.whatsapp"),
            hasTelegram = contact.listOfMessagingApps.contains("org.telegram.messenger"),
            hasSignal = contact.listOfMessagingApps.contains("org.thoughtcrime.securesms"),
            onClickedCallback = EquatableCallback {
                Log.i("GetId", "contact.id : ${contact.id}")
                setCurrentContactIdUseCase.invoke(contact.id)
            })
    }


    private fun filterWithInput(input: String, listOfContacts: List<ContactsListViewState>): List<ContactsListViewState> {
        return listOfContacts.filter { contact ->
            val name = contact.firstName + " " + contact.lastName
            name.contains(input) || name.uppercase().contains(input.uppercase()) || name.lowercase().contains(input.lowercase())
        }
    }

    private fun sortContactsList(sortedBy: Int, filterBy: Int, listOfContacts: List<ContactsListViewState>): List<ContactsListViewState> {
        return when (sortedBy) {
            R.id.sort_by_full_name -> {
                filterContactsList(filterBy, listOfContacts.sortedBy { it.fullName })
            }

            R.id.sort_by_priority -> {
                filterContactsList(filterBy, listOfContacts.sortedBy { it.fullName }.sortedByDescending { it.priority })
            }

            R.id.sort_by_favorite -> {
                filterContactsList(filterBy, listOfContacts.sortedBy { it.fullName }.sortedByDescending { it.isFavorite })
            }

            else -> {
                filterContactsList(filterBy, listOfContacts.sortedByDescending { it.priority }.sortedBy { it.fullName })
            }
        }
    }

    private fun filterContactsList(filterBy: Int, listOfContacts: List<ContactsListViewState>): List<ContactsListViewState> {
        when (filterBy) {
            R.id.sms_filter -> {
                return listOfContacts.filter { it.firstPhoneNumber.phoneNumber.isNotBlank() }
            }

            R.id.mail_filter -> {
                return listOfContacts.filter { it.listOfMails.isNotEmpty() && it.listOfMails[0].isNotEmpty() }
            }

            R.id.whatsapp_filter -> {
                return listOfContacts.filter { it.hasWhatsapp }
            }

            R.id.messenger_filter -> {
                return listOfContacts.filter {
                    it.messengerId != "" && it.messengerId.isNotBlank() && it.messengerId.isNotEmpty()
                }
            }

            R.id.signal_filter -> {
                return listOfContacts.filter { it.hasSignal }
            }

            R.id.telegram_filter -> {
                return listOfContacts.filter { it.hasTelegram }
            }

            else -> {
                return listOfContacts
            }
        }
    }

    fun setSearchTextChanged(text: String) {
        searchBarTextFlow.tryEmit(text)
    }

    fun setSortedBy(sortedBy: Int) {
        sortingMutableStateFlow.tryEmit(sortedBy)
    }

    fun setFilterBy(filterBy: Int) {
        filteringMutableStateFlow.tryEmit(filterBy)
    }

    private fun compareIfNotificationDateIsToday(timestamp: Long): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.now().format(formatter)

            val configuration = application.resources.configuration
            val notificationDate = if (configuration.locales.get(0).language == "ar") {
                translateToEnglish(SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))) ?: date
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))
            }

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

    private fun translateToEnglish(arabicDateTime: String): String? {
        try {
            val arabicFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))
            val arabicDate = arabicFormat.parse(arabicDateTime)
            val englishFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            return arabicDate?.let { englishFormat.format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    suspend fun deleteContactsSelected(listOfContacts: List<Int>) {
        listOfContacts.map {
            deleteContactUseCase.deleteContactById(it)
        }
    }
}