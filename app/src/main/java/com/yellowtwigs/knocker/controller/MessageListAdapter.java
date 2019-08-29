package com.yellowtwigs.knocker.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.yellowtwigs.knocker.R;
import com.yellowtwigs.knocker.model.Message;

import java.util.ArrayList;

public class MessageListAdapter extends BaseAdapter {

    private ArrayList<Message> listMessages;
    private Context mContext;

    public MessageListAdapter(Context context, ArrayList<Message> listMessages) {
        this.mContext = context;
        this.listMessages = listMessages;
    }

    public void add(Message msg) {
        this.listMessages.add(msg);
        notifyDataSetChanged();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return listMessages.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return listMessages.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message msg = listMessages.get(position);

        if (msg.getFromMe()) {
            convertView = messageInflater.inflate(R.layout.item_message_sent, null);

            holder.messageContent = convertView.findViewById(R.id.item_message_sent_content);
            holder.date = convertView.findViewById(R.id.item_message_sent_date);
            holder.hour = convertView.findViewById(R.id.item_message_sent_hour);

            convertView.setTag(holder);

            holder.messageContent.setText(msg.getMessageContent());
            holder.date.setText(msg.getDate());
            holder.hour.setText(msg.getHour());
        } else {
            convertView = messageInflater.inflate(R.layout.item_message_received, null);

            holder.messageContent = convertView.findViewById(R.id.item_message_received_content);
            holder.date = convertView.findViewById(R.id.item_message_received_date);
            holder.hour = convertView.findViewById(R.id.item_message_received_hour);
            holder.nameSender = convertView.findViewById(R.id.item_message_received_sender_name);
            holder.profilePicture = convertView.findViewById(R.id.item_message_received_profile_image);

            convertView.setTag(holder);

            // Condition if the number is existing in the data base
            holder.nameSender.setText(msg.getNumber());
            holder.messageContent.setText(msg.getMessageContent());
            holder.date.setText(msg.getDate());
            holder.hour.setText(msg.getHour());
//            holder.profilePicture.setImageDrawable();
        }
        return convertView;
    }

    class MessageViewHolder {
        TextView nameSender;
        TextView messageContent;
        ImageView profilePicture;
        TextView date;
        TextView hour;
    }
}
