//package com.yellowtwigs.knockin.ui.group;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.PorterDuff;
//import android.graphics.drawable.Drawable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.widget.AppCompatImageView;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.yellowtwigs.knockin.R;
//import com.yellowtwigs.knockin.model.database.ContactsDatabase;
//import com.yellowtwigs.knockin.model.DbWorkerThread;
//import com.yellowtwigs.knockin.model.data.ContactWithAllInformation;
//import com.yellowtwigs.knockin.model.database.GroupDB;
//import com.google.android.material.dialog.MaterialAlertDialogBuilder;
//
//import java.util.ArrayList;
//import java.util.Objects;
//
//public class GroupEditAdapter extends RecyclerView.Adapter<GroupEditAdapter.ViewHolder> {
//    private ArrayList<GroupDB> listGroup;
//    Context context;
//    private ContactWithAllInformation contact;
//
//    public GroupEditAdapter(Context context, ArrayList<GroupDB> listGroup, ContactWithAllInformation contact) {
//        this.context = context;
//        this.listGroup = listGroup;
//        this.contact = contact;
//    }
//
//    @NonNull
//    @Override
//    public GroupEditAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View view = inflater.inflate(R.layout.group_item_in_edit_contact, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @SuppressLint("StringFormatInvalid")
//    @Override
//    public void onBindViewHolder(@NonNull final GroupEditAdapter.ViewHolder holder, final int position) {
//        holder.close.setOnClickListener(v -> {
//            new MaterialAlertDialogBuilder(context, R.style.AlertDialog)
//                    .setTitle(R.string.delete_contact_from_group_alert_dialog_title)
//                    .setMessage(String.format(context.getString(R.string.delete_contact_from_group_alert_dialog_message), Objects.requireNonNull(contact.getContactDB()).getFirstName() + " " + contact.getContactDB().getLastName(), listGroup.get(position).getName()))
//                    .setPositiveButton(R.string.edit_contact_validate, (dialog, which) -> {
//                        ContactsDatabase ContactsDatabase = ContactsDatabase.Companion.getDatabase(context);
//                        DbWorkerThread mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
//                        mDbWorkerThread.start();
//                        assert ContactsDatabase != null;
//                        ContactsDatabase.LinkContactGroupDao().deleteContactIngroup(contact.getContactId(), Objects.requireNonNull(listGroup.get(position).getId()).intValue());
//
//                        listGroup.remove(position);
//                        notifyDataSetChanged();
//                    }).setNegativeButton(R.string.alert_dialog_cancel, (dialog, which) -> {
//
//            }).show();
//        });
//        Drawable drawable = context.getDrawable(R.drawable.rounded_rectangle_group);
//        assert drawable != null;
////        drawable.setColorFilter(listGroup.get(position).randomColorGroup(context), PorterDuff.Mode.MULTIPLY);
//        drawable.setColorFilter(listGroup.get(position).getSection_color(), PorterDuff.Mode.MULTIPLY);
//        holder.layoutGroup.setBackground(drawable);
//        holder.groupName.setText(listGroup.get(position).getName());
//    }
//
//    public GroupDB getItem(int position) {
//        return listGroup.get(position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return listGroup.size();
//    }
//
//    class ViewHolder extends RecyclerView.ViewHolder {
//        TextView groupName;
//        AppCompatImageView close;
//        ConstraintLayout layoutGroup;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            groupName = itemView.findViewById(R.id.group_item_in_edit_group_name);
//            close = itemView.findViewById(R.id.group_item_in_edit_close);
//            layoutGroup = itemView.findViewById(R.id.group_item_in_edit_constraint_layout);
//        }
//
///*        @Override
//        public void onClick(View view) {
//            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
//        }*/
//    }
//
//}
