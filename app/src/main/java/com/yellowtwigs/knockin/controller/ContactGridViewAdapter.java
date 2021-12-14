package com.yellowtwigs.knockin.controller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yellowtwigs.knockin.R;
import com.yellowtwigs.knockin.controller.activity.EditContactActivity;
import com.yellowtwigs.knockin.controller.activity.HomeActivity;
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity;
import com.yellowtwigs.knockin.model.ContactGesture;
import com.yellowtwigs.knockin.model.ContactManager;
import com.yellowtwigs.knockin.models.data.ContactDB;
import com.yellowtwigs.knockin.models.data.ContactWithAllInformation;
import com.yellowtwigs.knockin.models.data.NotificationDB;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.Objects;


/**
 * La Classe qui permet de remplir la convertView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
public class ContactGridViewAdapter extends RecyclerView.Adapter<ContactGridViewAdapter.ViewHolder> implements FloatingActionMenu.MenuStateChangeListener {
    private ContactManager gestionnaireContact;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;
    private ArrayList<FloatingActionMenu> listCircularMenu = new ArrayList<>();
    private FloatingActionMenu selectMenu;
    private String numberForPermission = "";
    private Boolean modeMultiSelect = false;
    private ArrayList<ContactWithAllInformation> listOfItemSelected = new ArrayList<>();
    private ArrayList<NotificationDB> listOfInteractions = new ArrayList<>();
    private int heightAndWidth;
    private int heightWidthImage;

    public ContactGridViewAdapter(Context context, ContactManager contactManager, Integer len) {
        this.context = context;
        this.gestionnaireContact = contactManager;
        this.len = len;
        layoutInflater = LayoutInflater.from(context);
    }

    public ArrayList<ContactWithAllInformation> getListOfItemSelected() {
        return listOfItemSelected;
    }

    public void setListOfItemSelected(ArrayList<ContactWithAllInformation> listOfItemSelected) {
        this.listOfItemSelected.clear();
        this.listOfItemSelected.addAll(listOfItemSelected);
    }

    public void setGestionnaireContact(ContactManager gestionnaireContact) {
        this.gestionnaireContact = gestionnaireContact;
    }

    public ContactWithAllInformation getItem(int position) {
        return gestionnaireContact.getContactList().get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.grid_contact_item_layout, parent, false);
//        parentGrid = ((GridView) parent);
        ContactGridViewAdapter.ViewHolder holder = new ContactGridViewAdapter.ViewHolder(view);

        heightWidthImage = holder.contactRoundedImageView.getLayoutParams().height;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final ContactDB contact = this.gestionnaireContact.getContactList().get(position).getContactDB();

        int height = heightWidthImage;
        int width = heightWidthImage;

        System.out.println(" layout params height " + height + " width " + width);
        RelativeLayout.LayoutParams layoutParamsTV = (RelativeLayout.LayoutParams) holder.contactFirstNameView.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParamsIV = (ConstraintLayout.LayoutParams) holder.contactRoundedImageView.getLayoutParams();

        if (!modeMultiSelect || !listOfItemSelected.contains(gestionnaireContact.getContactList().get(position))) {
            assert contact != null;
            if (!contact.getProfilePicture64().equals("")) {
                Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
                holder.contactRoundedImageView.setImageBitmap(bitmap);
            } else {
                holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
            }
        } else {
            // listOfItemSelected.add(gestionnaireContact.getContactList().get(position));
            holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
        }

        if (len == 3) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.05));
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.05));
            layoutParamsTV.topMargin = 30;
            layoutParamsIV.topMargin = 10;
        } else if (len == 4) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.25));
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.25));
            layoutParamsTV.topMargin = 10;
            layoutParamsIV.topMargin = 10;
        } else if (len == 5) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.40));
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.40));
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        } else if (len == 6) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.50));
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.50));
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        }

        assert contact != null;
        if (contact.getContactPriority() == 0) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZeroColor, null));
        } else if (contact.getContactPriority() == 1) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.transparentColor, null));
        } else if (contact.getContactPriority() == 2) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwoColor, null));
        }

        String firstname = contact.getFirstName();
        String lastName = contact.getLastName();

        if (len == 6) {
            if (contact.getFirstName().length() > 8)
                firstname = contact.getFirstName().substring(0, 7).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.81f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            if (contact.getLastName().length() > 8)
                lastName = contact.getLastName().substring(0, 7).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.81f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);

        } else if (len == 5) {
            if (contact.getFirstName().length() > 11)
                firstname = contact.getFirstName().substring(0, 9).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.9f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary, null));
            if (contact.getLastName().length() > 11)
                lastName = contact.getLastName().substring(0, 9).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.9f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);

        } else if (len == 4) {
            if (contact.getFirstName().length() > 12)
                firstname = contact.getFirstName().substring(0, 10).concat("..");

            Spannable spanFistName = new SpannableString(firstname);
            spanFistName.setSpan(new RelativeSizeSpan(0.95f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);
            if (contact.getLastName().length() > 12)
                lastName = contact.getLastName().substring(0, 10).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.95f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
        } else if (len == 3) {
            Spannable spanFistName = new SpannableString(firstname);
            spanFistName.setSpan(new RelativeSizeSpan(0.95f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);
            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.95f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
            //holder.contactFirstNameView.;
        }

        if (firstname.isEmpty()) {
            holder.contactFirstNameView.setVisibility(View.GONE);
        }

     /*   if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());

            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture())); //////////////
        }
       */ //region circular menu

        //final ImageView buttonMessenger = new ImageView(context);
        final ImageView buttonCall = new ImageView(context);
        final ImageView buttonWhatsApp = new ImageView(context);
        final ImageView buttonSMS = new ImageView(context);
        final ImageView buttonEdit = new ImageView(context);
        final ImageView buttonMail = new ImageView(context);
