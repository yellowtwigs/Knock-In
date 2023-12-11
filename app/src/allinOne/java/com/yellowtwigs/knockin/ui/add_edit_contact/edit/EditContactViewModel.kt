package com.yellowtwigs.knockin.ui.add_edit_contact.edit

import androidx.lifecycle.*
import com.yellowtwigs.knockin.domain.contact.CreateContactUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepository
import com.yellowtwigs.knockin.ui.contacts.SingleContactViewState
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberToSinglePhoneNumberWithFlag
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberWithSpinnerToFlag
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(
    private val editContactRepository: EditContactRepository,
    private val createContactUseCase: CreateContactUseCase,
    dispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    val phoneNumbersFlow = MutableStateFlow<ArrayList<PhoneNumberWithFlag>>(arrayListOf())

    fun getEditContactViewStateById(id: Int): LiveData<EditContactViewState> {
        return editContactRepository.getContact(id).map {
            transformContactDbToEditContactViewState(it)
        }
    }

    fun getSingleContactViewStateById(id: Int): LiveData<SingleContactViewState> {
        return editContactRepository.getContact(id).map {
            transformContactDbToSingleContactViewState(it)
        }
    }

    val phoneNumbersViewStateLiveData: LiveData<List<PhoneNumberWithFlag>> = liveData(dispatcherProvider.io) {
        phoneNumbersFlow.collect()
    }

    private fun transformContactDbToSingleContactViewState(contact: ContactDB): SingleContactViewState {
        return if (contact != null) {
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
    }

    private fun transformContactDbToEditContactViewState(contact: ContactDB): EditContactViewState {
        return EditContactViewState(
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
    }

    suspend fun updateContact(contact: ContactDB) {
        editContactRepository.updateContact(contact)
    }

    fun checkDuplicateContact(contact: ContactDB): Boolean {
        return createContactUseCase.checkIfContactDuplicate(contact)
    }

    suspend fun addNewContact(contact: ContactDB) = editContactRepository.addNewContact(contact)

    suspend fun deleteContactById(id: Int) = editContactRepository.deleteContactById(id)
}