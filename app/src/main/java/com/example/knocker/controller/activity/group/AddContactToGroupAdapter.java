package com.example.knocker.controller.activity.group;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.knocker.R;
import com.example.knocker.controller.CircularImageView;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AddContactToGroupAdapter extends BaseAdapter {

    private List<ContactWithAllInformation> listContacts;
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<ContactDB> selectContact;

    public AddContactToGroupAdapter(Context context, List<ContactWithAllInformation> listContacts) {
        this.context = context;
        this.listContacts = listContacts;
        layoutInflater = LayoutInflater.from(context);
        selectContact = new ArrayList<ContactDB>();
    }

    @Override
    public int getCount() { return listContacts.size(); }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public ContactWithAllInformation getItem(int position) {
        return listContacts.get(position);
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View listview;
        final ViewHolder holder;
        holder = new ViewHolder();
        holder.position = position;
        final ContactDB contact = getItem(position).getContactDB();
        listview = layoutInflater.inflate(R.layout.list_contact_selected_group, null);

        holder.contactRoundedImageView = listview.findViewById(R.id.add_group_contact_list_item_contactRoundedImageView);
        holder.contactFirstNameView = listview.findViewById(R.id.add_group_contact_list_item_contactFirstName);
        holder.contactSelect = listview.findViewById(R.id.add_group_contact_checkbox);

        assert contact != null;
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            System.out.println(contact.getProfilePicture());
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
        }
        String contactName = contact.getFirstName() + " " + contact.getLastName();
        if (contactName.length() > 15) {

            Spannable spanFistName = new SpannableString(contactName);
            spanFistName.setSpan(new RelativeSizeSpan(1.0f), 0, contactName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);

            contactName = contact.getFirstName() + " " + contact.getLastName();
            contactName = contactName.substring(0, 15) + "..";
        }
        holder.contactFirstNameView.setText(contactName);
        holder.contactSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked)
                    selectContact.add(contact);
                else
                    selectContact.remove(contact);
            }
        });
        if(selectContact.contains(contact)){
            holder.contactSelect.setChecked(true);
        }else{
            holder.contactSelect.setChecked(false);
        }
        return listview;
    }

    public List<ContactDB> getAllSelectContact() { return selectContact; }

    private int randomDefaultImage(int avatarId) {
            switch (avatarId) {
                case 0:
                    return R.drawable.ic_user_purple;
                case 1:
                    return R.drawable.ic_user_blue;
                case 2:
                    return R.drawable.ic_user_knocker;
                case 3:
                    return R.drawable.ic_user_green;
                case 4:
                    return R.drawable.ic_user_om;
                case 5:
                    return R.drawable.ic_user_orange;
                case 6:
                    return R.drawable.ic_user_pink;
                default:
                    return R.drawable.ic_user_blue;
            }
    }

    private Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        CircularImageView contactRoundedImageView;
        int position;
        CheckBox contactSelect;
    }
}
