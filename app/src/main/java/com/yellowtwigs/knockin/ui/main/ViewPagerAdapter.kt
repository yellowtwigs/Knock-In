package com.yellowtwigs.knockin.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListFragment
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsListFragment

class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ContactsListFragment()
            }
            1 -> {
                // Group
                ContactsListFragment()
            }
            2 -> {
                // Notifications
                NotificationsListFragment()
            }
            else -> {
                // Cockbit
                ContactsListFragment()
            }
        }
    }
}