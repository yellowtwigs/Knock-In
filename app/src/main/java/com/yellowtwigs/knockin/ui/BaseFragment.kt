package com.yellowtwigs.knockin.ui

import androidx.fragment.app.Fragment
import com.yellowtwigs.knockin.ui.main.MainActivity

abstract class BaseFragment : Fragment() {

    protected val navController by lazy {
        (activity as MainActivity).navController
    }

    protected val contextActivity by lazy {
        activity as MainActivity
    }
}