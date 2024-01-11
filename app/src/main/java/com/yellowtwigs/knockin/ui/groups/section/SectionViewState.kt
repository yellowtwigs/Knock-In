package com.yellowtwigs.knockin.ui.groups.section

import com.yellowtwigs.knockin.ui.groups.ContactInGroupViewState
import com.yellowtwigs.knockin.utils.EquatableCallback

data class SectionViewState(
    var id: Int,
    var title: String = "",
    var sectionColor: Int,
    var items: List<ContactInGroupViewState> = ArrayList(),
    var phoneNumbers: ArrayList<String> = ArrayList(),
    var emails: ArrayList<String> = arrayListOf(),
    val onClickedCallback: EquatableCallback,
) 