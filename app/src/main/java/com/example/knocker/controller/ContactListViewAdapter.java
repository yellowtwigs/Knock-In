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
import android.widget.TextView;

import com.example.knocker.R;
import com.example.knocker.model.ContactList;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.List;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * La Classe qui permet de remplir la listview avec les bon éléments
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
public class ContactListViewAdapter extends BaseAdapter {
    private List<ContactWithAllInformation> listContacts;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;
    private ContactList gestionnaireContacts;

    public ContactListViewAdapter(Context context, List<ContactWithAllInformation> listContacts, Integer len) {
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

        View listview = convertView;
        ViewHolder holder;

        holder = new ViewHolder();
        ContactDB contact= ((ContactWithAllInformation) getItem(position)).getContactDB();
        if (listview == null) {
            listview = layoutInflater.inflate(R.layout.list_contact_item_layout, null);

        }
        holder.contactRoundedImageView = listview.findViewById(R.id.list_adapter_contactRoundedImageView);
        holder.contactFirstNameView= listview.findViewById(R.id.list_adapter_contactFirstName);
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture(), "Get"));
            //holder.contactRoundedImageView.setBackgroundColor(context.getResources().getColor(R.color.black));
        }
        String contactName=contact.getFirstName()+" "+contact.getLastName();
        if(contactName.length()>19){
        contactName=contact.getFirstName()+" "+contact.getLastName();
        contactName=contactName.substring(0,17)+"..";
        }

        holder.contactFirstNameView.setText(contactName);
        return listview;
    }

    public int randomDefaultImage(int avatarId, String createOrGet) {
        if (createOrGet.equals("Create")) {
            return new Random().nextInt(9);
        } else if (createOrGet.equals("Get")) {
            switch(avatarId) {
                case 0: return R.drawable.ic_user_yellow;
                case 1: return R.drawable.ic_user_blue;
                case 2: return R.drawable.ic_user_brown;
                case 3: return R.drawable.ic_user_green;
                case 4: return R.drawable.ic_user_om;
                case 5: return R.drawable.ic_user_orange;
                case 6: return R.drawable.ic_user_pink;
                case 7: return R.drawable.ic_user_purple;
                case 8: return R.drawable.ic_user_red;
            }
        }
        return -1;
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
    }
}
