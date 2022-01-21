package com.yellowtwigs.knockin.ui.fragments

import androidx.fragment.app.Fragment
import com.yellowtwigs.knockin.ui.activities.edit_contact.EditContactActivity

abstract class BaseFragmentEditContact : Fragment() {

    protected val navController by lazy {
        (activity as EditContactActivity).navController
    }

    protected val contextActivity by lazy {
        activity as EditContactActivity
    }
}