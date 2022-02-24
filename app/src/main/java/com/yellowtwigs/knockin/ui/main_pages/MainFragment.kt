package com.yellowtwigs.knockin.ui.main_pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.FragmentMainBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import com.yellowtwigs.knockin.utils.AppTheme.checkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseFragment() {

    private var binding: FragmentMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkTheme(cxt)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
    }

    private fun setupViewPager() {
        binding?.apply {
            viewPager.adapter = ViewPagerAdapter(cxt)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> {
                        getString(R.string.bottom_navigation_view_phone_book)
                    }
                    1 -> {
                        getString(R.string.bottom_navigation_view_groups)
                    }
                    2 -> {
                        getString(R.string.bottom_navigation_view_notify_history)
                    }
                    else -> {
                        getString(R.string.bottom_navigation_view_cockpit)
                    }
                }
            }.attach()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}