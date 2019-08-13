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

import com.example.knocker.R;
import com.example.knocker.controller.activity.ContactDetailsActivity;
import com.example.knocker.controller.activity.EditContactActivity;
import com.example.knocker.controller.activity.MainActivity;
import com.example.knocker.controller.activity.group.GroupActivity;
import com.example.knocker.model.ContactGesture;
import com.example.knocker.model.ContactList;
import com.example.knocker.model.ContactsRoomDatabase;
import com.example.knocker.model.DbWorkerThread;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.example.knocker.model.ModelDB.GroupDB;

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
    private ContactList gestionnaireContacts;
    private Boolean modeMultiSelect = false;
    private Boolean secondClick = false;
    private Boolean lastClick = false;

    private ConstraintLayout lastSelectMenuLen1;

    public ArrayList<ContactWithAllInformation> getListOfItemSelected() {
        return listOfItemSelected;
    }

    public void checkListOfItemSelected() {

        DbWorkerThread main_mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
        main_mDbWorkerThread.start();

        System.out.println("list contact grid size" + gestionnaireContacts.getContacts().size());
        modeMultiSelect = true;
        for (int i = 0; i < gestionnaireContacts.getContacts().size(); i++) {
            if (gestionnaireContacts.getContacts().get(i).getFirstGroup(context) != null) {
                if (Objects.equals(Objects.requireNonNull(gestionnaireContacts.getContacts().get(i).getFirstGroup(context)).getId(), gestionnaireContacts.getContacts().get(i).getFirstGroup(context).getId())) {
                    if (listOfItemSelected.contains(gestionnaireContacts.getContacts().get(i))) {
                        System.out.println(Objects.requireNonNull(getItem(i).getContactDB()).getFirstName() + " " + Objects.requireNonNull(getItem(i).getContactDB()).getLastName());
                        ((MainActivity) context).longRecyclerItemClick(i);
//                        ((MainActivity) context).longRecyclerItemClick(i, true);
                        listOfItemSelected.remove(gestionnaireContacts.getContacts().get(i));
                    }
                }
            }
        }
        secondClick = true;
        notifyDataSetChanged();
    }

    private ArrayList<ContactWithAllInformation> listOfItemSelected = new ArrayList<>();

    private String numberForPermission = "";

    public ContactRecyclerViewAdapter(Context context, ContactList gestionnaireContacts, Integer len) {
        this.context = context;
        this.listContacts = gestionnaireContacts.getContacts();
        this.len = len;
        this.gestionnaireContacts = gestionnaireContacts;
        lastSelectMenuLen1 = null;
    }

    public ContactWithAllInformation getItem(int position) {
        return listContacts.get(position);
    }

    public void setGestionnaireContact(ContactList gestionnaireContact) {
        this.gestionnaireContacts = gestionnaireContact;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (len == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_contact_item_layout_smaller, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_contact_item_layout, parent, false);
        }

        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactViewHolder holder, final int position) {
        final ContactDB contact = getItem(position).getContactDB();
        assert contact != null;

        if (len == 0) {
            if (contact.getContactPriority() == 0) {
                holder.contactFirstNameView.setTextColor(context.getResources().getColor(R.color.priorityZeroColor));
            } else if (contact.getContactPriority() == 2) {
                holder.contactFirstNameView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            }
        } else {
            if (contact.getContactPriority() == 0) {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZeroColor));
            } else if (contact.getContactPriority() == 1) {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.lightColor));
            } else if (contact.getContactPriority() == 2) {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwoColor));
            }

        }
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            System.out.println(contact.getProfilePicture());
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
        }

        String contactName = contact.getFirstName() + " " + contact.getLastName();

        holder.contactFirstNameView.setText(contactName);
        String group = "";
        GroupDB firstGroup = getItem(position).getFirstGroup(context);
        if (context instanceof GroupActivity) {
            holder.groupWordingConstraint.setVisibility(View.VISIBLE);
            if (len == 0) {
                holder.contactRoundedImageView.setVisibility(View.INVISIBLE);
            }
        }
        if (firstGroup == null) {
            System.out.println("no group " + contact.getFirstName() + " " + contact.getLastName());
            SharedPreferences sharedThemePreferences = context.getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE);
            if (sharedThemePreferences.getBoolean("darkTheme", false)) {
                Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColorDark), PorterDuff.Mode.MULTIPLY);
                holder.groupWordingConstraint.setBackground(roundedLayout);
                System.out.println(" black color");
            } else {
                Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColor), PorterDuff.Mode.MULTIPLY);
                holder.groupWordingConstraint.setBackground(roundedLayout);
            }
            //Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            //roundedLayout.setColorFilter(Color.parseColor("#f0f0f0"), PorterDuff.Mode.MULTIPLY);
            //holder.groupWordingConstraint.setBackground(roundedLayout);
        } else {
            System.out.println("have group");
            group = firstGroup.getName();
            Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            assert roundedLayout != null;
            roundedLayout.setColorFilter(firstGroup.randomColorGroup(this.context), PorterDuff.Mode.MULTIPLY);
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
            if (listOfItemSelected.contains(gestionnaireContacts.getContacts().get(position))) {
                if (context instanceof GroupActivity && len == 0) {
                    holder.constraintLayoutSmaller.setBackgroundColor(context.getResources().getColor(R.color.priorityTwoColor));
                } else {
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_contact_selected);
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
                ContactGesture.INSTANCE.openWhatsapp(converter06To33(contactWithAllInformation.getFirstPhoneNumber()), context);
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
                    Intent intent = new Intent(context, ContactDetailsActivity.class);
                    intent.putExtra("ContactId", contact.getId());
                    context.startActivity(intent);
                }
            }
        };

        View.OnLongClickListener longClick = v -> {
            if (listOfItemSelected.size() == 0 && len == 1 && holder.constraintLayoutMenu != null) {
                holder.constraintLayoutMenu.setVisibility(View.GONE);
            }

            view.setTag(holder);
            ContactDB contact1 = gestionnaireContacts.getContacts().get(position).getContactDB();
            assert contact1 != null;

            if (listOfItemSelected.contains(gestionnaireContacts.getContacts().get(position))) {
                listOfItemSelected.remove(gestionnaireContacts.getContacts().get(position));

                if (!contact1.getProfilePicture64().equals("")) {
                    Bitmap bitmap = base64ToBitmap(contact1.getProfilePicture64());
                    holder.contactRoundedImageView.setImageBitmap(bitmap);
                } else {
                    holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact1.getProfilePicture()));
                }
            } else {
                listOfItemSelected.add(gestionnaireContacts.getContacts().get(position));
                if (context instanceof GroupActivity && len == 0) {
                    holder.constraintLayoutSmaller.setBackgroundColor(context.getResources().getColor(R.color.priorityTwoColor));
                } else {
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_contact_selected);
                }
            }

            if (context instanceof GroupActivity) {
                ((GroupActivity) context).longRecyclerItemClick(position, false, false);
            } else if (context instanceof MainActivity) {
                ((MainActivity) context).longRecyclerItemClick(position);
//                ((MainActivity) context).longRecyclerItemClick(position, false);
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

            } else {
                if (lastClick) {
                    lastClick = false;
                } else {
                    if (len == 0) {
                        Intent intent = new Intent(context, ContactDetailsActivity.class);
                        intent.putExtra("ContactId", contact.getId());
                        context.startActivity(intent);
                    } else if (len == 1) {
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
            if (whatsappIsNotInstalled()) {
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
            if (whatsappIsNotInstalled()) {
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
        } else {
            holder.constraintLayoutSmaller.setOnLongClickListener(longClick);
            holder.constraintLayoutSmaller.setOnClickListener(listItemClick);
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

            System.out.println("list contact grid size" + gestionnaireContacts.getContacts().size());
            if (!secondClick) {
                if (!modeMultiSelect) {
                    modeMultiSelect = true;
                    for (int i = 0; i < gestionnaireContacts.getContacts().size(); i++) {
                        if (gestionnaireContacts.getContacts().get(i).getFirstGroup(context) != null) {
                            if (Objects.equals(Objects.requireNonNull(gestionnaireContacts.getContacts().get(i).getFirstGroup(context)).getId(), gestionnaireContacts.getContacts().get(position).getFirstGroup(context).getId())) {
                                if (!listOfItemSelected.contains(gestionnaireContacts.getContacts().get(i))) {
                                    System.out.println(Objects.requireNonNull(getItem(i).getContactDB()).getFirstName() + " " + Objects.requireNonNull(getItem(i).getContactDB()).getLastName());
                                    ((GroupActivity) context).longRecyclerItemClick(i, secondClick, true);
                                    listOfItemSelected.add(gestionnaireContacts.getContacts().get(i));
                                }
                            }
                        }
                    }
                    secondClick = true;
                    notifyDataSetChanged();
                }
            } else {
                for (int i = 0; i < gestionnaireContacts.getContacts().size(); i++) {
                    if (gestionnaireContacts.getContacts().get(i).getFirstGroup(context) != null) {
                        if (Objects.equals(Objects.requireNonNull(gestionnaireContacts.getContacts().get(i).getFirstGroup(context)).getId(), gestionnaireContacts.getContacts().get(position).getFirstGroup(context).getId())) {
                            if (listOfItemSelected.contains(gestionnaireContacts.getContacts().get(i))) {
                                ((GroupActivity) context).longRecyclerItemClick(i, secondClick, true);
                                listOfItemSelected.remove(gestionnaireContacts.getContacts().get(i));
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

    class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactFirstNameView;
        RelativeLayout constraintLayout;
        ConstraintLayout constraintLayoutMenu;
        AppCompatImageView listContactItemFavoriteShine;
        CircularImageView contactRoundedImageView;
        ConstraintLayout constraintLayoutSmaller;
        ConstraintLayout constraintLayoutMenuSmaller;

        ConstraintLayout callCl;
        ConstraintLayout smsCl;
        ConstraintLayout whatsappCl;
        ConstraintLayout mailCl;
        ConstraintLayout editCl;

        ConstraintLayout groupWordingConstraint;
        TextView groupWordingTv;
        Boolean open;

        ContactViewHolder(@NonNull View view) {
            super(view);

            if (len == 0) {
                contactRoundedImageView = view.findViewById(R.id.list_contact_item_contactRoundedImageView);
                contactFirstNameView = view.findViewById(R.id.list_contact_item_contactFirstName);
                constraintLayoutSmaller = view.findViewById(R.id.list_contact_item_layout_smaller);
                constraintLayoutMenuSmaller = view.findViewById(R.id.list_contact_item_menu_smaller);
                callCl = view.findViewById(R.id.list_contact_item_smaller_constraint_call);
                smsCl = view.findViewById(R.id.list_contact_item_smaller_constraint_sms);
                whatsappCl = view.findViewById(R.id.list_contact_item_smaller_constraint_whatsapp);
                mailCl = view.findViewById(R.id.list_contact_item_smaller_constraint_mail);
                groupWordingConstraint = view.findViewById(R.id.list_contact_item_wording_group_constraint_layout);
                groupWordingTv = view.findViewById(R.id.list_contact_item_wording_group_tv);
                listContactItemFavoriteShine = view.findViewById(R.id.list_contact_item_favorite_shine);
                open = false;
            } else {
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

    }

    public String getPhonePermission() {
        return numberForPermission;
    }
}