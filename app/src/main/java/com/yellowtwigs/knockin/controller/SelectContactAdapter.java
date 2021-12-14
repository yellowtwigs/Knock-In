package com.yellowtwigs.knockin.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.yellowtwigs.knockin.R;
import com.yellowtwigs.knockin.controller.activity.HomeActivity;
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity;
import com.yellowtwigs.knockin.model.ContactManager;
import com.yellowtwigs.knockin.models.data.ContactDB;
import com.yellowtwigs.knockin.models.data.ContactWithAllInformation;
import com.yellowtwigs.knockin.models.data.GroupDB;

import java.util.ArrayList;

public class SelectContactAdapter extends BaseAdapter {

    private ContactManager gestionnaireContact;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;
    private ArrayList<ContactWithAllInformation> listSelectedItem;
    private Boolean secondClick = true;

    public SelectContactAdapter(Context context, ContactManager contactManager, Integer len) {
        this.context = context;
        this.gestionnaireContact = contactManager;
        this.len = len;
        layoutInflater = LayoutInflater.from(context);
        listSelectedItem = new ArrayList<>();
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
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridview = convertView;
        final ViewHolder holder;

        if (gridview == null) {
            gridview = layoutInflater.inflate(R.layout.grid_multi_select_item_layout, null);

            holder = new ViewHolder();
            holder.contactRoundedImageView = gridview.findViewById(R.id.contactRoundedImageView);
            holder.multi_select_ContactLayout = gridview.findViewById(R.id.multi_select_contact_layout);

            int height = holder.contactRoundedImageView.getLayoutParams().height;
            int width = holder.contactRoundedImageView.getLayoutParams().width;

            holder.contactFirstNameView = gridview.findViewById(R.id.grid_adapter_contactFirstName);
            RelativeLayout.LayoutParams layoutParamsTV = (RelativeLayout.LayoutParams) holder.contactFirstNameView.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParamsIV = (ConstraintLayout.LayoutParams) holder.contactRoundedImageView.getLayoutParams();

            if (len == 3) {
                holder.contactRoundedImageView.getLayoutParams().height -= height * 0.05;
                holder.contactRoundedImageView.getLayoutParams().width -= height * 0.05;
                layoutParamsTV.topMargin = 30;
                layoutParamsIV.topMargin = 10;
            } else if (len == 4) {
                holder.contactRoundedImageView.getLayoutParams().height -= height * 0.25;
                holder.contactRoundedImageView.getLayoutParams().width -= width * 0.25;
                layoutParamsTV.topMargin = 10;
                layoutParamsIV.topMargin = 10;
            } else if (len == 5) {
                holder.contactRoundedImageView.getLayoutParams().height -= height * 0.40;
                holder.contactRoundedImageView.getLayoutParams().width -= width * 0.40;
                layoutParamsTV.topMargin = 0;
                layoutParamsIV.topMargin = 0;
            } else if (len == 6) {
                holder.contactRoundedImageView.getLayoutParams().height -= height * 0.50;
                holder.contactRoundedImageView.getLayoutParams().width -= width * 0.50;
                layoutParamsTV.topMargin = 0;
                layoutParamsIV.topMargin = 0;
            }

            holder.contactLastNameView = gridview.findViewById(R.id.grid_adapter_contactLastName);

            gridview.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.isSelect = false;

        final ContactDB contact = getItem(position).getContactDB();

        assert contact != null;

        String firstname = contact.getFirstName();
        String lastName = contact.getLastName();
        String group = "";

        GroupDB firstGroup = getItem(position).getFirstGroup(context);
        if (context instanceof GroupManagerActivity) {
            if (len == 0) {
                holder.contactRoundedImageView.setVisibility(View.INVISIBLE);
            }
        }
        if (firstGroup == null) {
            System.out.println("no group " + contact.getFirstName() + " " + contact.getLastName());
            SharedPreferences sharedThemePreferences = context.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE);
            if (sharedThemePreferences.getBoolean("darkTheme", false)) {
                Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColorDark, null), PorterDuff.Mode.MULTIPLY);
                System.out.println(" black color");
            } else {
                Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColor, null), PorterDuff.Mode.MULTIPLY);
            }

        } else {
            System.out.println("have group");
            group = firstGroup.getName();
            Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            assert roundedLayout != null;
//            roundedLayout.setColorFilter(firstGroup.randomColorGroup(this.context), PorterDuff.Mode.MULTIPLY);
            roundedLayout.setColorFilter(firstGroup.getSection_color(), PorterDuff.Mode.MULTIPLY);
        }
        if (len == 3) {
            Spannable spanFistName = new SpannableString(firstname);
            spanFistName.setSpan(new RelativeSizeSpan(0.95f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);
            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.95f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
            //holder.contactFirstNameView.;
            Spannable spanGroup = new SpannableString(group);
            spanGroup.setSpan(new RelativeSizeSpan(0.95f), 0, group.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        if (len == 4) {
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

            if (group.length() > 8)
                group = group.substring(0, 7).concat("..");
            Spannable spanGroup = new SpannableString(group);
            spanLastName.setSpan(new RelativeSizeSpan(0.95f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        if (len == 5) {
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

            if (group.length() > 9)
                group = group.substring(0, 7).concat("..");
            Spannable spanGroup = new SpannableString(group);
            spanLastName.setSpan(new RelativeSizeSpan(0.9f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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

            if (group.length() > 6)
                group = group.substring(0, 5).concat("..");
            Spannable spanGroup = new SpannableString(group);
            spanLastName.setSpan(new RelativeSizeSpan(0.81f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());

            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture())); //////////////
        }

        if (context instanceof HomeActivity || context instanceof GroupManagerActivity) {
            if (listSelectedItem.contains(getItem(position))) {
                holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
            } else {
                if (!contact.getProfilePicture64().equals("")) {
                    Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());

                    holder.contactRoundedImageView.setImageBitmap(bitmap);
                } else {
                    holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture())); //////////////
                }
            }
        } else {
            if (listSelectedItem.contains(getItem(position))) {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwoColor, null));
            } else {
                holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.lightColor, null));
            }
        }

        return gridview;
    }

    static class ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        CircularImageView contactRoundedImageView;
        ConstraintLayout multi_select_ContactLayout;
        Boolean isSelect;
    }


    private Bitmap base64ToBitmap(String base64) {

        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    public void itemSelected(int position) {

        ContactWithAllInformation contact = getItem(position);

        if (listSelectedItem.contains(contact)) {
            listSelectedItem.remove(contact);
        } else {
            if (listSelectedItem.size() >= 5) {
            } else {
                listSelectedItem.add(contact);
            }
        }
    }

    public void itemDeselected() {

        listSelectedItem.clear();
    }

    public ArrayList<ContactWithAllInformation> getListContactSelect() {
        return listSelectedItem;
    }

    private int randomDefaultImage(int avatarId) {

        SharedPreferences sharedPreferencesIsMultiColor = context.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE);
        int multiColor = sharedPreferencesIsMultiColor.getInt("IsMultiColor", 0);

        SharedPreferences sharedPreferencesContactsColor = context.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE);
        int contactsColorPosition = sharedPreferencesContactsColor.getInt("IsMultiColor", 0);

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
}