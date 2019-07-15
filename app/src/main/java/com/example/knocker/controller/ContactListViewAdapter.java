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
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.knocker.R;
import com.example.knocker.controller.activity.EditContactActivity;
import com.example.knocker.controller.activity.MainActivity;
import com.example.knocker.controller.activity.MultiChannelActivity;
import com.example.knocker.model.ContactGesture;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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
    private ArrayList<AppCompatImageView> listItemChannelSelected = new ArrayList<>();
    private ArrayList<String> listOfNumberSelected = new ArrayList<>();
    private ArrayList<String> listOfMailSelected = new ArrayList<>();

    public ContactListViewAdapter(Context context, List<ContactWithAllInformation> listContacts) {
        this.context = context;
        this.listContacts = listContacts;
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

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View listview;
        final ViewHolder holder;
        holder = new ViewHolder();
        holder.position = position;
        final ContactDB contact = getItem(position).getContactDB();

        listview = layoutInflater.inflate(R.layout.list_contact_selected_with_channel, null);

        holder.contactRoundedImageView = listview.findViewById(R.id.multi_channel_list_item_contactRoundedImageView);
        holder.contactFirstNameView = listview.findViewById(R.id.multi_channel_list_item_contactFirstName);

        holder.constraintLayout = listview.findViewById(R.id.multi_channel_list_item_layout);
        holder.smsCl = listview.findViewById(R.id.multi_channel_list_item_sms_iv);
        holder.whatsappCl = listview.findViewById(R.id.multi_channel_list_item_whatsapp_iv);
        holder.mailCl = listview.findViewById(R.id.multi_channel_list_item_mail_iv);

        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            System.out.println(contact.getProfilePicture());
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture(), "Get"));
        }
        String contactName = contact.getFirstName() + " " + contact.getLastName();
        if (contactName.length() > 27) {
            contactName = contact.getFirstName() + " " + contact.getLastName();
            contactName = contactName.substring(0, 25) + "..";
        }

        holder.contactFirstNameView.setText(contactName);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getId() == holder.smsCl.getId()) {
                    if (listItemChannelSelected.contains(holder.smsCl)) {
                        listItemChannelSelected.remove(holder.smsCl);
                        holder.smsCl.setImageResource(R.drawable.ic_sms_selector);
                        listOfNumberSelected.remove(listContacts.get(position).getPhoneNumber());
                    } else {
                        listItemChannelSelected.add(holder.smsCl);
                        holder.smsCl.setImageResource(R.drawable.ic_contact_selected);
                        listOfNumberSelected.add(listContacts.get(position).getPhoneNumber());
//                        Toast.makeText(context, listContacts.get(position).getPhoneNumber(), Toast.LENGTH_SHORT).show();
                    }
                }
                if (v.getId() == holder.whatsappCl.getId()) {
                    if (listItemChannelSelected.contains(holder.whatsappCl)) {
                        listItemChannelSelected.remove(holder.whatsappCl);
                        holder.whatsappCl.setImageResource(R.drawable.ic_whatsapp);
//                        listOfNumberSelected.remove(listContacts.get(position).getPhoneNumber());
                    } else {
                        listItemChannelSelected.add(holder.whatsappCl);
                        holder.whatsappCl.setImageResource(R.drawable.ic_contact_selected);
//                        listOfNumberSelected.add(listContacts.get(position).getPhoneNumber());
                    }
                }
                if (v.getId() == holder.mailCl.getId()) {
                    if (listItemChannelSelected.contains(holder.mailCl)) {
                        listItemChannelSelected.remove(holder.mailCl);
                        holder.mailCl.setImageResource(R.drawable.ic_email);
                        listOfMailSelected.remove(listContacts.get(position).getFirstMail());
                    } else {
                        listItemChannelSelected.add(holder.mailCl);
                        holder.mailCl.setImageResource(R.drawable.ic_contact_selected);
                        listOfMailSelected.add(listContacts.get(position).getFirstMail());
                    }
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
        holder.smsCl.setOnClickListener(listener);

        return listview;
    }

    public ArrayList<String> getListOfMailSelected() {
        return listOfMailSelected;
    }

    public ArrayList<String> getListOfNumberSelected() {
        return listOfNumberSelected;
    }

//    private void monoChannelMailClick(ArrayList<String> listOfMail) {
//        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size()));
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.putExtra(Intent.EXTRA_EMAIL, contact)/*listOfMail.toArray(new String[listOfMail.size()]*/
//        intent.data = Uri.parse("mailto:")
//        intent.type = "message/rfc822"
//        intent.putExtra(Intent.EXTRA_SUBJECT, "")
//        intent.putExtra(Intent.EXTRA_TEXT, "")
//        startActivity(intent)
//    }

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
            boolean popup = sharedPreferences.getBoolean("popup", true);
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
        CircularImageView contactRoundedImageView;

        int position;

        AppCompatImageView callCl;
        AppCompatImageView smsCl;
        AppCompatImageView whatsappCl;
        AppCompatImageView mailCl;
    }
}