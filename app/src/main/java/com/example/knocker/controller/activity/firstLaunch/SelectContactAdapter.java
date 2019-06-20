package com.example.knocker.controller.activity.firstLaunch;

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
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.knocker.R;
import com.example.knocker.controller.CircularImageView;
import com.example.knocker.model.ContactList;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.ArrayList;
import java.util.Random;

public class SelectContactAdapter extends BaseAdapter {

    private ContactList gestionnaireContact;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;
    private ArrayList<ContactWithAllInformation> listSelectedItem;

    public SelectContactAdapter(Context context, ContactList contactList, Integer len) {
        this.context = context;
        this.gestionnaireContact = contactList;
        this.len = len;
        layoutInflater = LayoutInflater.from(context);
        listSelectedItem = new ArrayList<ContactWithAllInformation>();
    }

    @Override
    public int getCount() {
        return gestionnaireContact.getContacts().size();
    }

    @Override
    public ContactWithAllInformation getItem(int position) {
        return gestionnaireContact.getContacts().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("into view position " + position);
        View gridview = convertView;
        final ViewHolder holder;

        if (gridview == null) {
            System.out.println("convert is null");
            gridview = layoutInflater.inflate(R.layout.grid_contact_item_layout, null);
//            gridview = layoutInflater.inflate(R.layout.list_contact_item_layout, null);


            holder = new ViewHolder();
            holder.contactRoundedImageView = gridview.findViewById(R.id.contactRoundedImageView);
//            holder.whatsapp_click_bubbles = gridview.findViewById(R.id.whatsapp_click_bubbles);
//            holder.messenger_click_bubbles = gridview.findViewById(R.id.messenger_click_bubbles);
//            holder.phone_call_click_bubbles = gridview.findViewById(R.id.phone_call_click_bubbles);
//            holder.sms_click_bubbles = gridview.findViewById(R.id.sms_click_bubbles);

            SharedPreferences sharedPreferences = context.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE);


            int len = sharedPreferences.getInt("gridview", 4);
            int height = holder.contactRoundedImageView.getLayoutParams().height;
            int width = holder.contactRoundedImageView.getLayoutParams().width;

            holder.contactFirstNameView = gridview.findViewById(R.id.grid_adapter_contactFirstName);
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
            holder.contactLastNameView = gridview.findViewById(R.id.grid_adapter_contactLastName);

            gridview.setTag(holder);
        } else {
            System.out.println("convert is  not null");
            holder = (ViewHolder) convertView.getTag();
        }
        holder.isSelect = false;

        final ContactDB contact = getItem(position).getContactDB();

        String firstname = contact.getFirstName();
        String lastName = contact.getLastName();
        if (len == 3) {
            holder.contactFirstNameView.setText(firstname);
            holder.contactLastNameView.setText(lastName);
            //holder.contactFirstNameView.;
        }
        if (len == 4) {
            if (contact.getFirstName().length() > 12)
                firstname = contact.getFirstName().substring(0, 10).concat("..");

            Spannable spanFistName = new SpannableString(firstname);
            spanFistName.setSpan(new RelativeSizeSpan(0.9f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);
            if (contact.getLastName().length() > 12)
                lastName = contact.getLastName().substring(0, 10).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.9f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
        }
        if (len == 5) {
            if (contact.getFirstName().length() > 11)
                firstname = contact.getFirstName().substring(0, 9).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.8f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            if (contact.getLastName().length() > 11)
                lastName = contact.getLastName().substring(0, 9).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.8f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
        }
        if (len == 6) {
            if (contact.getFirstName().length() > 10)
                firstname = contact.getFirstName().substring(0, 8).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.71f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            if (contact.getLastName().length() > 10)
                lastName = contact.getLastName().substring(0, 8).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.71f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
        }
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());

            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture(), "Get")); //////////////
        }
        if (listSelectedItem.contains(getItem(position))) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwoColor));
        } else {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.whiteColor));
        }
        return gridview;
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        CircularImageView contactRoundedImageView;
        Boolean isSelect;
    }


    private Bitmap base64ToBitmap(String base64) {

        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    public void itemSelected(int position) {

        ContactWithAllInformation contact = getItem(position);
        if (listSelectedItem.contains(contact)) {
            listSelectedItem.remove(contact);
        } else {
            listSelectedItem.add(contact);
        }
    }

    public ArrayList<ContactWithAllInformation> listContactSelect() {

        return listSelectedItem;
    }

    public int randomDefaultImage(int avatarId, String createOrGet) {
        if (createOrGet.equals("Create")) {
            return new Random().nextInt(7);
        } else if (createOrGet.equals("Get")) {
            switch(avatarId) {
                case 0: return R.drawable.ic_user_purple;
                case 1: return R.drawable.ic_user_blue;
                case 2: return R.drawable.ic_user_brown;
                case 3: return R.drawable.ic_user_green;
                case 4: return R.drawable.ic_user_om;
                case 5: return R.drawable.ic_user_orange;
                case 6: return R.drawable.ic_user_pink;
            }
        }
        return -1;
    }
}

