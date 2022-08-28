package com.yellowtwigs.knockin.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemScrollBarLayoutBinding
import java.util.*


class ScrollBarAdapter() :
    ListAdapter<String, ScrollBarAdapter.ViewHolder>(StringComparator()), SectionIndexer {

    private val mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    private var sectionsTranslator: HashMap<Int, Int> = HashMap()
    private var mSectionPositions: ArrayList<Int>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemScrollBarLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ViewHolder(private val binding: ItemScrollBarLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(letter: String) {
            binding.tvAlphabet.text = letter
        }
    }

    class StringComparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem.equals(newItem)

        }
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }

    override fun getSections(): Array<Any> {
        val sections: MutableList<String> = ArrayList(27)
        val alphabetFull: ArrayList<String> = ArrayList()
        mSectionPositions = ArrayList()
        run {
            for((i, item) in currentList.withIndex()){
                val section = item.uppercase(Locale.getDefault())
                if (!sections.contains(section)) {
                    sections.add(section)
                    mSectionPositions!!.add(i)
                }
            }
        }
        for (element in mSections) {
            alphabetFull.add(element.toString())
        }
//        sectionsTranslator = Helpers.Companion.sectionsHelper(sections, alphabetFull)
        return alphabetFull.toArray(arrayOfNulls<String>(0))
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions?.get(sectionsTranslator[sectionIndex]!!)!!
    }

}