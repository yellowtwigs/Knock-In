package com.yellowtwigs.knockin.ui.fragments.edit_contact

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.FragmentVipSettingsBinding
import com.yellowtwigs.knockin.ui.fragments.BaseFragmentEditContact

class VipSettingsFragment : BaseFragmentEditContact() {

    private var binding: FragmentVipSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVipSettingsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}