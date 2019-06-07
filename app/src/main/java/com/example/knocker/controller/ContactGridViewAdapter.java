package com.example.knocker.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.knocker.controller.activity.EditContactActivity;
import com.example.knocker.model.ContactGesture;
import com.example.knocker.model.ContactList;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.R;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static java.sql.DriverManager.println;

/**
 * La Classe qui permet de remplir la gridview avec les bon éléments
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
public class ContactGridViewAdapter extends BaseAdapter implements FloatingActionMenu.MenuStateChangeListener {
    private ContactList gestionnaireContact;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;
    private ArrayList<FloatingActionMenu> listCircularMenu= new ArrayList<FloatingActionMenu>();
    private FloatingActionMenu selectMenu;

    public ContactGridViewAdapter(Context context, ContactList contactList, Integer len) {
        this.context = context;
        this.gestionnaireContact= contactList;
        this.len = len;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setGestionnairecontact(ContactList gestionnaireContact){
        gestionnaireContact= gestionnaireContact;
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
            holder = (ViewHolder) convertView.getTag();
        }

        final ContactDB contact = this.gestionnaireContact.getContacts().get(position).getContactDB();
        if (contact.getContactPriority() == 0) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZero));
        } else if (contact.getContactPriority() == 1) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityOne));
        } else if (contact.getContactPriority() == 2) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwo));
        }
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
            if(contact.getLastName().length()>12)
                lastName =contact.getLastName().substring(0,10).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.9f),0,lastName.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
        }
        if (len == 5) {
            if (contact.getFirstName().length() > 11)
                firstname = contact.getFirstName().substring(0, 9).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.8f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.knockerColorPrimary));
            if(contact.getLastName().length()>11)
                lastName =contact.getLastName().substring(0,9).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.8f),0,lastName.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
        }
        if (len == 6) {
            if (contact.getFirstName().length() > 10)
                firstname = contact.getFirstName().substring(0, 8).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.71f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            if(contact.getLastName().length()>10)
                lastName =contact.getLastName().substring(0,8).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.71f),0,lastName.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
        }
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());

            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture(), "Get")); //////////////
            //holder.contactRoundedImageView.setBackgroundColor(context.getResources().getColor(R.color.black));
        }
        //region circular menu

        final ImageView buttonMessenger= new ImageView(context);
        final ImageView buttonCall= new ImageView(context);
        final ImageView buttonWhatsApp = new ImageView(context);
        final ImageView buttonSMS= new ImageView(context);
        final ImageView buttonEdit = new ImageView(context);
        final ImageView buttonMail = new ImageView(context);

        buttonMessenger.setId(0);
        buttonCall.setId(1);
        buttonSMS.setId(2);
        buttonWhatsApp.setId(3);
        buttonEdit.setId(4);
        buttonMail.setId(5);




        buttonMessenger.setImageResource(R.drawable.ic_messenger);
        buttonCall.setImageResource( R.drawable.ic_phone_call);
        buttonWhatsApp.setImageResource(R.drawable.ic_whatsapp);
        buttonSMS.setImageResource(R.drawable.ic_sms);
        buttonEdit.setImageResource(R.drawable.ic_edit_floating_button);
        buttonMail.setImageResource(android.R.drawable.ic_dialog_email);



        SubActionButton.Builder builderIcon= new SubActionButton.Builder((Activity) context);
        builderIcon.setBackgroundDrawable(context.getDrawable(R.drawable.knocker_circle));
        builderIcon.setContentView(buttonCall);
        int startAngle;
        int endAngle;
        if(position%len==0){
            System.out.println("position vaut "+ position+" modulo vaut" +position%len);
            startAngle=90;
            endAngle=-90;
        }else if(position%len==len-1){
            System.out.println("position vaut "+ position+" modulo vaut" +position%len);
            startAngle=90;
            endAngle=270;
        }else{
            System.out.println("position vaut "+ position+" modulo vaut" +position%len);
            startAngle=0;
            endAngle=-180;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int diametreBoutton =(int) (0.30*metrics.densityDpi);
        int radiusMenu = (int) (0.40*metrics.densityDpi);
        int border =  (int) (0.0625*metrics.densityDpi);

        FrameLayout.LayoutParams layoutParams= new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(border,border,border,border);

        FloatingActionMenu.Builder builder = new FloatingActionMenu.Builder((Activity) context)
                .setStartAngle(startAngle)
                .setEndAngle(endAngle)
                .setRadius(radiusMenu)
                .addSubActionView(builderIcon.setContentView(buttonEdit,layoutParams).build(),diametreBoutton,diametreBoutton)
                .attachTo(holder.contactRoundedImageView)
                .setStateChangeListener(this)
                .disableAnimations();
        if(appIsInstalled("com.whatsapp")&& getItem(position).getPhoneNumber()!=""){
            builder.addSubActionView(builderIcon.setContentView(buttonWhatsApp,layoutParams).build(),diametreBoutton,diametreBoutton);
        }
        if(getItem(position).getFirstMail()!=""){
            builder.addSubActionView(builderIcon.setContentView(buttonMail,layoutParams).build(),diametreBoutton,diametreBoutton);
        }
        if(getItem(position).getPhoneNumber()!=""){
            builder.addSubActionView(builderIcon.setContentView(buttonSMS,layoutParams).build(),diametreBoutton,diametreBoutton)
                    .addSubActionView(builderIcon.setContentView(buttonCall,layoutParams).build(),diametreBoutton,diametreBoutton);
        }


       /* if( appIsInstalled( "com.facebook.orca")){
            builder.addSubActionView(builderIcon.setContentView(buttonMessenger,layoutParams).build(),diametreBoutton,diametreBoutton);
        }*/




        FloatingActionMenu quickMenu=builder.build();
        listCircularMenu.add(quickMenu);
              //  quickMenu.addSubActionView(builderIcon.setContentView(buttonSMS,layoutParams).build(),diametreBoutton,diametreBoutton)
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
                    ContactGesture.INSTANCE.openWhatsapp(contactphone.getPhoneNumber(),context);
                }else if(v.getId()==buttonEdit.getId()){

                    Intent intent = new Intent(context, EditContactActivity.class);
                    intent.putExtra("ContactId",contact.getId());
                    context.startActivity(intent);
                }else if(v.getId()==buttonCall.getId()){

                    callPhone(getItem(position).getPhoneNumber());

                }else if(v.getId()==buttonSMS.getId()){
                    String phone = getItem(position).getPhoneNumber();
                    Intent i = new Intent(Intent.ACTION_VIEW,Uri.fromParts("sms",phone,null));
                    context.startActivity(i);
                }else if(v.getId()==buttonMail.getId()){
                    String mail = getItem(position).getFirstMail();
                    Intent intent=new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("mailto:"));
                    intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail.substring(0, mail.length()-1)});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT,"");
                    println("intent "+ intent.getExtras().toString());
                    context.startActivity(Intent.createChooser(intent,"envoyer un mail à "+ mail.substring(0, mail.length()-1)));
                }
                selectMenu.close(true);
            }
        };
        buttonMessenger.setOnClickListener(buttonListener);
        buttonWhatsApp.setOnClickListener(buttonListener);
        buttonCall.setOnClickListener(buttonListener);
        buttonSMS.setOnClickListener(buttonListener);
        buttonEdit.setOnClickListener(buttonListener);
        buttonMail.setOnClickListener(buttonListener);
        //endregion
        return gridview;

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
//    fun randomDefaultImage(avatarId:Int, createOrGet:String): Int {
//
//        if (createOrGet == "Create") {
//            return kotlin.random.Random.nextInt(0, 11)
//        } else if (createOrGet == "Get") {
//            when (avatarId) {
//                0 -> return R.drawable.ic_user_yellow
//                1 -> return R.drawable.ic_user_blue
//                2 -> return R.drawable.ic_user_brown
//                3 -> return R.drawable.ic_user_green
//                4 -> return R.drawable.ic_user_om
//                5 -> return R.drawable.ic_user_om
//                6 -> return R.drawable.ic_user_orange
//                7 -> return R.drawable.ic_user_pink
//                8 -> return R.drawable.ic_user_purple
//                9 -> return R.drawable.ic_user_red
//                10 -> return R.drawable.ic_user_yellow
//            }
//        } else if (createOrGet == "Both") {
//            val avatar = kotlin.random.Random.nextInt(0, 11)
//            return randomDefaultImage(avatar, "Get")
//        }
//        return -1
//    }

    private void callPhone(final String phoneNumber) {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.CALL_PHONE},1);
        }else{
            //Intent intent=new Intent(Intent.ACTION_CALL);
            //intent.setData(Uri.parse(getItem(position).getPhoneNumber()));
            SharedPreferences sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE);
            Boolean popup = sharedPreferences.getBoolean("popup", true);
            if (popup) {
                new AlertDialog.Builder(context)
                        .setTitle("Voulez vous appeler ce contact ?")
                        .setMessage("Vous pouvez désactiver cette validiation depuis les options")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                context.startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            } else {
                context.startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
            }
        }
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
        if(selectMenu!=null)
           selectMenu.close(true);

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
    public FloatingActionMenu getSelectMenu(){
        return selectMenu;
    }
    private boolean appIsInstalled(String appPackage){
        PackageManager pm =context.getPackageManager();
        try{
            pm.getApplicationInfo(appPackage,0);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
