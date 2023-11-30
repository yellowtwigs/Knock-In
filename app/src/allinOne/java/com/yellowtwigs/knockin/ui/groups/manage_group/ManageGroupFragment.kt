package com.yellowtwigs.knockin.ui.groups.manage_group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityManageGroupBinding
import com.yellowtwigs.knockin.databinding.FragmentManageGroupBinding
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ManageGroupViewState
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageGroupFragment : Fragment(R.layout.fragment_manage_group) {

    private val binding by viewBinding(FragmentManageGroupBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        intent.getIntegerArrayListExtra("contacts")?.forEach { id ->
//            id?.let {
//                listOfItemSelected.add(it.toString())
//            }
//        }

//        setupToolbar()
    }
}