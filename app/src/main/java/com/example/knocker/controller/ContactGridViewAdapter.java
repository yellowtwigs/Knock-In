package com.example.knocker.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.knocker.model.ContactGesture;
import com.example.knocker.model.ContactList;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.R;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;

import static java.sql.DriverManager.println;

/**
 * La Classe qui permet de remplir la gridview avec les bon éléments
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
public class ContactGridViewAdapter extends BaseAdapter implements FloatingActionMenu.MenuStateChangeListener {
    private List<ContactWithAllInformation> listContacts;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;
    private ArrayList<FloatingActionMenu> listCircularMenu= new ArrayList<FloatingActionMenu>();
    private FloatingActionMenu selectMenu;

    public ContactGridViewAdapter(Context context, ContactList contactList, Integer len) {
        this.context = context;
        this.listContacts = contactList.getContacts();
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

    @SuppressLint({"InflateParams", "ResourceType"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

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

        final ContactDB contact = this.listContacts.get(position).getContactDB();
        if (contact.getContactPriority() == 0) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZero));
        } else if (contact.getContactPriority() == 1) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityOne));
        } else if (contact.getContactPriority() == 2) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwo));
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
        //region circular menu
        final ImageView buttonMessenger= new ImageView(context);
        final ImageView buttonCall= new ImageView(context);
        final ImageView buttonWhatsApp = new ImageView(context);
        final ImageView buttonSMS= new ImageView(context);

        buttonMessenger.setId(0);
        buttonCall.setId(1);
        buttonSMS.setId(2);
        buttonWhatsApp.setId(3);
        LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(60,60);

        buttonMessenger.setLayoutParams(layoutParams);
        buttonCall.setLayoutParams(layoutParams);
        buttonSMS.setLayoutParams(layoutParams);
        buttonWhatsApp.setLayoutParams(layoutParams);

        buttonMessenger.setImageResource(R.drawable.ic_messenger);
        buttonCall.setImageResource( R.drawable.ic_phone_call);
        buttonWhatsApp.setImageResource(R.drawable.ic_whatsapp);
        buttonSMS.setImageResource(R.drawable.ic_sms);
        int distanceToCenter=120-(12*len);
        System.out.println("distance to center"+distanceToCenter);
        FloatingActionMenu quickMenu = new FloatingActionMenu.Builder((MainActivity) context)
                .setStartAngle(10)
                .setEndAngle(-180)
                .setRadius(110)
                .addSubActionView(buttonMessenger)
                .addSubActionView(buttonCall)
                .addSubActionView(buttonSMS)
                .addSubActionView(buttonWhatsApp)
                .attachTo(holder.contactRoundedImageView)
                .setStateChangeListener(this)
                .disableAnimations()
                .build();
        listCircularMenu.add(quickMenu);


        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(v.getId()==buttonMessenger.getId()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" +""));
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + ""));//id facebook into double quote
                        context.startActivity(intent);
                    }
                }else if (v.getId()==buttonWhatsApp.getId()){
                    System.out.println("================whatsapp=============");
                    ContactWithAllInformation contactphone=(ContactWithAllInformation) getItem(position);
                }
            }
        };
        buttonMessenger.setOnClickListener(buttonListener);
        buttonWhatsApp.setOnClickListener(buttonListener);
        buttonCall.setOnClickListener(buttonListener);
        buttonSMS.setOnClickListener(buttonListener);
        //endregion
        return gridview;

    }

    private Bitmap base64ToBitmap(String base64) {

        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    @Override
    public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
        if(selectMenu!=null) {
            selectMenu.close(true);
        }
        selectMenu=floatingActionMenu;

    }

    @Override
    public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
        selectMenu=null;
    }


    public void closeMenu(){
        for(FloatingActionMenu menu:listCircularMenu){

            menu.close(true);
        }
    }
    public void onScroll(){
        if(selectMenu!=null)
            selectMenu.updateItemPositions();
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        CircularImageView contactRoundedImageView;
    }
}
