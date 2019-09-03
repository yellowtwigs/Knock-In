package com.yellowtwigs.knockin.model

import android.os.Handler
import android.os.HandlerThread

/**
 * La Classe qui permet d'executer un runnable dans un thread
 * @author Ryan Granet
 */
class DbWorkerThread(threadName: String) : HandlerThread(threadName){

    private lateinit var mWorkerHandler: Handler

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mWorkerHandler = Handler(looper)
    }

    //lance la fonction task dans un thread
    fun postTask(task: Runnable) {
        if (this::mWorkerHandler.isInitialized) {
            mWorkerHandler.post(task)
        } else {
            postTask(task)
        }
    }

}