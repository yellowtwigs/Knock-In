package com.yellowtwigs.knockin.controller.adapter;

import android.content.Context;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.yellowtwigs.knockin.R;

import com.yellowtwigs.knockin.controller.CircularImageView;
import com.yellowtwigs.knockin.controller.activity.MainActivity;
import com.yellowtwigs.knockin.controller.activity.group.AddNewGroupActivity;
import com.yellowtwigs.knockin.controller.activity.group.AddContactToGroupActivity;
import com.yellowtwigs.knockin.controller.activity.group.DeleteContactFromGroupActivity;
import com.yellowtwigs.knockin.model.ContactManager;

import com.yellowtwigs.knockin.model.ModelDB.ContactDB;
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupGridViewAdapter extends RecyclerView.Adapter<CreateGroupGridViewAdapter.ViewHolder> {
    private ContactManager gestionnaireContact;
    private Context context;
    private Integer len;
    private Map<Integer, Integer> contactMap = new HashMap<>();
    private Integer positionOnBindViewHolder;
    private List<ContactWithAllInformation> listContact;
    private LayoutInflater layoutInflater;
    private Boolean modeMultiSelect = true;
    private ArrayList<ContactWithAllInformation> listOfItemSelected = new ArrayList<>();
    private int heightWidthImage;

    public CreateGroupGridViewAdapter(Context context, ContactManager contactManager, Integer len, List<ContactWithAllInformation> listContact) {
        this.context = context;
        this.gestionnaireContact = contactManager;
        this.len = len;
        this.listContact = listContact;
        this.positionOnBindViewHolder = 0;
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
        System.out.println(parent.getClass());
//        parentGrid = ((GridView) parent);
        CreateGroupGridViewAdapter.ViewHolder holder = new CreateGroupGridViewAdapter.ViewHolder(view);

        heightWidthImage = holder.contactRoundedImageView.getLayoutParams().height;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CreateGroupGridViewAdapter.ViewHolder holder, int position) {
        Boolean addedInMap = false;
        if (context instanceof AddContactToGroupActivity || context instanceof DeleteContactFromGroupActivity) {
            ContactDB actContact = this.gestionnaireContact.getContactList().get(positionOnBindViewHolder).getContactDB();
            Boolean isContained = true; // dans le groupe
            while (isContained) {
                for (ContactWithAllInformation actualContact : listContact) { // liste des contact pas dans le groupe
                    if (actualContact.getContactId() == actContact.getId()) {
                        if (!contactMap.containsValue(actContact.getId())) {
                            isContained = false;
                            if (!contactMap.containsKey(position)) {
                                contactMap.put(position, positionOnBindViewHolder);
                                addedInMap = true;
                            }
                        }
                    }
                }
                if (isContained) {
                    if (positionOnBindViewHolder != this.gestionnaireContact.getContactList().size()-1)
                        positionOnBindViewHolder = positionOnBindViewHolder + 1;
                    actContact = this.gestionnaireContact.getContactList().get(positionOnBindViewHolder).getContactDB();
                }
            }
        }
        if (context instanceof AddNewGroupActivity) {
            if (!contactMap.containsKey(position)) {
                contactMap.put(position, positionOnBindViewHolder);
                addedInMap = true;
            }
        }
        final ContactDB contact = this.gestionnaireContact.getContactList().get(contactMap.get(position)).getContactDB();

            int height = heightWidthImage;
            int width = heightWidthImage;

            RelativeLayout.LayoutParams layoutParamsTV = (RelativeLayout.LayoutParams) holder.contactFirstNameView.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParamsIV = (ConstraintLayout.LayoutParams) holder.contactRoundedImageView.getLayoutParams();

            if (!modeMultiSelect || !listOfItemSelected.contains(gestionnaireContact.getContactList().get(contactMap.get(position)))) {
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

            if (contact.getFavorite() == 1) {
                holder.gridAdapterFavoriteShine.setVisibility(View.VISIBLE);
            } else {
                holder.gridAdapterFavoriteShine.setVisibility(View.GONE);
            }

            ///((AddNewGroupActivity) context).gridMultiSelectItemClick(position); ////////

            View.OnClickListener gridItemClick = v -> {
                if (listOfItemSelected.contains(gestionnaireContact.getContactList().get(contactMap.get(position)))) {
                    listOfItemSelected.remove(gestionnaireContact.getContactList().get(contactMap.get(position)));

                    if (!contact.getProfilePicture64().equals("")) {
                        Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
                        holder.contactRoundedImageView.setImageBitmap(bitmap);
                    } else {
                        holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
                    }
                /*if (listOfItemSelected.isEmpty()) {
                    modeMultiSelect = false;
                }*/
                } else {
                    listOfItemSelected.add(gestionnaireContact.getContactList().get(contactMap.get(position)));
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
                }
                if (context instanceof AddNewGroupActivity) {
                    ((AddNewGroupActivity) context).gridMultiSelectItemClick(contactMap.get(position));
                }
                if (context instanceof AddContactToGroupActivity) {
                    ((AddContactToGroupActivity) context).multiSelectItemClick(contactMap.get(position));
                }
                if (context instanceof DeleteContactFromGroupActivity) {
                    ((DeleteContactFromGroupActivity) context).multiSelectItemClick(contactMap.get(position));
                }
            };

            holder.gridContactItemLayout.setOnClickListener(gridItemClick);
            holder.contactRoundedImageView.setOnClickListener(gridItemClick);
        if (addedInMap && positionOnBindViewHolder != this.gestionnaireContact.getContactList().size()-1)
            positionOnBindViewHolder = positionOnBindViewHolder + 1 ;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (context instanceof AddNewGroupActivity) {
            itemCount = gestionnaireContact.getContactList().size();
        }
        if (context instanceof AddContactToGroupActivity || context instanceof DeleteContactFromGroupActivity) {
            itemCount = listContact.size();
        }
        return itemCount;
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

    private Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
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

}
