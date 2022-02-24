package com.yellowtwigs.knockin.ui

import androidx.fragment.app.Fragment
import com.yellowtwigs.knockin.ui.activity.MainActivity

abstract class BaseFragment : Fragment() {

    protected val navController by lazy {
        (activity as MainActivity).navController
    }

    protected val cxt by lazy {
        activity as MainActivity
    }
}