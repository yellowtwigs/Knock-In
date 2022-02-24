package com.yellowtwigs.knockin.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.databinding.FragmentContactsListBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import com.yellowtwigs.knockin.utils.AppTheme.checkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsListFragment : BaseFragment() {

    private var binding: FragmentContactsListBinding? = null
    private val viewModel: ContactsViewModel by activityViewModels()
    private lateinit var contactsAdapter: ContactsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkTheme(cxt)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding?.recyclerView?.apply {
            contactsAdapter = ContactsListAdapter(cxt, this@ContactsListFragment) { id ->
                setCurrentContact(id)

//                val navDir = ListOfMoviesFragmentDirections.actionListOfMoviesFragmentToMovieDetailFragment()
//                navController.navigate(navDir)
            }
            adapter = contactsAdapter
            getAllContactsFromViewModel()
            layoutManager = LinearLayoutManager(cxt)
            setHasFixedSize(true)
        }
    }

    private fun getAllContactsFromViewModel() {
        viewModel.getAllContacts.observe(viewLifecycleOwner) { contacts ->
            CoroutineScope(Dispatchers.Main).launch {
                launchProgressBarSpin(2000)
            }
            contactsAdapter.submitList(contacts)
        }
    }

    private fun setCurrentContact(id: Int) {
        viewModel.setContactLiveData(id)
    }

    private suspend fun launchProgressBarSpin(time: Long) {
        binding?.apply {
            progressBar.isVisible = true
            delay(time)
            progressBar.isVisible = false
        }
    }

    fun openSms(id: Int) = viewModel.onSendSmsClick(id, cxt)
    fun phoneCall(id: Int) = viewModel.onPhoneCallClick(id, cxt)
    fun openWhatsapp(id: Int) = viewModel.onOpenWhatsappClick(id, cxt)
    fun openMail(id: Int) = viewModel.onOpenMailClick(id, cxt)

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}