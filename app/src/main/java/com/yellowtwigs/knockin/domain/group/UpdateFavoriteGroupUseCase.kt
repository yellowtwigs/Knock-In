package com.yellowtwigs.knockin.domain.group

import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import javax.inject.Inject

class UpdateFavoriteGroupUseCase @Inject constructor(
    private val groupsListRepository: GroupsListRepository,
    private val manageGroupRepository: ManageGroupRepository
) {
//    suspend fun updateFavoriteGroup(contactId: String) {
//        groupsListRepository.getAllGroupsFlow().collectLatest { groups ->
//            groups.map { group ->
//                if (group.name == "Favorites") {
//                    val list = arrayListOf<String>()
//                    list.addAll(group.listOfContactsData)
//
//                    if (group.listOfContactsData.contains(contactId)) {
//                        list.remove(contactId)
//                    } else {
//                        list.add(contactId)
//                    }
//
//                    manageGroupRepository.updateGroup(
//                        GroupDB(
//                            group.id,
//                            group.name,
//                            group.profilePicture,
//                            group.section_color,
//                            group.listOfContactsData,
//                            group.priority
//                        )
//                    )
//                } else {
//                    manageGroupRepository.insertGroup(
//                        GroupDB(
//                            0,
//                            "Favorites",
//                            "",
//                            0,
//                            listOf(contactId),
//                            0
//                        )
//                    )
//                }
//            }
//        }
//    }
}