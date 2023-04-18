package com.yellowtwigs.knockin.utils

import androidx.collection.ArrayMap
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import dagger.hilt.android.scopes.ViewModelScoped
import java.lang.reflect.Method
import javax.inject.Inject
import kotlin.reflect.KClass

@ViewModelScoped
class NavArgProducer @Inject constructor(private val savedStateHandle: SavedStateHandle) {

    companion object {
        // Saves references to reflected Methods, because reflection can be costly
        private val methodMap = ArrayMap<KClass<out NavArgs>, Method>()
    }

    /**
     * Inspired from [androidx.navigation.NavArgsLazy]. Should its implementation change,
     * this function should change accordingly.
     */
    fun <Args : NavArgs> getNavArgs(clazz: KClass<Args>): Args {
        val method = methodMap.getOrPut(clazz) { clazz.java.getMethod("fromSavedStateHandle", SavedStateHandle::class.java) }

        @Suppress("UNCHECKED_CAST")
        return method.invoke(null, savedStateHandle) as Args
    }
}