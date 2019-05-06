package com.example.firsttestknocker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private List<Contacts> listContacts;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;

    public ContactAdapter(Context context, List<Contacts> listContacts, Integer len) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridview = convertView;
        ViewHolder holder;

        if (gridview == null) {
            gridview = layoutInflater.inflate(R.layout.grid_item_layout, null);

            holder = new ViewHolder();
            holder.contactRoundedImageView = gridview.findViewById(R.id.contactRoundedImageView);
            SharedPreferences sharedPreferences = context.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE);
            int len = sharedPreferences.getInt("gridview",3);
            int height = holder.contactRoundedImageView.getLayoutParams().height;
            int width = holder.contactRoundedImageView.getLayoutParams().width;
            if (len == 3) {
                holder.contactRoundedImageView.getLayoutParams().height -= height*0.05;
                holder.contactRoundedImageView.getLayoutParams().width -= height*0.05;
            } else if (len == 4) {
                holder.contactRoundedImageView.getLayoutParams().height -= height*0.15;
                holder.contactRoundedImageView.getLayoutParams().width -= width*0.15;
            } else if (len == 5 || len == 6) {
                holder.contactRoundedImageView.getLayoutParams().height -= height*0.50; //175
                holder.contactRoundedImageView.getLayoutParams().width -= width*0.50;
            }
            holder.contactFirstNameView = gridview.findViewById(R.id.contactFirstName);

            gridview.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Contacts contact = this.listContacts.get(position);
        String firstname = contact.getFirstName();
        if (len == 4 && contact.getFirstName().length() > 10) {
            firstname = contact.getFirstName().substring(0,10);
        }
        if (len == 5 && contact.getFirstName().length() > 7) {
            firstname = contact.getFirstName().substring(0,7);
        }
        if (len == 6 && contact.getFirstName().length() > 5) {
            firstname = contact.getFirstName().substring(0,5);
        }
        holder.contactFirstNameView.setText(firstname);
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(contact.getProfilePicture());
        }
        return gridview;
    }

    public Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString,0, decodedString.length, options);
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        TextView contactPhoneNumberView;
        ImageView contactRoundedImageView;
    }
}