//        final ImageView buttonMessenger = new ImageView(context);

        //  buttonMessenger.setId(0);
        buttonCall.setId(1);
        buttonSMS.setId(2);
        buttonWhatsApp.setId(3);
        buttonEdit.setId(4);
        buttonMail.setId(5);
//        buttonMessenger.setId(6);

        //buttonMessenger.setImageDrawable(iconMessenger);
        if (contact.getFavorite() == 1) {
            holder.gridAdapterFavoriteShine.setVisibility(View.VISIBLE);
        } else {
            holder.gridAdapterFavoriteShine.setVisibility(View.GONE);
        }

        buttonCall.setImageResource(R.drawable.ic_google_call);
        buttonWhatsApp.setImageResource(R.drawable.ic_circular_whatsapp);
        buttonSMS.setImageResource(R.drawable.ic_sms_selector);
        buttonEdit.setImageResource(R.drawable.ic_circular_edit);
        buttonMail.setImageResource(R.drawable.ic_circular_mail); // toto
//        buttonMessenger.setImageResource(R.drawable.ic_circular_messenger);

        SubActionButton.Builder builderIcon = new SubActionButton.Builder((Activity) context);
        builderIcon.setBackgroundDrawable(context.getDrawable(R.drawable.ic_circular));
        builderIcon.setContentView(buttonCall);

        int startAngle;
        int endAngle;
        if (position % len == 0) {
            System.out.println("position vaut " + position + " modulo" + len + " vaut" + position % len);
            startAngle = 90;
            endAngle = -90;
        } else if (position % len == len - 1) {
            System.out.println("position vaut " + position + " modulo" + len + " vaut" + position % len);
            startAngle = 90;
            endAngle = 270;
        } else {
            System.out.println("position vaut " + position + " modulo" + len + " vaut" + position % len);
            startAngle = 0;
            endAngle = -180;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int diametreBoutton = (int) (0.38 * metrics.densityDpi);
        int radiusMenu = (int) (0.50 * metrics.densityDpi);
        int border = (int) (0.0625 * metrics.densityDpi);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(5, 5, 5, 5);

        FloatingActionMenu.Builder builder = new FloatingActionMenu.Builder((Activity) context)
                .setStartAngle(startAngle)
                .setEndAngle(endAngle)
                .setRadius(radiusMenu)
                .addSubActionView(builderIcon.setContentView(buttonEdit, layoutParams).build(), diametreBoutton, diametreBoutton)
                .attachTo(holder.contactRoundedImageView)
                .setStateChangeListener(this)
                .disableAnimations();
        if (appIsInstalled() && !getItem(position).getFirstPhoneNumber().equals("") && contact.getHasWhatsapp() == 1) {
            builder.addSubActionView(builderIcon.setContentView(buttonWhatsApp, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        if (!getItem(position).getFirstMail().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonMail, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        if (!getItem(position).getFirstPhoneNumber().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonSMS, layoutParams).build(), diametreBoutton, diametreBoutton)
                    .addSubActionView(builderIcon.setContentView(buttonCall, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        if (!getItem(position).getMessengerID().equals("")) {
//            builder.addSubActionView(builderIcon.setContentView(buttonMessenger, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        /*if (!getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber()).equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonCall, layoutParams).build(), diametreBoutton, diametreBoutton);
        }*/

       /* if( appIsInstalled( "com.facebook.orca")){
            builder.addSubActionView(builderIcon.setContentView(buttonMessenger,layoutParams).build(),diametreBoutton,diametreBoutton);
        }*/

        final FloatingActionMenu quickMenu = builder.build();
        listCircularMenu.add(quickMenu);

        //quickMenu.addSubActionView(builderIcon.setContentView(buttonSMS,layoutParams).build(),diametreBoutton,diametreBoutton);
        View.OnClickListener buttonListener = v -> {

           /* if (v.getId() == buttonMessenger.getId()) {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + "")));
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + "")));
                }
            } else*/
            if (v.getId() == buttonWhatsApp.getId()) {

                ContactWithAllInformation contactWithAllInformation = getItem(position);
                ContactGesture.INSTANCE.openWhatsapp(converter06To33(contactWithAllInformation.getFirstPhoneNumber()), context);

            } else if (v.getId() == buttonEdit.getId()) {

                Intent intent = new Intent(context, EditContactActivity.class);
                intent.putExtra("ContactId", contact.getId());
                intent.putExtra("position", position);

                if (context instanceof GroupManagerActivity) {
                    intent.putExtra("fromGroupActivity", true);
                }

                context.startActivity(intent);
            } else if (v.getId() == buttonCall.getId()) {

                callPhone(getItem(position).getFirstPhoneNumber());

            } else if (v.getId() == buttonSMS.getId()) {

                String phone = getItem(position).getFirstPhoneNumber();
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null));
                context.startActivity(i);

            } else if (v.getId() == buttonMail.getId()) {

                String mail = getItem(position).getFirstMail();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                //intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                System.out.println("intent " + Objects.requireNonNull(intent.getExtras()).toString());
                context.startActivity(intent);
            }

//            else if (v.getId() == buttonMessenger.getId()) {
//
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + getItem(position).getMessengerID()));
//                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//
//            }
            selectMenu.close(false);
        };

        View.OnLongClickListener gridlongClick = v -> {
            if (!modeMultiSelect) {
                int firstPosVis;
                closeMenu();
                modeMultiSelect = true;
                listOfItemSelected.add(gestionnaireContact.getContactList().get(position));

                if (position < 2 * len) {
                    firstPosVis = 0;
                } else {
                    firstPosVis = 0;
//                    firstPosVisfirstPosVis = ((GridView) parent).getFirstVisiblePosition() + len;
                }
                System.out.println("selection" + firstPosVis);
                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).gridMultiSelectItemClick(position);
                } else {
                    ((GroupManagerActivity) context).gridMultiSelectItemClick(len, position, firstPosVis);
                }
                holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
            }
            return true;
        };

        View.OnClickListener gridItemClick = v -> {
            if (modeMultiSelect) {
                if (listOfItemSelected.contains(gestionnaireContact.getContactList().get(position))) {
                    listOfItemSelected.remove(gestionnaireContact.getContactList().get(position));

                    if (!contact.getProfilePicture64().equals("")) {
                        Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
                        holder.contactRoundedImageView.setImageBitmap(bitmap);
                    } else {
                        holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
                    }
                    if (listOfItemSelected.isEmpty()) {
                        modeMultiSelect = false;
                    }
                } else {
                    listOfItemSelected.add(gestionnaireContact.getContactList().get(position));
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
                }
                ((HomeActivity) context).gridMultiSelectItemClick(position);
            } else {
                if (quickMenu.isOpen()) {
                    quickMenu.close(false);
                } else {
                    quickMenu.open(false);
                }
            }
        };

        buttonCall.setOnLongClickListener(v -> {
            String phoneNumber = getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber());
            if (!phoneNumber.isEmpty()) {
                callPhone(phoneNumber);
            }
            return true;
        });

     /*   holder.groupWordingConstraint.setOnClickListener(v -> {
            DbWorkerThread main_mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
            main_mDbWorkerThread.start();
            ArrayList<Integer> listPosition = new ArrayList<>();

            if (!secondClick) {
                System.out.println("list contact grid size" + gestionnaireContact.getContactList().size());
                for (int i = 0; i < gestionnaireContact.getContactList().size(); i++) {
                    if (gestionnaireContact.getContactList().get(i).getFirstGroup(context) != null) {
                        if (Objects.equals(gestionnaireContact.getContactList().get(i).getFirstGroup(context).getId(), Objects.requireNonNull(gestionnaireContact.getContactList().get(position).getFirstGroup(context)).getId())) {
                            System.out.println("id egale a l'autre ");
                            listPosition.add(i);
                        } else {
                            System.out.println(gestionnaireContact.getContactList().get(i).getFirstGroup(context).getId() + " id different a " + gestionnaireContact.getContactList().get(position).getFirstGroup(context).getId());
                        }
                    }
                }
                ((GroupActivity) context).clickGroupGrid(len, listPosition, ((GridView) parent).getFirstVisiblePosition(), secondClick, true);
            }
        });*/

        holder.gridContactItemLayout.setOnLongClickListener(gridlongClick);
        holder.contactRoundedImageView.setOnLongClickListener(gridlongClick);

        holder.gridContactItemLayout.setOnClickListener(gridItemClick);
        holder.contactRoundedImageView.setOnClickListener(gridItemClick);
        //buttonMessenger.setOnClickListener(buttonListener);
        buttonWhatsApp.setOnClickListener(buttonListener);
        buttonCall.setOnClickListener(buttonListener);
        buttonSMS.setOnClickListener(buttonListener);
        buttonEdit.setOnClickListener(buttonListener);
        buttonMail.setOnClickListener(buttonListener);
