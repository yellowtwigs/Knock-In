package com.yellowtwigs.knockin.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DemoWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val demoWorkerDependencies: DemoWorkerDependencies,
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.i("GetNotification", "Passe par là : doWork()")
        return Result.success()
    }
}