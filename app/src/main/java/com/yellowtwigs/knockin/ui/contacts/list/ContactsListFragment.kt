package com.yellowtwigs.knockin.ui.contacts.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.FragmentContactsListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsListFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentContactsListBinding.inflate(inflater, container, false)
        val contactsListViewModel: ContactsListViewModel by activityViewModels()

        setupRecyclerView(binding, contactsListViewModel)

        return binding.root
    }

    //region =========================================== SETUP UI ===========================================

    private fun setupRecyclerView(
        binding: FragmentContactsListBinding, contactsListViewModel: ContactsListViewModel
    ) {
        val contactsListAdapter = context?.let { context ->
            ContactsListAdapter(context) { id ->

            }
        }

        binding.recyclerView.apply {
            contactsListViewModel.getAllContacts()
                .observe(viewLifecycleOwner, Observer { contacts ->
                    contactsListAdapter?.submitList(contacts)
                })
            adapter = contactsListAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
    }

    //endregion
}