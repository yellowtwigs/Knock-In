package com.yellowtwigs.knockin.ui.groups.list.section

import com.yellowtwigs.knockin.ui.groups.list.GroupsListViewState

data class SectionViewState(
    var id: Int,
    var title: String = "",
    var items: ArrayList<GroupsListViewState> = ArrayList()
) {
}