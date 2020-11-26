package com.yellowtwigs.knockin.controller.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.yellowtwigs.knockin.controller.ContactRecyclerViewAdapter;
import com.yellowtwigs.knockin.controller.activity.group.AddContactToGroupActivity;
import com.yellowtwigs.knockin.controller.activity.group.AddNewGroupActivity;
import com.yellowtwigs.knockin.controller.activity.group.DeleteContactFromGroupActivity;
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity;
import com.yellowtwigs.knockin.model.ContactManager;
import com.yellowtwigs.knockin.model.ModelDB.ContactDB;
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupListViewAdapter extends  RecyclerView.Adapter<CreateGroupListViewAdapter.ContactViewHolder>{

    private List<ContactWithAllInformation> listContacts;
    private View view;
    private Integer len;
    private Map<Integer, Integer> contactMap = new HashMap<>();
    private List<ContactWithAllInformation> listContact;
    private Integer positionOnBindViewHolder;
    private ContactManager gestionnaireContacts;
    private Context context;
    private Boolean isCreated;

    private ConstraintLayout lastSelectMenuLen1;

    public ArrayList<ContactWithAllInformation> getListOfItemSelected() {
        return listOfItemSelected;
    }

    public void setListOfItemSelected(ArrayList<ContactWithAllInformation> listOfItemSelected) {
        this.listOfItemSelected.clear();
        this.listOfItemSelected.addAll(listOfItemSelected);
    }

    private ArrayList<ContactWithAllInformation> listOfItemSelected = new ArrayList<>();

    public CreateGroupListViewAdapter(Context context, ContactManager gestionnaireContacts, Integer len, List<ContactWithAllInformation> listContact) {
        this.context = context;
        this.len = len;
        this.gestionnaireContacts = gestionnaireContacts;
        this.listContact = listContact;
        this.positionOnBindViewHolder = 0;
        this.listContacts = gestionnaireContacts.getContactList();
        lastSelectMenuLen1 = null;
    }

    public void setGestionnaireContact(ContactManager gestionnaireContact) {
        this.gestionnaireContacts = gestionnaireContact;
    }

    public ContactWithAllInformation getItem(int position) {
        return listContacts.get(position);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_contact_item_layout, parent, false);

        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        if (context instanceof AddContactToGroupActivity || context instanceof DeleteContactFromGroupActivity) {
            Boolean isContained = true;
            System.out.println("......................................................ooooooooooooooooooooooooooooooooooooooo.");
            ContactDB actContact = getItem(positionOnBindViewHolder).getContactDB();
            while (isContained) {
                for (ContactWithAllInformation actualContact : listContact) {
                    if (actualContact.getContactId() == actContact.getId()) {
                        isContained = false;
                        if (!contactMap.containsKey(position))
                            contactMap.put(position, positionOnBindViewHolder);
                    }
                }
                if (isContained) {
                    if (positionOnBindViewHolder != listContacts.size()-1)
                        positionOnBindViewHolder = positionOnBindViewHolder + 1;
                    actContact = getItem(positionOnBindViewHolder).getContactDB();
                }
            }
        }
        if (context instanceof AddNewGroupActivity) {
            if (!contactMap.containsKey(position))
                contactMap.put(position, positionOnBindViewHolder);
        }
        final ContactDB contact = getItem(contactMap.get(position)).getContactDB();
        System.out.println(contact);
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
            System.out.println(contact.getProfilePicture());
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
        }
        String contactName = contact.getFirstName() + " " + contact.getLastName();

        holder.contactFirstNameView.setText(contactName);
        //
        //

        if (listOfItemSelected.contains(gestionnaireContacts.getContactList().get(contactMap.get(position)))) {
                holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
        }

        //((AddNewGroupActivity) context).listMultiSelectItemClick(position);

        View.OnClickListener listItemClick = v -> {
            if (listOfItemSelected.size() == 0 && len == 1 && holder.constraintLayoutMenu != null) {
                holder.constraintLayoutMenu.setVisibility(View.GONE);
            }

            view.setTag(holder);
            ContactDB contactDB = gestionnaireContacts.getContactList().get(contactMap.get(position)).getContactDB();
            assert contactDB != null;

            if (listOfItemSelected.contains(gestionnaireContacts.getContactList().get(contactMap.get(position)))) {
                listOfItemSelected.remove(gestionnaireContacts.getContactList().get(contactMap.get(position)));

                if (!contactDB.getProfilePicture64().equals("")) {
                    Bitmap bitmap = base64ToBitmap(contactDB.getProfilePicture64());
                    holder.contactRoundedImageView.setImageBitmap(bitmap);
                } else {
                    holder.contactRoundedImageView.setImageResource(randomDefaultImage(contactDB.getProfilePicture()));
                }
            } else {
                listOfItemSelected.add(gestionnaireContacts.getContactList().get(contactMap.get(position)));
                if (context instanceof GroupManagerActivity && len == 0) {
                } else {
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected);
                }
            }

            if (context instanceof AddNewGroupActivity) {
                ((AddNewGroupActivity) context).listMultiSelectItemClick(contactMap.get(position));
            }
            if (context instanceof AddContactToGroupActivity) {
                ((AddContactToGroupActivity) context).multiSelectItemClick(contactMap.get(position));
            }
            if (context instanceof DeleteContactFromGroupActivity) {
                ((DeleteContactFromGroupActivity) context).multiSelectItemClick(contactMap.get(position));
            }
        };

        if (holder.constraintLayout != null) {
            holder.constraintLayout.setOnClickListener(listItemClick);
        }
        
        if (contact.getFavorite() == 1) {
            holder.listContactItemFavoriteShine.setVisibility(View.VISIBLE);
        } else {
            holder.listContactItemFavoriteShine.setVisibility(View.GONE);
        }
        if (positionOnBindViewHolder != listContacts.size()-1)
            positionOnBindViewHolder = positionOnBindViewHolder + 1 ;
    }

    @Override
    public long getItemId(int position) {
        return listContacts.size();
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (context instanceof AddNewGroupActivity) {
            itemCount = listContacts.size();
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

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactFirstNameView;
        RelativeLayout constraintLayout;
        ConstraintLayout constraintLayoutMenu;
        AppCompatImageView listContactItemFavoriteShine;
        CircularImageView contactRoundedImageView;

        ConstraintLayout groupWordingConstraint;
        TextView groupWordingTv;
        Boolean open;

        ContactViewHolder(@NonNull View view) {
            super(view);

            contactRoundedImageView = view.findViewById(R.id.list_contact_item_contactRoundedImageView);
            contactFirstNameView = view.findViewById(R.id.list_contact_item_contactFirstName);
            constraintLayout = view.findViewById(R.id.list_contact_item_layout);
            constraintLayoutMenu = view.findViewById(R.id.list_contact_item_menu);
            groupWordingConstraint = view.findViewById(R.id.list_contact_wording_group_constraint_layout);
            groupWordingTv = view.findViewById(R.id.list_contact_wording_group_tv);
            listContactItemFavoriteShine = view.findViewById(R.id.list_contact_item_favorite_shine);
            open = false;
        }
    }

}
