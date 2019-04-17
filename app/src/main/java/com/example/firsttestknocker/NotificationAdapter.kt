package com.example.firsttestknocker

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class NotificationAdapter : ListAdapter<StatusBarParcelable, NotificationAdapter.ItemViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NotificationAdapter.ItemViewHolder {
        return ItemViewHolder(
                LayoutInflater.from(p0.context).inflate(R.layout.activity_notification_adapter,p0,false)
        )
    }

    override fun onBindViewHolder(p0: NotificationAdapter.ItemViewHolder, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind (item: StatusBarParcelable) = with(itemView){

         setOnClickListener {

         }
        }
    }

}
class DiffCallback : DiffUtil.ItemCallback<StatusBarParcelable>(){
    override fun areItemsTheSame(oldItem: StatusBarParcelable, newItem: StatusBarParcelable): Boolean {
        return oldItem?.id==newItem?.id
    }

    override fun areContentsTheSame(oldItem: StatusBarParcelable, newItem: StatusBarParcelable): Boolean {
        return oldItem == newItem
    }
}