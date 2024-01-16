package com.yellowtwigs.knockin.ui.add_edit_contact.edit

import androidx.lifecycle.*
import com.yellowtwigs.knockin.domain.contact.CreateContactUseCase
import com.yellowtwigs.knockin.domain.contact.GetContactByIdFlowUseCase
import com.yellowtwigs.knockin.domain.contact.id.get.GetCurrentContactIdChannelUseCase
import com.yellowtwigs.knockin.domain.contact.id.get.GetCurrentContactIdFlowUseCase
import com.yellowtwigs.knockin.domain.contact.id.set.SetCurrentContactIdUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepository
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberToSinglePhoneNumberWithFlag
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberWithSpinnerToFlag
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import com.yellowtwigs.knockin.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(
    private val editContactRepository: EditContactRepository,
    private val createContactUseCase: CreateContactUseCase,
    private val getContactByIdFlowUseCase: GetContactByIdFlowUseCase,
    private val getCurrentContactIdUseCase: GetCurrentContactIdFlowUseCase,
    dispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    val phoneNumbersFlow = MutableStateFlow<ArrayList<PhoneNumberWithFlag>>(arrayListOf())

    private var singleContactViewStateMutableSharedFlow = MutableSharedFlow<SingleContactViewState>(replay = 1)
    private var editContactViewStateMutableSharedFlow = MutableSharedFlow<EditContactViewState>(replay = 1)
    private var test = MutableStateFlow(0)
    private var test2 = MutableStateFlow(0)

    val singleContactViewStateLiveData = liveData {
        combine(singleContactViewStateMutableSharedFlow, test, test2) { singleViewState, aggregatedPhotos, isSold ->
            emit(singleViewState)
        }.collect()
    }

    val editContactViewStateLiveData = liveData {
        combine(editContactViewStateMutableSharedFlow, test, test2) { editContactViewState, aggregatedPhotos, isSold ->
            emit(editContactViewState)
        }.collect()
    }

    fun getSingleContactByIdLiveData(id: Int): LiveData<SingleContactViewState> {
        return getContactByIdFlowUseCase.invoke(id).asLiveData().map { contact ->
            SingleContactViewState(
                id = contact.id,
                androidId = contact.androidId,
                fullName = contact.fullName,
                firstName = contact.firstName,
                lastName = contact.lastName,
                profilePicture = contact.profilePicture,
                profilePicture64 = contact.profilePicture64,
                firstPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, true).phoneNumber,
                firstPhoneNumberFlag = transformPhoneNumberWithSpinnerToFlag(
                    transformPhoneNumberToSinglePhoneNumberWithSpinner(
                        contact.listOfPhoneNumbers, true
                    )
                ),
                listOfMails = contact.listOfMails,
                mail_name = contact.mail_name,
                priority = contact.priority,
                isFavorite = contact.isFavorite,
                messengerId = contact.messengerId,
                listOfMessagingApps = contact.listOfMessagingApps,
                notificationTone = contact.notificationTone,
                notificationSound = contact.notificationSound,
                isCustomSound = contact.isCustomSound,
                vipSchedule = contact.vipSchedule,
                hourLimitForNotification = contact.hourLimitForNotification,
                audioFileName = contact.audioFileName,
            )
        }
    }

    fun getContactByIdLiveData(id: Int): LiveData<EditContactViewState> {
        return getContactByIdFlowUseCase.invoke(id).asLiveData().map { contact ->
            if (contact != null) {
                EditContactViewState(
                    id = contact.id,
                    androidId = contact.androidId,
                    fullName = contact.fullName,
                    firstName = contact.firstName,
                    lastName = contact.lastName,
                    profilePicture = contact.profilePicture,
                    profilePicture64 = contact.profilePicture64,
                    firstPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithFlag(contact.listOfPhoneNumbers, true),
                    listOfMails = contact.listOfMails,
                    mail_name = contact.mail_name,
                    priority = contact.priority,
                    isFavorite = contact.isFavorite,
                    messengerId = contact.messengerId,
                    listOfMessagingApps = contact.listOfMessagingApps,
                    notificationTone = contact.notificationTone,
                    notificationSound = contact.notificationSound,
                    isCustomSound = contact.isCustomSound,
                    vipSchedule = contact.vipSchedule,
                    hourLimitForNotification = contact.hourLimitForNotification,
                    audioFileName = contact.audioFileName,
                )
            } else {
                EditContactViewState(
                    id = 0,
                    androidId = 0,
                    fullName = "",
                    firstName = "",
                    lastName = "",
                    profilePicture = 1,
                    profilePicture64 = "",
                    firstPhoneNumber = PhoneNumberWithFlag("", ""),
                    listOfMails = listOf(),
                    mail_name = "",
                    priority = 1,
                    isFavorite = 1,
                    messengerId = "",
                    listOfMessagingApps = listOf(),
                    notificationTone = "",
                    notificationSound = 1,
                    isCustomSound = 1,
                    vipSchedule = 1,
                    hourLimitForNotification = "",
                    audioFileName = "",
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            getCurrentContactIdUseCase.invoke().collect { id ->
                id?.let {
                    val contact = getContactByIdFlowUseCase.invoke(id).firstOrNull()

                    val singleContact = if (contact != null) {
                        SingleContactViewState(
                            id = contact.id,
                            androidId = contact.androidId,
                            fullName = contact.fullName,
                            firstName = contact.firstName,
                            lastName = contact.lastName,
                            profilePicture = contact.profilePicture,
                            profilePicture64 = contact.profilePicture64,
                            firstPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, true).phoneNumber,
                            firstPhoneNumberFlag = transformPhoneNumberWithSpinnerToFlag(
                                transformPhoneNumberToSinglePhoneNumberWithSpinner(
                                    contact.listOfPhoneNumbers, true
                                )
                            ),
                            listOfMails = contact.listOfMails,
                            mail_name = contact.mail_name,
                            priority = contact.priority,
                            isFavorite = contact.isFavorite,
                            messengerId = contact.messengerId,
                            listOfMessagingApps = contact.listOfMessagingApps,
                            notificationTone = contact.notificationTone,
                            notificationSound = contact.notificationSound,
                            isCustomSound = contact.isCustomSound,
                            vipSchedule = contact.vipSchedule,
                            hourLimitForNotification = contact.hourLimitForNotification,
                            audioFileName = contact.audioFileName,
                        )
                    } else {
                        SingleContactViewState(
                            id = 0,
                            androidId = 0,
                            fullName = "",
                            firstName = "",
                            lastName = "",
                            profilePicture = 1,
                            profilePicture64 = "",
                            firstPhoneNumber = "",
                            firstPhoneNumberFlag = "",
                            listOfMails = listOf(),
                            mail_name = "",
                            priority = 1,
                            isFavorite = 1,
                            messengerId = "",
                            listOfMessagingApps = listOf(),
                            notificationTone = "",
                            notificationSound = 1,
                            isCustomSound = 1,
                            vipSchedule = 1,
                            hourLimitForNotification = "",
                            audioFileName = "",
                        )
                    }

                    val editContact = if (contact != null) {
                        EditContactViewState(
                            id = contact.id,
                            androidId = contact.androidId,
                            fullName = contact.fullName,
                            firstName = contact.firstName,
                            lastName = contact.lastName,
                            profilePicture = contact.profilePicture,
                            profilePicture64 = contact.profilePicture64,
                            firstPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithFlag(contact.listOfPhoneNumbers, true),
                            listOfMails = contact.listOfMails,
                            mail_name = contact.mail_name,
                            priority = contact.priority,
                            isFavorite = contact.isFavorite,
                            messengerId = contact.messengerId,
                            listOfMessagingApps = contact.listOfMessagingApps,
                            notificationTone = contact.notificationTone,
                            notificationSound = contact.notificationSound,
                            isCustomSound = contact.isCustomSound,
                            vipSchedule = contact.vipSchedule,
                            hourLimitForNotification = contact.hourLimitForNotification,
                            audioFileName = contact.audioFileName,
                        )
                    } else {
                        EditContactViewState(
                            id = 0,
                            androidId = 0,
                            fullName = "",
                            firstName = "",
                            lastName = "",
                            profilePicture = 1,
                            profilePicture64 = "",
                            firstPhoneNumber = PhoneNumberWithFlag("", ""),
                            listOfMails = listOf(),
                            mail_name = "",
                            priority = 1,
                            isFavorite = 1,
                            messengerId = "",
                            listOfMessagingApps = listOf(),
                            notificationTone = "",
                            notificationSound = 1,
                            isCustomSound = 1,
                            vipSchedule = 1,
                            hourLimitForNotification = "",
                            audioFileName = "",
                        )
                    }

                    singleContactViewStateMutableSharedFlow.tryEmit(singleContact)
                    editContactViewStateMutableSharedFlow.tryEmit(editContact)
                }
            }
        }
    }

    val phoneNumbersViewStateLiveData: LiveData<List<PhoneNumberWithFlag>> = liveData(dispatcherProvider.io) {
        phoneNumbersFlow.collect()
    }

    suspend fun updateContact(contact: ContactDB) {
        editContactRepository.updateContact(contact)
    }

    fun checkDuplicateContact(contact: ContactDB): Boolean {
        return createContactUseCase.checkIfContactDuplicate(contact)
    }

    suspend fun addNewContact(contact: ContactDB) = editContactRepository.addNewContact(contact)

    fun deleteContactById(id: Int) = viewModelScope.launch {
        editContactRepository.deleteContactById(id)
    }

    suspend fun updateFavorite(contactId: String) {
//        updateFavoriteGroupUseCase.updateFavoriteGroup(contactId)
    }
}