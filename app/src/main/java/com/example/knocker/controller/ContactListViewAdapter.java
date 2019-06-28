package com.example.knocker.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Objects;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static java.sql.DriverManager.println;

/**
 * La Classe qui permet de remplir la listview avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
public class ContactListViewAdapter extends BaseAdapter {
    private List<ContactWithAllInformation> listContacts;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;
    private ContactList gestionnaireContacts;
    private ViewHolder activeMenu;

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
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        @SuppressLint("ViewHolder") View listview = layoutInflater.inflate(R.layout.list_contact_item_layout, null);
        final ViewHolder holder;
        holder = new ViewHolder();
        holder.position = position;
        ContactDB contact = getItem(position).getContactDB();

        holder.contactRoundedImageView = listview.findViewById(R.id.list_contact_item_contactRoundedImageView);

        assert contact != null;
        if (contact.getContactPriority() == 0) {
            holder.contactRoundedImageView.setBetweenBorderColor(context.getResources().getColor(R.color.priorityOneColor));
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZeroColor));
        } else if (contact.getContactPriority() == 1) {
            holder.contactRoundedImageView.setBetweenBorderColor(context.getResources().getColor(R.color.priorityOneColor));
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityOneColor));
        } else if (contact.getContactPriority() == 2) {
            holder.contactRoundedImageView.setBetweenBorderColor(context.getResources().getColor(R.color.priorityOneColor));
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwoColor));
        }

        holder.contactFirstNameView = listview.findViewById(R.id.list_contact_item_contactFirstName);
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            System.out.println(contact.getProfilePicture());
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture(), "Get"));
            //holder.contactRoundedImageView.setBackgroundColor(context.getResources().getColor(R.color.black));
        }
        String contactName = contact.getFirstName() + " " + contact.getLastName();
        if (contactName.length() > 27) {
            contactName = contact.getFirstName() + " " + contact.getLastName();
            contactName = contactName.substring(0, 25) + "..";
        }

        holder.contactFirstNameView.setText(contactName);
        holder.constraintLayout = listview.findViewById(R.id.list_contact_item_layout);
        holder.constraintLayoutMenu = listview.findViewById(R.id.list_contact_item_menu);
        holder.callCl = listview.findViewById(R.id.list_contact_item_constraint_call);
        holder.smsCl = listview.findViewById(R.id.list_contact_item_constraint_sms);
        holder.whatsappCl = listview.findViewById(R.id.list_contact_item_constraint_whatsapp);
        holder.mailCl = listview.findViewById(R.id.list_contact_item_constraint_mail);
        holder.editCl = listview.findViewById(R.id.list_contact_item_constraint_edit);

        if (activeMenu != null) {
            if (holder.position == activeMenu.position) {
                holder.constraintLayoutMenu.setVisibility(View.VISIBLE);
            }
        }


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getId() == holder.smsCl.getId()) {

                    String phone = getItem(position).getPhoneNumber();
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null));
                    context.startActivity(i);
                }
                if (v.getId() == holder.callCl.getId()) {
                    callPhone(getItem(position).getPhoneNumber());
                }
                if (v.getId() == holder.whatsappCl.getId()) {
                    ContactWithAllInformation contactWithAllInformation = getItem(position);
                    ContactGesture.INSTANCE.openWhatsapp(converter06To33(contactWithAllInformation.getPhoneNumber()), context);
                }
                if (v.getId() == holder.editCl.getId()) {

                    System.out.println("edit");
                    Intent intent = new Intent(context, EditContactActivity.class);
                    intent.putExtra("ContactId", getItem(position).getContactDB().getId());
                    context.startActivity(intent);
                }
                if (v.getId() == holder.mailCl.getId()) {
                    String mail = getItem(position).getFirstMail();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("mailto:"));
                    intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail.substring(0, mail.length() - 1)});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, "");
                    println("intent " + Objects.requireNonNull(intent.getExtras()).toString());
                    context.startActivity(Intent.createChooser(intent, "envoyer un mail à " + mail.substring(0, mail.length() - 1)));
                }
                System.out.println("click position " + position);

                if (holder.constraintLayoutMenu.getVisibility() == View.GONE) {
                    holder.constraintLayoutMenu.setVisibility(View.VISIBLE);
                    if (activeMenu != null) {
                        activeMenu.constraintLayoutMenu.setVisibility(View.GONE);
                    }
                    activeMenu = holder;
                    println("active menu" + activeMenu.toString());
                } else {
                    println("menu active");
                    holder.constraintLayoutMenu.setVisibility(View.GONE);
                    activeMenu = null;

                    Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                    holder.constraintLayoutMenu.startAnimation(slideDown);
                }
            }
        };

        if (!whatsappIsInstalled()) {
            holder.whatsappCl.setVisibility(View.GONE);
        } else {
            holder.whatsappCl.setVisibility(View.VISIBLE);
        }

        if (getItem(position).getFirstMail().isEmpty()) {
            holder.mailCl.setVisibility(View.GONE);
        } else {
            holder.mailCl.setVisibility(View.VISIBLE);
        }

        holder.mailCl.setOnClickListener(listener);
        holder.whatsappCl.setOnClickListener(listener);
        holder.callCl.setOnClickListener(listener);
        holder.editCl.setOnClickListener(listener);
        holder.smsCl.setOnClickListener(listener);

        holder.constraintLayout.setOnClickListener(listener);

        return listview;
    }

    private String converter06To33(String phoneNumber) {
        if (phoneNumber.charAt(0) == '0') {
            return "+33" + phoneNumber;
        }
        return phoneNumber;
    }

    private int randomDefaultImage(int avatarId, String createOrGet) {
        if (createOrGet.equals("Create")) {
            return new Random().nextInt(7);
        } else if (createOrGet.equals("Get")) {
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

    private void callPhone(final String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            //Intent intent=new Intent(Intent.ACTION_CALL);
            //intent.setData(Uri.parse(getItem(position).getPhoneNumber()));
            SharedPreferences sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE);
            Boolean popup = sharedPreferences.getBoolean("popup", true);
            if (popup) {
                new AlertDialog.Builder(context)
                        .setTitle("Voulez-vous appeler ce contact ?")
                        .setMessage("Vous pouvez désactiver cette validation depuis les options")
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
    }//code duplicate à mettre dans contactAllInfo

    private boolean whatsappIsInstalled() {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo("com.whatsapp", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        ConstraintLayout constraintLayout;
        ConstraintLayout constraintLayoutMenu;
        CircularImageView contactRoundedImageView;
        int position;

        ConstraintLayout callCl;
        ConstraintLayout smsCl;
        ConstraintLayout whatsappCl;
        ConstraintLayout mailCl;
        ConstraintLayout editCl;
    }
}