package com.yellowtwigs.knockin.domain.group

import com.yellowtwigs.knockin.repositories.groups.manage.CurrentGroupIdRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetGroupIdFlowUseCase @Inject constructor(private val currentGroupIdRepository: CurrentGroupIdRepository) {

    fun invoke(id: Int) {
        currentGroupIdRepository.setCurrentGroupIdFlow(id)
    }
}