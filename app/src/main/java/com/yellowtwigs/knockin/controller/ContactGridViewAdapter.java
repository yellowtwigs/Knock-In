package com.yellowtwigs.knockin.controller;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yellowtwigs.knockin.R;
import com.yellowtwigs.knockin.controller.activity.EditContactActivity;
import com.yellowtwigs.knockin.controller.activity.MainActivity;
import com.yellowtwigs.knockin.controller.activity.group.GroupActivity;
import com.yellowtwigs.knockin.model.ContactGesture;
import com.yellowtwigs.knockin.model.ContactManager;
import com.yellowtwigs.knockin.model.ModelDB.ContactDB;
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation;
import com.yellowtwigs.knockin.model.ModelDB.NotificationDB;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


/**
 * La Classe qui permet de remplir la convertView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
public class ContactGridViewAdapter extends BaseAdapter implements FloatingActionMenu.MenuStateChangeListener {
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

    @Override
    public int getCount() {
        return gestionnaireContact.getContactList().size();
    }

    @Override
    public ContactWithAllInformation getItem(int position) {
        return gestionnaireContact.getContactList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "ResourceType", "ViewHolder"})
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_contact_item_layout, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.contactRoundedImageView = convertView.findViewById(R.id.contactRoundedImageView);
            heightAndWidth = holder.contactRoundedImageView.getLayoutParams().height;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.contactRoundedImageView = convertView.findViewById(R.id.contactRoundedImageView);
        holder.gridAdapterFavoriteShine = convertView.findViewById(R.id.grid_adapter_favorite_shine);
        holder.gridContactItemLayout = convertView.findViewById(R.id.grid_contact_item_layout);
        holder.groupWordingConstraint = convertView.findViewById(R.id.grid_adapter_wording_group_constraint_layout);
        holder.groupWordingTv = convertView.findViewById(R.id.grid_adapter_wording_group_tv);

        // int width = holder.contactRoundedImageView.getLayoutParams().width;

        holder.contactFirstNameView = convertView.findViewById(R.id.grid_adapter_contactFirstName);
        ConstraintLayout.LayoutParams layoutParamsTV = (ConstraintLayout.LayoutParams) holder.contactFirstNameView.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParamsIV = (ConstraintLayout.LayoutParams) holder.contactRoundedImageView.getLayoutParams();

        final ContactDB contact = this.gestionnaireContact.getContactList().get(position).getContactDB();
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
        if (len == 6) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightAndWidth * 0.50);
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightAndWidth * 0.50);
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        } else if (len == 5) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightAndWidth * 0.60);
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightAndWidth * 0.60);
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        } else if (len == 4) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightAndWidth * 0.75);
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightAndWidth * 0.75);
            layoutParamsTV.topMargin = 10;
            layoutParamsIV.topMargin = 10;
        } else if (len == 3) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightAndWidth * 0.95);
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightAndWidth * 0.95);
            layoutParamsTV.topMargin = 30;
            layoutParamsIV.topMargin = 10;
        }

        holder.contactLastNameView = convertView.findViewById(R.id.grid_adapter_contactLastName);

        convertView.setTag(holder);

        assert contact != null;
        if (contact.getContactPriority() == 0) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZeroColor, null));
        } else if (contact.getContactPriority() == 1) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityOneColor, null));
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

        //  buttonMessenger.setId(0);
        buttonCall.setId(1);
        buttonSMS.setId(2);
        buttonWhatsApp.setId(3);
        buttonEdit.setId(4);
        buttonMail.setId(5);

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
        buttonMail.setImageResource(R.drawable.ic_circular_gmail);

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
        if (appIsInstalled() && !getItem(position).getFirstPhoneNumber().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonWhatsApp, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        if (!getItem(position).getFirstMail().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonMail, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        if (!getItem(position).getFirstPhoneNumber().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonSMS, layoutParams).build(), diametreBoutton, diametreBoutton)
                    .addSubActionView(builderIcon.setContentView(buttonCall, layoutParams).build(), diametreBoutton, diametreBoutton);
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

                if (context instanceof GroupActivity) {
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
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                context.startActivity(intent);
            }
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
                    firstPosVis = ((GridView) parent).getFirstVisiblePosition() + len;
                }
                System.out.println("selection" + firstPosVis);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).gridMultiSelectItemClick(position);
                } else {
                    ((GroupActivity) context).gridMultiSelectItemClick(len, position, firstPosVis);
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
                ((MainActivity) context).gridMultiSelectItemClick(position);
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
/*
        holder.gridContactItemLayout.setOnClickListener(v -> {
            if (quickMenu.isOpen()) {
                quickMenu.close(false);
            } else {
                quickMenu.open(false);
            }
        });*/
        return convertView;
    }

    private String converter06To33(String phoneNumber) {
        if (phoneNumber.charAt(0) == '0') {
            return "+33" + phoneNumber;
        }
        return phoneNumber;
    }

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
        selectMenu = floatingActionMenu;

        if (modeMultiSelect) {
            floatingActionMenu.close(false);
            selectMenu = null;
        }
    }

    @Override
    public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
        System.out.println("menu close");
        selectMenu = null;
    }


    public void closeMenu() {

        if (selectMenu != null)
            selectMenu.close(true);

    }


    static class ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        CircularImageView contactRoundedImageView;
        AppCompatImageView gridAdapterFavoriteShine;
        ConstraintLayout gridContactItemLayout;
        ConstraintLayout groupWordingConstraint;
        TextView groupWordingTv;
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