package com.yellowtwigs.knockin.ui.groups.create;

import android.annotation.SuppressLint;
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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.yellowtwigs.knockin.R;
import com.yellowtwigs.knockin.ui.CircularImageView;
import com.yellowtwigs.knockin.model.database.data.ContactDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter qui nous permet de gérer chaque item de la liste des contact en rapport avec un groupe ces contacts peuvent être supprimer d'un groupe ou utiliser pour créer un groupe
 * @author Ryan Granet, Florian Striebel
 */
public class AddContactToGroupAdapter extends BaseAdapter {

    private List<ContactDB> listContacts;
    private LayoutInflater layoutInflater;
    private ArrayList<ContactDB> selectContact;
    private Context context;

    /**
     * Constructeur de l'adapteur
     * @param context //Activité qui lance l'adapter
     * @param listContacts //List des contact qui ne font pas encore partie du groupe
     */
    public AddContactToGroupAdapter(Context context, List<ContactDB> listContacts) {
        this.listContacts = listContacts;
        layoutInflater = LayoutInflater.from(context);
        selectContact = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount() {
        return listContacts.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public ContactDB getItem(int position) {
        return listContacts.get(position);
    }

    /**
     * Gère pour chaque item son affichage en fonction de sa position
     * Chaque fois que nous affichons de nouveaux éléments sur les écrans nous rappelons cette méthode les item qui sont sorti de l'écran sont détruits
     * @param position [int]
     * @param convertView [View]
     * @param parent [ViewGroup]
     * @return [View]
     */
    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View listview;
        final ViewHolder holder;
        holder = new ViewHolder();
        holder.position = position;
        final ContactDB contact = getItem(position);
        listview = layoutInflater.inflate(R.layout.list_contact_selected_group, null);

        holder.contactRoundedImageView = listview.findViewById(R.id.add_group_contact_list_item_contactRoundedImageView);
        holder.contactFirstNameView = listview.findViewById(R.id.add_group_contact_list_item_contactFirstName);
        holder.contactSelect = listview.findViewById(R.id.add_group_contact_checkbox);

        assert contact != null;

        if (selectContact.contains(contact)) {//vérifie si le contact avait été selectionner avant de revenir à l'écran si oui on valide la checkbox
            holder.contactSelect.setChecked(true);
        } else {
            holder.contactSelect.setChecked(false);
        }

        if (!contact.getProfilePicture64().equals("")) {// récupère la photo contact et l'affiche
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
        }
        String contactName = contact.getFirstName() + " " + contact.getLastName();
        if (contactName.length() > 15) {

            Spannable spanFistName = new SpannableString(contactName);
            spanFistName.setSpan(new RelativeSizeSpan(1.0f), 0, contactName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);

            contactName = contact.getFirstName() + " " + contact.getLastName();
            contactName = contactName.substring(0, 15) + "..";
        }
        holder.contactFirstNameView.setText(contactName);
        holder.contactSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!selectContact.contains(contact)) {
                selectContact.add(contact);
                holder.contactSelect.setChecked(true);
            } else {
                selectContact.remove(contact);
                holder.contactSelect.setChecked(false);
            }
        });
        return listview;
    }

    public List<ContactDB> getAllSelectContact() {
        return selectContact;
    }

    public void setAllSelectContact(ArrayList<ContactDB> selectContact) {
        this.selectContact = selectContact;
    }

    /**
     *Renvoie l'image du contact sous forme de ressource
     * @param avatarId [Int]
     * @return [Int]
     */
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

    /**
     * Convertit les image de base64 en Bitmap
     * @param base64 [String]
     * @return [Bitmap]
     */
    private Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    /**
     * class qui permet pour chaque item de la list de différencier leurs items
     */
    static class ViewHolder {
        TextView contactFirstNameView;
        CircularImageView contactRoundedImageView;
        int position;
        CheckBox contactSelect;
    }
}
