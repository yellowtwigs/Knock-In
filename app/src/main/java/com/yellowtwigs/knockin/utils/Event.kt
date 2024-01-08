package com.yellowtwigs.knockin.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

data class Event<out T>(private val content: T) {

    private var handled = false

    // Keep this function private to force the use of the observeEvent extension
    private fun getContentIfNotHandled(): T? = if (handled) {
        null
    } else {
        handled = true
        content
    }

    companion object {
        /**
         * An extension for [Event]s, simplifying the pattern of checking if the [Event]'s content has already been handled.
         *
         * [onEventUnhandledContent] is *only* called if the [Event]'s contents has not been handled.
         */
        fun <T> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, onEventUnhandledContent: (T) -> Unit) {
            observe(owner) { event ->
                event.getContentIfNotHandled()?.let {
                    onEventUnhandledContent(it)
                }
            }
        }
    }
}