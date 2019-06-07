package com.example.knocker.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.Visibility;
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

import com.example.knocker.R;
import com.example.knocker.controller.activity.EditContactActivity;
import com.example.knocker.model.ContactGesture;
import com.example.knocker.model.ContactList;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.List;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static java.sql.DriverManager.println;

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
    private ConstraintLayout activeMenu;

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
    public ContactWithAllInformation getItem(int position) {
        return listContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View listview = convertView;
        final ViewHolder holder;

        holder = new ViewHolder();
        ContactDB contact=  getItem(position).getContactDB();
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
        holder.constraintLayout= listview.findViewById(R.id.list_adapter_constraint_layout);
        holder.constraintLayoutMenu=listview.findViewById(R.id.list_adapter_constraint_menu);
        holder.callCl=listview.findViewById(R.id.list_adapter_constraint_call);
        holder.smsCl=listview.findViewById(R.id.list_adapter_constraint_sms);
        holder.whatsappCl=listview.findViewById(R.id.list_adapter_constraint_whatsapp);
        holder.mailCl=listview.findViewById(R.id.list_adapter_constraint_mail);
        holder.editCl=listview.findViewById(R.id.list_adapter_constraint_edit);

        View.OnClickListener listener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(v.getId()==holder.smsCl.getId()){

                    String phone = getItem(position).getPhoneNumber();
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",phone,null));
                    context.startActivity(i);
                }
                if (v.getId()==holder.callCl.getId()){
                    callPhone(getItem(position).getPhoneNumber());
                }
                if (v.getId()== holder.whatsappCl.getId()){
                    ContactWithAllInformation contactphone=(ContactWithAllInformation) getItem(position);
                    ContactGesture.INSTANCE.openWhatsapp(contactphone.getPhoneNumber(),context);
                }
                if(v.getId() == holder.editCl.getId()){

                    System.out.println("edit");
                    Intent intent = new Intent(context, EditContactActivity.class);
                    intent.putExtra("ContactId",getItem(position).getContactDB().getId());
                    context.startActivity(intent);
                }
                if(v.getId() == holder.mailCl.getId()){
                    String mail = getItem(position).getFirstMail();
                    if(mail!=null) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("mailto:"));
                        intent.setType("text/html");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail.substring(0, mail.length() - 1)});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        intent.putExtra(Intent.EXTRA_TEXT, "");
                        println("intent " + intent.getExtras().toString());
                        context.startActivity(Intent.createChooser(intent, "envoyer un mail à " + mail.substring(0, mail.length() - 1)));
                    }
                }
                System.out.println("click");
                if(holder.constraintLayoutMenu.getVisibility()== View.GONE)
                {
                    holder.constraintLayoutMenu.setVisibility(View.VISIBLE);
                    if(activeMenu!=null){
                        activeMenu.setVisibility(View.GONE);
                    }
                    activeMenu=holder.constraintLayoutMenu;

                }else{
                    holder.constraintLayoutMenu.setVisibility(View.GONE);
                    activeMenu=null;
                }
            }
        };
        holder.mailCl.setOnClickListener(listener);
        holder.whatsappCl.setOnClickListener(listener);
        holder.callCl.setOnClickListener(listener);
        holder.editCl.setOnClickListener(listener);
        holder.smsCl.setOnClickListener(listener);

        holder.constraintLayout.setOnClickListener(listener);

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
    private void callPhone(String phoneNumber) {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.CALL_PHONE},1);
        }else{
            //Intent intent=new Intent(Intent.ACTION_CALL);
            //intent.setData(Uri.parse(getItem(position).getPhoneNumber()));
            context.startActivity(new Intent(Intent.ACTION_CALL ,Uri.fromParts("tel",phoneNumber,null)));
        }
    }//code duplicate à mettre dans contactAllInfo
    static class ViewHolder {
        TextView contactFirstNameView;
        ConstraintLayout constraintLayout;
        ConstraintLayout constraintLayoutMenu;
        CircularImageView contactRoundedImageView;

        ConstraintLayout callCl;
        ConstraintLayout smsCl;
        ConstraintLayout whatsappCl;
        ConstraintLayout mailCl;
        ConstraintLayout editCl;

    }
}
