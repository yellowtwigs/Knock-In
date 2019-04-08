package com.example.firsttestknocker;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private List<Contact> listContact;
    private LayoutInflater layoutInflater;
    private Context context;

    public ContactAdapter(Context context, List<Contact> listContact) {
        this.context = context;
        this.listContact = listContact;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listContact.size();
    }

    @Override
    public Object getItem(int position) {
        return listContact.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridview = convertView;
        ViewHolder holder;

        if (gridview == null) {
            gridview = layoutInflater.inflate(R.layout.grid_item_layout, null);

            holder = new ViewHolder();
            holder.contactRoundedImageView = gridview.findViewById(R.id.contactRoundedImageView);
            holder.contactFirstNameView = gridview.findViewById(R.id.contactFirstName);

            gridview.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Contact contact = this.listContact.get(position);
        holder.contactFirstNameView.setText(contact.getContactFirstName());
        holder.contactRoundedImageView.setImageResource(contact.getContactImage());

        return gridview;
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        TextView contactPhoneNumberView;
        RoundedImageView contactRoundedImageView;
    }
}
