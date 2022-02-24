package com.yellowtwigs.knockin.ui.main_pages

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yellowtwigs.knockin.ui.contacts.ContactsListFragment

class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ContactsListFragment()
            }
            1 -> {
                GroupsFragment()
            }
            2 -> {
                NotificationsListFragment()
            }
            else -> {
                CockpitFragment()
            }
        }
    }
}