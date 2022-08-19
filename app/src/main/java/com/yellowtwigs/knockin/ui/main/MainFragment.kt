package com.yellowtwigs.knockin.ui.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.FragmentMainBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainFragment : BaseFragment() {

    private var binding: FragmentMainBinding? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupViewPager()
    }

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar() {
        contextActivity.setSupportActionBar(binding?.toolbar)
        val actionbar = contextActivity.supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(false)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(input: String?): Boolean {
                if (input != null) {
                    viewModel.setSearchText(input)
                }
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_by_first_name -> {
                item.isChecked = !item.isChecked
                viewModel.setSortedBy(R.id.sort_by_first_name)
                return true
            }
            R.id.sort_by_last_name -> {
                item.isChecked = !item.isChecked
                viewModel.setSortedBy(R.id.sort_by_last_name)
                return true
            }
            R.id.sort_by_priority -> {
                item.isChecked = !item.isChecked
                viewModel.setSortedBy(R.id.sort_by_priority)
                return true
            }
            R.id.sort_by_favorite -> {
                item.isChecked = !item.isChecked
                viewModel.setSortedBy(R.id.sort_by_favorite)
                return true
            }

            R.id.sms_filter -> {
                item.isChecked = !item.isChecked
                viewModel.setFilterBy(R.id.sms_filter)
                return true
            }
            R.id.mail_filter -> {
                item.isChecked = !item.isChecked
                viewModel.setFilterBy(R.id.mail_filter)
                return true
            }
            R.id.whatsapp_filter -> {
                item.isChecked = !item.isChecked
                viewModel.setFilterBy(R.id.whatsapp_filter)
                return true
            }
            R.id.messenger_filter -> {
                item.isChecked = !item.isChecked
                viewModel.setFilterBy(R.id.messenger_filter)
                return true
            }
            R.id.signal_filter -> {
                item.isChecked = !item.isChecked
                viewModel.setFilterBy(R.id.signal_filter)
                return true
            }
            R.id.telegram_filter -> {
                item.isChecked = !item.isChecked
                viewModel.setFilterBy(R.id.telegram_filter)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //endregion

    private fun setupViewPager() {
        binding?.viewPager?.apply {
            adapter = ViewPagerAdapter(contextActivity)

            binding?.tabLayout?.let { tabLayout ->
                TabLayoutMediator(tabLayout, this) { tab, position ->
                    when (position) {
                        0 -> {
                            tab.icon =
                                AppCompatResources.getDrawable(contextActivity, R.drawable.ic_home)
                            tab.setText(R.string.bottom_navigation_view_phone_book)
                        }
                        1 -> {
                            tab.icon =
                                AppCompatResources.getDrawable(contextActivity, R.drawable.ic_groups)
                            tab.setText(R.string.bottom_navigation_view_groups)
                        }
                        2 -> {
                            tab.icon = AppCompatResources.getDrawable(
                                contextActivity,
                                R.drawable.ic_notification
                            )
                            tab.setText(R.string.bottom_navigation_view_notify_history)
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
                            tab.setText(R.string.bottom_navigation_view_phone_book)
                        }
                    }
                }.attach()
            }
        }
    }
}