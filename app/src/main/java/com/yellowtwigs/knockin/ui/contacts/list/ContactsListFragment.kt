package com.yellowtwigs.knockin.ui.contacts.list

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.FragmentContactsListBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import com.yellowtwigs.knockin.ui.main.MainViewModel
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
        setupRecyclerView(binding)
        return binding.root
    }

    //region =========================================== SETUP UI ===========================================

    private fun setupRecyclerView(binding: FragmentContactsListBinding) {
        val contactsListViewModel: ContactsListViewModel by activityViewModels()

        val contactsListAdapter = ContactsListAdapter(contextActivity) { id ->
//            navController.navigate(
//                ContactsListFragmentDirections.actionContactsListFragmentToEditContactFragment(
//                    id
//                )
//            )
        }

        binding.recyclerView.apply {
            contactsListViewModel.getAllContacts().observe(viewLifecycleOwner) { contacts ->
                contactsListAdapter.submitList(contacts)
                scrollToPosition(0)
            }
            adapter = contactsListAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    contactsListAdapter.setIsScrolling(true)
                    super.onScrollStateChanged(recyclerView, newState)
                }
            })
        }
    }

    //endregion
}