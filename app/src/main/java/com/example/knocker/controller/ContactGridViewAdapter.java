package com.example.knocker.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.R;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ContactGridViewAdapter extends BaseAdapter {
    private List<ContactWithAllInformation> listContacts;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;

    public ContactGridViewAdapter(Context context, List<ContactWithAllInformation> listContacts, Integer len) {
        this.context = context;
        this.listContacts = listContacts;
        this.len = len;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return listContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridview = convertView;
        ViewHolder holder;

        if (gridview == null) {
            gridview = layoutInflater.inflate(R.layout.grid_contact_item_layout, null);
//            gridview = layoutInflater.inflate(R.layout.list_contact_item_layout, null);


            holder = new ViewHolder();
            holder.contactRoundedImageView = gridview.findViewById(R.id.contactRoundedImageView);
//            holder.whatsapp_click_bubbles = gridview.findViewById(R.id.whatsapp_click_bubbles);
//            holder.messenger_click_bubbles = gridview.findViewById(R.id.messenger_click_bubbles);
//            holder.phone_call_click_bubbles = gridview.findViewById(R.id.phone_call_click_bubbles);
//            holder.sms_click_bubbles = gridview.findViewById(R.id.sms_click_bubbles);

            SharedPreferences sharedPreferences = context.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE);


            int len = sharedPreferences.getInt("gridview", 3);
            int height = holder.contactRoundedImageView.getLayoutParams().height;
            int width = holder.contactRoundedImageView.getLayoutParams().width;

            holder.contactFirstNameView = gridview.findViewById(R.id.contactFirstName);
            ConstraintLayout.LayoutParams layoutParamsTV = (ConstraintLayout.LayoutParams) holder.contactFirstNameView.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParamsIV = (ConstraintLayout.LayoutParams) holder.contactRoundedImageView.getLayoutParams();

            if (len == 3) {
                holder.contactRoundedImageView.getLayoutParams().height -= height * 0.05;
                holder.contactRoundedImageView.getLayoutParams().width -= height * 0.05;
                layoutParamsTV.topMargin = 30;
                layoutParamsIV.topMargin = 10;
            } else if (len == 4) {
                holder.contactRoundedImageView.getLayoutParams().height -= height * 0.15;
                holder.contactRoundedImageView.getLayoutParams().width -= width * 0.15;
                layoutParamsTV.topMargin = 10;
                layoutParamsIV.topMargin = 10;
            } else if (len == 5 || len == 6) {
                holder.contactRoundedImageView.getLayoutParams().height -= height * 0.50; //175
                holder.contactRoundedImageView.getLayoutParams().width -= width * 0.50;
                layoutParamsTV.topMargin = 0;
                layoutParamsIV.topMargin = 0;
            }
            holder.contactFirstNameView = gridview.findViewById(R.id.contactFirstName);

            gridview.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ContactDB contact = this.listContacts.get(position).getContactDB();
        if (contact.getContactPriority() == 0) {
//            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZero));
        } else if (contact.getContactPriority() == 1) {
//            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityOne));
        } else if (contact.getContactPriority() == 2) {
//            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwo));
        }
        String firstname = contact.getFirstName();
        if (len == 3) {
            holder.contactFirstNameView.setText(firstname);
            //holder.contactFirstNameView.;
        }
        if (len == 4) {
            if (contact.getFirstName().length() > 10)
                firstname = contact.getFirstName().substring(0, 10).concat("..");
            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.9f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
        }
        if (len == 5) {
            if (contact.getFirstName().length() > 9)
                firstname = contact.getFirstName().substring(0, 9).concat("..");
            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.8f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.knockerColorPrimary));
        }
        if (len == 6) {
            if (contact.getFirstName().length() > 8)
                firstname = contact.getFirstName().substring(0, 8).concat("..");
            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.71f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
        }
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(contact.getProfilePicture());
            //holder.contactRoundedImageView.setBackgroundColor(context.getResources().getColor(R.color.black));
        }
        return gridview;
    }

    private Bitmap base64ToBitmap(String base64) {

        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        ImageView contactRoundedImageView;
    }
}
