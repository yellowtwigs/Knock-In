package com.yellowtwigs.knockin.ui.fragments

import androidx.fragment.app.Fragment
import com.yellowtwigs.knockin.ui.activity.MainActivity

abstract class BaseFragment : Fragment() {

    protected val navController by lazy {
        (activity as MainActivity).navController
    }

    protected val contextActivity by lazy {
        activity as MainActivity
    }
}