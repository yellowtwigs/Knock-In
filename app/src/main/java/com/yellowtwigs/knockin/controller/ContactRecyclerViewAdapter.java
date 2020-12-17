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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
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
import com.yellowtwigs.knockin.controller.activity.MainActivity;
import com.yellowtwigs.knockin.controller.activity.group.AddNewGroupActivity;
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity;
import com.yellowtwigs.knockin.model.ContactGesture;
import com.yellowtwigs.knockin.model.ContactManager;
import com.yellowtwigs.knockin.model.DbWorkerThread;
import com.yellowtwigs.knockin.model.ModelDB.ContactDB;
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation;
import com.yellowtwigs.knockin.model.ModelDB.GroupDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.sql.DriverManager.println;

/**
 * La Classe qui permet de remplir la RecyclerView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ContactViewHolder> {

    private List<ContactWithAllInformation> listContacts;
    private Context context;
    private Integer len;
    private View view;
    private ContactManager gestionnaireContacts;
    private Boolean modeMultiSelect = false;
    private Boolean secondClick = false;
    private Boolean lastClick = false;

    private ConstraintLayout lastSelectMenuLen1;

    public ArrayList<ContactWithAllInformation> getListOfItemSelected() {
        return listOfItemSelected;
    }

    public void setListOfItemSelected(ArrayList<ContactWithAllInformation> listOfItemSelected) {
        this.listOfItemSelected.clear();
        this.listOfItemSelected.addAll(listOfItemSelected);
    }

    private ArrayList<ContactWithAllInformation> listOfItemSelected = new ArrayList<>();

    private String numberForPermission = "";

    public ContactRecyclerViewAdapter(Context context, ContactManager gestionnaireContacts, Integer len) {
        this.context = context;
        this.len = len;
        this.gestionnaireContacts = gestionnaireContacts;
        this.listContacts = gestionnaireContacts.getContactList();
        lastSelectMenuLen1 = null;
    }

    public ContactWithAllInformation getItem(int position) {
        return listContacts.get(position);
    }

    public void setGestionnaireContact(ContactManager gestionnaireContact) {
        this.gestionnaireContacts = gestionnaireContact;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_contact_item_layout, parent, false);

        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactViewHolder holder, final int position) {
        final ContactDB contact = getItem(position).getContactDB();
        assert contact != null;

        if (len == 0) {
            if (contact.getContactPriority() == 0) {
            } else if (contact.getContactPriority() == 1) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE);
                if (sharedPreferences.getBoolean("darkTheme", false)) {
//                    holder.contactFirstNameView.setTextColor(context.getResources().getColor(R.color.textColorLight, null));
                } else {
                    holder.contactFirstNameView.setTextColor(context.getResources().getColor(R.color.textColorDark, null));
                }

            } else if (contact.getContactPriority() == 2) {
                holder.contactFirstNameView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark, null));
            }
        } else {
            if (contact.getContactPriority() == 0) {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZeroColor, null));
            } else if (contact.getContactPriority() == 1) {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.transparentColor, null));
            } else if (contact.getContactPriority() == 2) {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwoColor, null));
            }

        }
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
        }

        String contactName = contact.getFirstName() + " " + contact.getLastName();

        holder.contactFirstNameView.setText(contactName);
        String group = "";
        GroupDB firstGroup = getItem(position).getFirstGroup(context);
        if (context instanceof GroupManagerActivity) {
            holder.groupWordingConstraint.setVisibility(View.VISIBLE);
            if (len == 0) {
                holder.contactRoundedImageView.setVisibility(View.INVISIBLE);
            }
        }
        if (firstGroup == null) {
            SharedPreferences sharedThemePreferences = context.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE);
            if (sharedThemePreferences.getBoolean("darkTheme", false)) {
                Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColorDark, null), PorterDuff.Mode.MULTIPLY);
                holder.groupWordingConstraint.setBackground(roundedLayout);
            } else {
                Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColor, null), PorterDuff.Mode.MULTIPLY);
                holder.groupWordingConstraint.setBackground(roundedLayout);
            }
            //Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            //roundedLayout.setColorFilter(Color.parseColor("#f0f0f0"), PorterDuff.Mode.MULTIPLY);
            //holder.groupWordingConstraint.setBackground(roundedLayout);
        } else {
            group = firstGroup.getName();
            Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            assert roundedLayout != null;
//            roundedLayout.setColorFilter(firstGroup.randomColorGroup(this.context), PorterDuff.Mode.MULTIPLY);
            roundedLayout.setColorFilter(firstGroup.getSection_color(), PorterDuff.Mode.MULTIPLY);
            holder.groupWordingConstraint.setBackground(roundedLayout);
        }
        if (len == 0) {
            if (group.length() > 6) {
                group = group.substring(0, 6).concat("..");
            }
        } else {
            if (group.length() > 9) {
                group = group.substring(0, 9).concat("..");
            }
        }
        if (holder.groupWordingTv != null) {
            holder.groupWordingTv.setText(group);
        }
        if (modeMultiSelect) {
            if (listOfItemSelected.contains(gestionnaireContacts.getContactList().get(position))) {
                if (context instanceof GroupManagerActivity && len == 0) {
                } else {
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
                }
            }
        }

        View.OnClickListener listener = v -> {
            if (v.getId() == holder.smsCl.getId()) {
                String phone = getItem(position).getFirstPhoneNumber();
                context.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null)));
            }
            if (v.getId() == holder.callCl.getId()) {
                callPhone(getItem(position).getFirstPhoneNumber());
            }
            if (v.getId() == holder.whatsappCl.getId()) {
                ContactWithAllInformation contactWithAllInformation = getItem(position);
                ContactGesture.INSTANCE.openWhatsapp(contactWithAllInformation.getFirstPhoneNumber(), context);
            }
            if (v.getId() == holder.mailCl.getId()) {
                String mail = getItem(position).getFirstMail();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                println("intent " + Objects.requireNonNull(intent.getExtras()).toString());
                context.startActivity(intent);
            }
            if (len == 1) {
                if (v.getId() == holder.editCl.getId()) {
                    Intent intent = new Intent(context, EditContactActivity.class);
                    intent.putExtra("ContactId", contact.getId());
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
            }
        };

        View.OnLongClickListener longClick = v -> {
            if (listOfItemSelected.size() == 0 && len == 1 && holder.constraintLayoutMenu != null) {
                holder.constraintLayoutMenu.setVisibility(View.GONE);
            }

            view.setTag(holder);
            ContactDB contactDB = gestionnaireContacts.getContactList().get(position).getContactDB();
            assert contactDB != null;

            if (listOfItemSelected.contains(gestionnaireContacts.getContactList().get(position))) {
                listOfItemSelected.remove(gestionnaireContacts.getContactList().get(position));

                if (!contactDB.getProfilePicture64().equals("")) {
                    Bitmap bitmap = base64ToBitmap(contactDB.getProfilePicture64());
                    holder.contactRoundedImageView.setImageBitmap(bitmap);
                } else {
                    holder.contactRoundedImageView.setImageResource(randomDefaultImage(contactDB.getProfilePicture()));
                }
            } else {
                listOfItemSelected.add(gestionnaireContacts.getContactList().get(position));
                if (context instanceof GroupManagerActivity && len == 0) {
                } else {
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
                }
            }

            if (context instanceof MainActivity) {
                ((MainActivity) context).recyclerMultiSelectItemClick(position);
            }

            if (listOfItemSelected.size() > 0) {
                modeMultiSelect = true;
                lastClick = false;
            } else {
                modeMultiSelect = false;
                lastClick = true;
            }

            return modeMultiSelect;
        };

        View.OnClickListener listItemClick = v -> {
            if (modeMultiSelect) {
                if (listOfItemSelected.size() == 0 && len == 1 && holder.constraintLayoutMenu != null) {
                    holder.constraintLayoutMenu.setVisibility(View.GONE);
                }

                view.setTag(holder);
                ContactDB contactDB = gestionnaireContacts.getContactList().get(position).getContactDB();
                assert contactDB != null;

                if (listOfItemSelected.contains(gestionnaireContacts.getContactList().get(position))) {
                    listOfItemSelected.remove(gestionnaireContacts.getContactList().get(position));

                    if (!contactDB.getProfilePicture64().equals("")) {
                        Bitmap bitmap = base64ToBitmap(contactDB.getProfilePicture64());
                        holder.contactRoundedImageView.setImageBitmap(bitmap);
                    } else {
                        holder.contactRoundedImageView.setImageResource(randomDefaultImage(contactDB.getProfilePicture()));
                    }
                } else {
                    listOfItemSelected.add(gestionnaireContacts.getContactList().get(position));
                    if (context instanceof GroupManagerActivity && len == 0) {
                    } else {
                        holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
                    }
                }

                if (context instanceof MainActivity) {
                    ((MainActivity) context).recyclerMultiSelectItemClick(position);
                }

                if (listOfItemSelected.size() > 0) {
                    modeMultiSelect = true;
                    lastClick = false;
                } else {
                    modeMultiSelect = false;
                    lastClick = true;
                }
            } else {
                if (lastClick) {
                    lastClick = false;
                } else {
                    if (len == 1) {
                        if (holder.constraintLayoutMenu != null) {
                            if (holder.constraintLayoutMenu.getVisibility() == View.GONE) {
                                holder.constraintLayoutMenu.setVisibility(View.VISIBLE);
                                slideUp(holder.constraintLayoutMenu);
                                if (lastSelectMenuLen1 != null)
                                    lastSelectMenuLen1.setVisibility(View.GONE);
                                lastSelectMenuLen1 = holder.constraintLayoutMenu;
                            } else {
                                holder.constraintLayoutMenu.setVisibility(View.GONE);
                                Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                                holder.constraintLayoutMenu.startAnimation(slideDown);
                                lastSelectMenuLen1 = null;
                            }
                        }
                    }
                }
            }
        };

        if (len == 0) {
            if (whatsappIsNotInstalled() || contact.getHasWhatsapp() == 0) {
                holder.whatsappCl.setVisibility(View.INVISIBLE);
            } else {
                holder.whatsappCl.setVisibility(View.VISIBLE);
            }

            if (getItem(position).getFirstMail().isEmpty()) {
                holder.mailCl.setVisibility(View.INVISIBLE);
            } else {
                holder.mailCl.setVisibility(View.VISIBLE);
            }

            if (getItem(position).getFirstPhoneNumber().isEmpty()) {
                holder.callCl.setVisibility(View.INVISIBLE);
                holder.smsCl.setVisibility(View.INVISIBLE);
            } else {
                holder.callCl.setVisibility(View.VISIBLE);
                holder.smsCl.setVisibility(View.VISIBLE);
            }
        }

        if (len == 1) {
            if (whatsappIsNotInstalled() || contact.getHasWhatsapp() == 0) {
                holder.whatsappCl.setVisibility(View.GONE);
            } else {
                holder.whatsappCl.setVisibility(View.VISIBLE);
            }

            if (getItem(position).getFirstMail().isEmpty()) {
                holder.mailCl.setVisibility(View.GONE);
            } else {
                holder.mailCl.setVisibility(View.VISIBLE);
            }

            if (getItem(position).getFirstPhoneNumber().isEmpty()) {
                holder.callCl.setVisibility(View.GONE);
                holder.smsCl.setVisibility(View.GONE);
            } else {
                holder.callCl.setVisibility(View.VISIBLE);
                holder.smsCl.setVisibility(View.VISIBLE);
            }
        }

        if (holder.constraintLayout != null) {
            holder.constraintLayout.setOnLongClickListener(longClick);
            holder.constraintLayout.setOnClickListener(listItemClick);
        }

        holder.mailCl.setOnClickListener(listener);
        holder.whatsappCl.setOnClickListener(listener);
        holder.callCl.setOnClickListener(listener);
        holder.smsCl.setOnClickListener(listener);
        holder.callCl.setOnLongClickListener(v -> {
            String phoneNumber = getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber());
            if (!phoneNumber.isEmpty()) {
                callPhone(phoneNumber);
            }
            return true;
        });
        if (holder.editCl != null) {
            holder.editCl.setOnClickListener(listener);
        }

        if (contact.getFavorite() == 1) {
            holder.listContactItemFavoriteShine.setVisibility(View.VISIBLE);
        } else {
            holder.listContactItemFavoriteShine.setVisibility(View.GONE);
        }

        holder.groupWordingConstraint.setOnClickListener(v -> {

            DbWorkerThread main_mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
            main_mDbWorkerThread.start();

            System.out.println("list contact grid size" + gestionnaireContacts.getContactList().size());
            if (!secondClick) {
                if (!modeMultiSelect) {
                    modeMultiSelect = true;
                    for (int i = 0; i < gestionnaireContacts.getContactList().size(); i++) {
                        if (gestionnaireContacts.getContactList().get(i).getFirstGroup(context) != null) {
                            if (Objects.equals(Objects.requireNonNull(gestionnaireContacts.getContactList().get(i).getFirstGroup(context)).getId(), gestionnaireContacts.getContactList().get(position).getFirstGroup(context).getId())) {
                                if (!listOfItemSelected.contains(gestionnaireContacts.getContactList().get(i))) {
                                    System.out.println(Objects.requireNonNull(getItem(i).getContactDB()).getFirstName() + " " + Objects.requireNonNull(getItem(i).getContactDB()).getLastName());
                                    ((GroupManagerActivity) context).recyclerMultiSelectItemClick(i, secondClick, true);
                                    listOfItemSelected.add(gestionnaireContacts.getContactList().get(i));
                                }
                            }
                        }
                    }
                    secondClick = true;
                    notifyDataSetChanged();
                }
            } else {
                for (int i = 0; i < gestionnaireContacts.getContactList().size(); i++) {
                    if (gestionnaireContacts.getContactList().get(i).getFirstGroup(context) != null) {
                        if (Objects.equals(Objects.requireNonNull(gestionnaireContacts.getContactList().get(i).getFirstGroup(context)).getId(), gestionnaireContacts.getContactList().get(position).getFirstGroup(context).getId())) {
                            if (listOfItemSelected.contains(gestionnaireContacts.getContactList().get(i))) {
                                ((GroupManagerActivity) context).recyclerMultiSelectItemClick(i, secondClick, true);
                                listOfItemSelected.remove(gestionnaireContacts.getContactList().get(i));
                            }
                        }
                    }
                }
                secondClick = false;
                modeMultiSelect = false;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return listContacts.size();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return listContacts.size();
    }

//    private String converter06To33(String phoneNumber) {
//        if (phoneNumber.charAt(0) == '0') {
//            return "+33" + phoneNumber;
//        }
//        return phoneNumber;
//    }

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

    private Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    public void callPhone(final String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_CALL_RESULT = 1;
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_RESULT);
            numberForPermission = phoneNumber;
        } else {
            //Intent intent=new Intent(Intent.ACTION_CALL);
            //intent.setData(Uri.parse(getItem(position).getFirstPhoneNumber()));
            SharedPreferences sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE);
            boolean popup = sharedPreferences.getBoolean("popup", true);
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
    }//code duplicate à mettre dans contactAllInfo


    private boolean whatsappIsNotInstalled() {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo("com.whatsapp", 0);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactFirstNameView;
        RelativeLayout constraintLayout;
        ConstraintLayout constraintLayoutMenu;
        AppCompatImageView listContactItemFavoriteShine;
        CircularImageView contactRoundedImageView;

        RelativeLayout callCl;
        RelativeLayout smsCl;
        RelativeLayout whatsappCl;
        RelativeLayout mailCl;
        RelativeLayout editCl;

        ConstraintLayout groupWordingConstraint;
        TextView groupWordingTv;
        Boolean open;

        ContactViewHolder(@NonNull View view) {
            super(view);

            contactRoundedImageView = view.findViewById(R.id.list_contact_item_contactRoundedImageView);
            contactFirstNameView = view.findViewById(R.id.list_contact_item_contactFirstName);
            constraintLayout = view.findViewById(R.id.list_contact_item_layout);
            constraintLayoutMenu = view.findViewById(R.id.list_contact_item_menu);
            callCl = view.findViewById(R.id.list_contact_item_constraint_call);
            smsCl = view.findViewById(R.id.list_contact_item_constraint_sms);
            editCl = view.findViewById(R.id.list_contact_item_constraint_edit);
            whatsappCl = view.findViewById(R.id.list_contact_item_constraint_whatsapp);
            mailCl = view.findViewById(R.id.list_contact_item_constraint_mail);
            groupWordingConstraint = view.findViewById(R.id.list_contact_wording_group_constraint_layout);
            groupWordingTv = view.findViewById(R.id.list_contact_wording_group_tv);
            listContactItemFavoriteShine = view.findViewById(R.id.list_contact_item_favorite_shine);
            open = false;
        }
    }

    public String getPhonePermission() {
        return numberForPermission;
    }
}