//        buttonMessenger.setOnClickListener(buttonListener);

//        holder.gridContactItemLayout.setOnClickListener(v -> {
//            if (selectMenu.isOpen()) {
//                selectMenu.close(false);
//            } else {
//                selectMenu.open(false);
//            }
//        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return gestionnaireContact.getContactList().size();
    }

    private String converter06To33(String phoneNumber) {
        if (phoneNumber.charAt(0) == '0') {
            return "+33" + phoneNumber;
        }
        return phoneNumber;
    }

    private int randomDefaultImage(int avatarId) {

        SharedPreferences sharedPreferencesIsMultiColor = context.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE);
        int multiColor = sharedPreferencesIsMultiColor.getInt("isMultiColor", 0);

        SharedPreferences sharedPreferencesContactsColor = context.getSharedPreferences("ContactsColor", Context.MODE_PRIVATE);
        int contactsColorPosition = sharedPreferencesContactsColor.getInt("contactsColor", 0);

        if (multiColor == 0) {
            switch (avatarId) {
                case 0:
                    return R.drawable.ic_user_purple;
                case 1:
                    return R.drawable.ic_user_blue;
                case 2:
                    return R.drawable.ic_user_cyan_teal;
                case 3:
                    return R.drawable.ic_user_green;
                case 4:
                    return R.drawable.ic_user_om;
                case 5:
                    return R.drawable.ic_user_orange;
                case 6:
                    return R.drawable.ic_user_red;
                default:
                    return R.drawable.ic_user_blue;
            }
        } else {
            switch (contactsColorPosition) {
                case 0:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_blue;
                        case 1:
                            return R.drawable.ic_user_blue_indigo1;
                        case 2:
                            return R.drawable.ic_user_blue_indigo2;
                        case 3:
                            return R.drawable.ic_user_blue_indigo3;
                        case 4:
                            return R.drawable.ic_user_blue_indigo4;
                        case 5:
                            return R.drawable.ic_user_blue_indigo5;
                        case 6:
                            return R.drawable.ic_user_blue_indigo6;
                        default:
                            return R.drawable.ic_user_om;
                    }
                case 1:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_green;
                        case 1:
                            return R.drawable.ic_user_green_lime1;
                        case 2:
                            return R.drawable.ic_user_green_lime2;
                        case 3:
                            return R.drawable.ic_user_green_lime3;
                        case 4:
                            return R.drawable.ic_user_green_lime4;
                        case 5:
                            return R.drawable.ic_user_green_lime5;
                        default:
                            return R.drawable.ic_user_green_lime6;
                    }
                case 2:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_purple;
                        case 1:
                            return R.drawable.ic_user_purple_grape1;
                        case 2:
                            return R.drawable.ic_user_purple_grape2;
                        case 3:
                            return R.drawable.ic_user_purple_grape3;
                        case 4:
                            return R.drawable.ic_user_purple_grape4;
                        case 5:
                            return R.drawable.ic_user_purple_grape5;
                        default:
                            return R.drawable.ic_user_purple;
                    }
                case 3:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_red;
                        case 1:
                            return R.drawable.ic_user_red1;
                        case 2:
                            return R.drawable.ic_user_red2;
                        case 3:
                            return R.drawable.ic_user_red3;
                        case 4:
                            return R.drawable.ic_user_red4;
                        case 5:
                            return R.drawable.ic_user_red5;
                        default:
                            return R.drawable.ic_user_red;
                    }
                case 4:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_grey;
                        case 1:
                            return R.drawable.ic_user_grey1;
                        case 2:
                            return R.drawable.ic_user_grey2;
                        case 3:
                            return R.drawable.ic_user_grey3;
                        case 4:
                            return R.drawable.ic_user_grey4;
                        default:
                            return R.drawable.ic_user_grey1;
                    }
                case 5:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_orange;
                        case 1:
                            return R.drawable.ic_user_orange1;
                        case 2:
                            return R.drawable.ic_user_orange2;
                        case 3:
                            return R.drawable.ic_user_orange3;
                        case 4:
                            return R.drawable.ic_user_orange4;
                        default:
                            return R.drawable.ic_user_orange3;
                    }
                case 6:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_cyan_teal;
                        case 1:
                            return R.drawable.ic_user_cyan_teal1;
                        case 2:
                            return R.drawable.ic_user_cyan_teal2;
                        case 3:
                            return R.drawable.ic_user_cyan_teal3;
                        case 4:
                            return R.drawable.ic_user_cyan_teal4;
                        default:
                            return R.drawable.ic_user_cyan_teal;
                    }
                default:
                    switch (avatarId) {
                        case 0:
                            return R.drawable.ic_user_purple;
                        case 1:
                            return R.drawable.ic_user_blue;
                        case 2:
                            return R.drawable.ic_user_cyan_teal;
                        case 3:
                            return R.drawable.ic_user_green;
                        case 4:
                            return R.drawable.ic_user_om;
                        case 5:
                            return R.drawable.ic_user_orange;
                        case 6:
                            return R.drawable.ic_user_red;
                        default:
                            return R.drawable.ic_user_blue;
                    }
            }
        }
    }

    public void callPhone(final String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_CALL_RESULT = 1;
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_RESULT);
            numberForPermission = phoneNumber;
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE);
            Boolean popup = sharedPreferences.getBoolean("popup", true);
            if (popup && numberForPermission.isEmpty()) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.main_contact_grid_title)
                        .setMessage(R.string.main_contact_grid_message)
                        .setPositiveButton(android.R.string.yes, (dialog, id) -> context.startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))))
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            } else {
                context.startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
                numberForPermission = "";
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
        System.out.println("menu select");
        if (selectMenu != null) {
            selectMenu.close(false);
        }
        if (multiSelectMode()) {
            floatingActionMenu.close(false);
        }
        selectMenu = floatingActionMenu;
    }

    /**
     * @param floatingActionMenu
     */
    @Override
    public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
        System.out.println("menu close");
        selectMenu = null;
    }

    /**
     * Ferme le menu qui est ouvert
     */
    public void closeMenu() {

        if (selectMenu != null)
            selectMenu.close(true);

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        CircularImageView contactRoundedImageView;
        AppCompatImageView gridAdapterFavoriteShine;
        ConstraintLayout gridContactItemLayout;

        ViewHolder(View view) {
            super(view);
            contactFirstNameView = view.findViewById(R.id.grid_adapter_contactFirstName);
            contactLastNameView = view.findViewById(R.id.grid_adapter_contactLastName);
            gridContactItemLayout = view.findViewById(R.id.grid_contact_item_layout);
            contactRoundedImageView = view.findViewById(R.id.contactRoundedImageView);
            gridAdapterFavoriteShine = view.findViewById(R.id.grid_adapter_favorite_shine);
//            heightWidthImage = holder.contactRoundedImageView.getLayoutParams().height;
        }
    }

    public FloatingActionMenu getSelectMenu() {
        return selectMenu;
    }

    private boolean appIsInstalled() {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo("com.whatsapp", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean multiSelectMode() {
        return modeMultiSelect;
    }

    public String getPhonePermission() {
        return numberForPermission;
    }
}