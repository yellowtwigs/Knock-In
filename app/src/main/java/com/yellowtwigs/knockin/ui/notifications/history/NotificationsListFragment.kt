package com.yellowtwigs.knockin.ui.notifications.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.databinding.FragmentNotificationsBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsListFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val notificationsListViewModel: NotificationsListViewModel by activityViewModels()

        setupRecyclerView(binding, notificationsListViewModel)

        return binding.root
    }

    //region =========================================== SETUP UI ===========================================

    private fun setupRecyclerView(
        binding: FragmentNotificationsBinding, notificationsListViewModel: NotificationsListViewModel
    ) {
        val notificationsListAdapter = NotificationsListAdapter(contextActivity)

        binding.recyclerView.apply {
            notificationsListViewModel.getAllNotifications()
                .observe(viewLifecycleOwner) { notifications ->
                    notificationsListAdapter.submitList(notifications)
                }
            adapter = notificationsListAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
    }

    //endregion
}