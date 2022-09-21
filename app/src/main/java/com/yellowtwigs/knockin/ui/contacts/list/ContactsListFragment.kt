package com.yellowtwigs.knockin.ui.contacts.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yellowtwigs.knockin.databinding.FragmentContactsListBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsListFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentContactsListBinding.inflate(inflater, container, false)
        return binding.root
    }
}