package com.yellowtwigs.knockin.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.google.android.material.tabs.TabLayoutMediator
import com.yellowtwigs.knockin.databinding.FragmentMainBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseFragment() {

    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    //region =========================================== TOOLBAR ============================================

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        searchBarTextListener()
    }

    private fun searchBarTextListener() {
        binding?.searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (text?.toString() != null) {
                    viewModel.setSearchText(text.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setupViewPager() {
        binding?.viewPager?.adapter = ViewPagerAdapter(contextActivity)

        TabLayoutMediator(
            binding?.tabLayout!!,
            binding?.viewPager!!
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Characters"
                }
                1 -> {
                    tab.text = "Episodes"
                }
            }
        }.attach()
    }


}