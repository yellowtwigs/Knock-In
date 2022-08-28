package com.yellowtwigs.knockin.ui

import androidx.fragment.app.Fragment
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity

abstract class BaseFragment : Fragment() {

    protected val navController by lazy {
    }

    protected val contextActivity by lazy {
        activity as ContactsListActivity
    }
}