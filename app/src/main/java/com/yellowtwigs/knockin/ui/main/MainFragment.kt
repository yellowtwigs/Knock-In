package com.yellowtwigs.knockin.ui.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.FragmentMainBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        setupViewPager(binding)
        return binding.root
    }

    private fun setupViewPager(binding: FragmentMainBinding) {
        val viewModel: MainViewModel by activityViewModels()
        binding.viewPager.apply {
            adapter = ViewPagerAdapter(contextActivity)

            TabLayoutMediator(binding.tabLayout, this) { tab, position ->
                when (position) {
                    0 -> {
                        tab.icon =
                            AppCompatResources.getDrawable(contextActivity, R.drawable.ic_home)
                        viewModel.setToolbarTitle(R.string.contacts_list_title)
                        tab.setText(R.string.contacts_list_title)
                    }
                    1 -> {
                        tab.icon =
                            AppCompatResources.getDrawable(
                                contextActivity,
                                R.drawable.ic_groups
                            )
                        tab.setText(R.string.groups_list_title)
                    }
                    2 -> {
                        tab.icon = AppCompatResources.getDrawable(
                            contextActivity,
                            R.drawable.ic_notification
                        )
                        tab.setText(R.string.history_title)
                    }
                    3 -> {
                        tab.icon = AppCompatResources.getDrawable(
                            contextActivity,
                            R.drawable.ic_phone_call
                        )
                        tab.setText(R.string.bottom_navigation_view_cockpit)
                    }
                    else -> {
                        tab.icon =
                            AppCompatResources.getDrawable(contextActivity, R.drawable.ic_home)
                        tab.setText(R.string.contacts_list_title)
                    }
                }
            }.attach()

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> {
                            viewModel.setToolbarTitle(R.string.contacts_list_title)
                        }
                        1 -> {
                            viewModel.setToolbarTitle(R.string.groups_list_title)
                        }
                        2 -> {
                            viewModel.setToolbarTitle(R.string.history_title)
                        }
                        3 -> {
                            viewModel.setToolbarTitle(R.string.cockpit_title)
                        }
                        else -> {
                            viewModel.setToolbarTitle(R.string.contacts_list_title)
                        }
                    }
                    super.onPageSelected(position)
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    when (position) {
                        0 -> {
                            viewModel.setToolbarTitle(R.string.contacts_list_title)
                        }
                        1 -> {
                            viewModel.setToolbarTitle(R.string.groups_list_title)
                        }
                        2 -> {
                            viewModel.setToolbarTitle(R.string.history_title)
                        }
                        3 -> {
                            viewModel.setToolbarTitle(R.string.cockpit_title)
                        }
                        else -> {
                            viewModel.setToolbarTitle(R.string.contacts_list_title)
                        }
                    }
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }
            })
        }
    }
}