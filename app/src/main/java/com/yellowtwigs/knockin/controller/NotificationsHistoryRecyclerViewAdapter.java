package com.yellowtwigs.knockin.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.yellowtwigs.knockin.R;
import com.yellowtwigs.knockin.controller.activity.MainActivity;
import com.yellowtwigs.knockin.controller.activity.NotificationHistoryActivity;
import com.yellowtwigs.knockin.controller.activity.group.GroupActivity;
import com.yellowtwigs.knockin.model.ContactsRoomDatabase;
import com.yellowtwigs.knockin.model.ModelDB.ContactDB;
import com.yellowtwigs.knockin.model.ModelDB.NotificationDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * La Classe qui permet de remplir l'historique des notifications
 *
 * @author Florian Striebel
 */
public class NotificationsHistoryRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsHistoryRecyclerViewAdapter.NotificationHistoryViewHolder> {

    private List<NotificationDB> notification_history_ListOfNotificationDB;
    private Context context;
    private View view;
    private Boolean modeMultiSelect = false;
    private Boolean secondClick = false;
    private Boolean lastClick = false;

    public ArrayList<NotificationDB> getListOfItemSelected() {
        return listOfItemSelected;
    }

    public void setListOfItemSelected(ArrayList<NotificationDB> listOfItemSelected) {
        this.listOfItemSelected.clear();
        this.listOfItemSelected.addAll(listOfItemSelected);
    }

    private ArrayList<NotificationDB> listOfItemSelected = new ArrayList<>();

    public NotificationsHistoryRecyclerViewAdapter(Context context, List<NotificationDB> notifications) {
        this.context = context;
        this.notification_history_ListOfNotificationDB = notifications;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public NotificationHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notification_history, parent, false);
        return new NotificationHistoryViewHolder(view);
    }

    public NotificationDB getItem(int position) {
        return notification_history_ListOfNotificationDB.get(position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull NotificationHistoryViewHolder holder, int position) {
        ContactsRoomDatabase notif_history_ContactsDatabase = ContactsRoomDatabase.Companion.getDatabase(context);
        assert notif_history_ContactsDatabase != null;
        notification_history_ListOfNotificationDB.addAll(notif_history_ContactsDatabase.notificationsDao().getAllNotifications());
//        ArrayList<NotificationDB>
        NotificationDB notif = getItem(position);
        String text = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(notif.getTimestamp()));
        PackageManager pckManager = context.getPackageManager();
        Drawable icon = null;
        try {
            icon = pckManager.getApplicationIcon(notif.getPlatform());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final Drawable icon2 = icon;

        holder.notif_history_item_AppImage.setImageDrawable(icon);
        if (notif.getDescription().length() > 100) {
            holder.notif_history_item_NotificationContent.setText(notif.getDescription().substring(0, 99) + "..");
        } else {
            holder.notif_history_item_NotificationContent.setText(notif.getDescription());
        }
        holder.notif_history_item_NotificationDate.setText(text);
        holder.notif_history_item_SenderName.setText(notif.getContactName());

        View.OnLongClickListener longClick = v -> {
            view.setTag(holder);

            if (listOfItemSelected.contains(notification_history_ListOfNotificationDB.get(position))) {
                listOfItemSelected.remove(notification_history_ListOfNotificationDB.get(position));
                holder.notif_history_item_AppImage.setImageDrawable(icon2);
            } else {
                listOfItemSelected.add(notification_history_ListOfNotificationDB.get(position));
                holder.notif_history_item_AppImage.setImageResource(R.drawable.ic_item_selected);
            }

            if (context instanceof NotificationHistoryActivity) {
                ((NotificationHistoryActivity) context).recyclerLongClick(position);
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

        View.OnClickListener click = v -> {
            if (modeMultiSelect) {
                view.setTag(holder);

                if (listOfItemSelected.contains(notification_history_ListOfNotificationDB.get(position))) {
                    listOfItemSelected.remove(notification_history_ListOfNotificationDB.get(position));
                    holder.notif_history_item_AppImage.setImageDrawable(icon2);
                } else {
                    listOfItemSelected.add(notification_history_ListOfNotificationDB.get(position));
                    holder.notif_history_item_AppImage.setImageResource(R.drawable.ic_item_selected);
                }

                if (context instanceof NotificationHistoryActivity) {
                    ((NotificationHistoryActivity) context).recyclerLongClick(position);
                }

                if (listOfItemSelected.size() > 0) {
                    modeMultiSelect = true;
                    lastClick = false;
                } else {
                    modeMultiSelect = false;
                    lastClick = true;
                }
            } else {
                if (context instanceof NotificationHistoryActivity) {
                    ((NotificationHistoryActivity) context).recyclerSimpleClick(position);
                }
            }
        };

        holder.notif_history_item_Layout.setOnLongClickListener(longClick);
        holder.notif_history_item_Layout.setOnClickListener(click);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return notification_history_ListOfNotificationDB.size();
    }

    class NotificationHistoryViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout notif_history_item_Layout;
        TextView notif_history_item_SenderName;
        TextView notif_history_item_NotificationContent;
        TextView notif_history_item_NotificationDate;
        AppCompatImageView notif_history_item_AppImage;

        NotificationHistoryViewHolder(@NonNull View view) {
            super(view);
            notif_history_item_Layout = view.findViewById(R.id.notif_history_item_layout);
            notif_history_item_SenderName = view.findViewById(R.id.notif_history_item_sender_name);
            notif_history_item_NotificationContent = view.findViewById(R.id.notif_history_item_notification_content);
            notif_history_item_NotificationDate = view.findViewById(R.id.notif_history_item_notification_time);
            notif_history_item_AppImage = view.findViewById(R.id.notif_history_item_app_image);
        }
    }
}