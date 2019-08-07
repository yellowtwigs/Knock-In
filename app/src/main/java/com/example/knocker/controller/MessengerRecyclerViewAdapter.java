package com.example.knocker.controller;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;
import com.example.knocker.controller.activity.EditContactActivity;
import com.example.knocker.controller.activity.MainActivity;
import com.example.knocker.controller.activity.MessengerActivity;
import com.example.knocker.controller.activity.group.GroupActivity;
import com.example.knocker.model.ContactGesture;
import com.example.knocker.model.ContactList;
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
 * @author Florian Striebel, Kenzy Suon
 */
public class MessengerRecyclerViewAdapter extends RecyclerView.Adapter<MessengerRecyclerViewAdapter.MessengerViewHolder> {
    private List<ContactWithAllInformation> listContacts;
    private Context context;
    private View view;
    private ContactList gestionnaireContacts;
    private Boolean modeMultiSelect = false;
    private Boolean lastClick = false;

    public ContactWithAllInformation getItem(int position) {
        return listContacts.get(position);
    }

    private ArrayList<ContactWithAllInformation> listOfItemSelected = new ArrayList<>();

    public MessengerRecyclerViewAdapter(Context context, ContactList gestionnaireContacts) {
        this.context = context;
        this.listContacts = gestionnaireContacts.getContacts();
        this.gestionnaireContacts = gestionnaireContacts;
    }

    @NonNull
    @Override
    public MessengerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messenger_recycler_item_layout, parent, false);

        return new MessengerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessengerViewHolder holder, final int position) {
        final ContactDB contact = getItem(position).getContactDB();
        assert contact != null;

        String contactName = contact.getFirstName() + " " + contact.getLastName();

        holder.messengerRecyclerItemContactFirstName.setText(contactName);

        View.OnLongClickListener longClick = v -> {
            view.setTag(holder);
            ContactDB contact1 = gestionnaireContacts.getContacts().get(position).getContactDB();
            assert contact1 != null;

//            if (context instanceof MessengerActivity) {
//                ((MessengerActivity) context).longRecyclerItemClick(position);
//            }

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
                    Intent intent = new Intent(context, EditContactActivity.class);
                    intent.putExtra("ContactId", contact.getId());
                    context.startActivity(intent);
                }
            }
        };

        if (holder.messengerRecyclerItemLayout != null) {
            holder.messengerRecyclerItemLayout.setOnLongClickListener(longClick);
            holder.messengerRecyclerItemLayout.setOnClickListener(listItemClick);
        }
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

    class MessengerViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout messengerRecyclerItemLayout;
        CircularImageView messengerRecyclerItemContactImage;
        TextView messengerRecyclerItemContactFirstName;
        TextView messengerRecyclerItemContactLastMessage;
        TextView messengerRecyclerItemContactLastMessageTime;

        MessengerViewHolder(@NonNull View view) {
            super(view);

            messengerRecyclerItemLayout = view.findViewById(R.id.messenger_recycler_item_layout);
            messengerRecyclerItemContactImage = view.findViewById(R.id.messenger_recycler_item_contact_image);
            messengerRecyclerItemContactFirstName = view.findViewById(R.id.messenger_recycler_item_contact_first_name);
            messengerRecyclerItemContactLastMessage = view.findViewById(R.id.messenger_recycler_item_last_message);
            messengerRecyclerItemContactLastMessageTime = view.findViewById(R.id.messenger_recycler_item_last_message_time);
        }

    }
}