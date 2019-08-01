package com.example.knocker.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;
import com.example.knocker.model.ContactsRoomDatabase;
import com.example.knocker.model.DbWorkerThread;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.example.knocker.model.ModelDB.GroupDB;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Objects;

public class GroupEditAdapter extends RecyclerView.Adapter<GroupEditAdapter.ViewHolder> {
    private ArrayList<GroupDB> listGroup;
    Context context;
    private ContactWithAllInformation contact;

    public GroupEditAdapter(Context context, ArrayList<GroupDB> listGroup, ContactWithAllInformation contact) {
        this.context = context;
        this.listGroup = listGroup;
        this.contact = contact;
    }

    @NonNull
    @Override
    public GroupEditAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.group_item_in_edit_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupEditAdapter.ViewHolder holder, final int position) {
        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("click close");
                String test=context.getString(R.string.edit_contact_alert_dialog_group_message);
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.edit_contact_alert_dialog_group_title)
                        .setMessage(String.format(context.getString(R.string.edit_contact_alert_dialog_group_message),contact.getContactDB().getFirstName()+" "+contact.getContactDB().getLastName(),listGroup.get(position).getName()))
                        .setPositiveButton(R.string.edit_contact_validate, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ContactsRoomDatabase ContactsDatabase = ContactsRoomDatabase.Companion.getDatabase(context);
                                DbWorkerThread mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
                                mDbWorkerThread.start();
                                assert ContactsDatabase != null;
                                ContactsDatabase.LinkContactGroupDao().deleteContactIngroup(contact.getContactId(), Objects.requireNonNull(listGroup.get(position).getId()).intValue());

                                listGroup.remove(position);
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

                // holder.layoutGroup.setVisibility(View.GONE);
            }
        });
        Drawable drawable = context.getDrawable(R.drawable.rounded_rectangle_group);
        assert drawable != null;
        drawable.setColorFilter(listGroup.get(position).randomColorGroup(context), PorterDuff.Mode.MULTIPLY);
        holder.layoutGroup.setBackground(drawable);
        holder.groupName.setText(listGroup.get(position).getName());
    }

    public GroupDB getItem(int position) {
        return listGroup.get(position);
    }

    @Override
    public int getItemCount() {
        return listGroup.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        AppCompatImageView close;
        ConstraintLayout layoutGroup;

        ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_item_in_edit_group_name);
            close = itemView.findViewById(R.id.group_item_in_edit_close);
            layoutGroup = itemView.findViewById(R.id.group_item_in_edit_constraint_layout);
        }

/*        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }*/
    }

}
