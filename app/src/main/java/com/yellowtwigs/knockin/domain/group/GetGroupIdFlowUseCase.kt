package com.yellowtwigs.knockin.domain.group

import com.yellowtwigs.knockin.repositories.groups.manage.CurrentGroupIdRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetGroupIdFlowUseCase @Inject constructor(private val currentGroupIdRepository: CurrentGroupIdRepository) {

    fun invoke(): Flow<Int?> {
        return currentGroupIdRepository.getCurrentGroupIdFlow()
    }
}