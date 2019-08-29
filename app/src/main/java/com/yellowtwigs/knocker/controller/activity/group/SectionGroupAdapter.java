package com.yellowtwigs.knocker.controller.activity.group;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yellowtwigs.knocker.R;
import com.yellowtwigs.knocker.model.ContactsRoomDatabase;
import com.yellowtwigs.knocker.model.DbWorkerThread;
import com.yellowtwigs.knocker.model.ModelDB.ContactWithAllInformation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class SectionGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private final Context mContext;
    private static final int SECTION_TYPE = 0;
    private boolean mValid = true;
    private int mSectionResourceId;
    private RecyclerView.Adapter mBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<>();
    private int color;


    public SectionGroupAdapter(Context context, int sectionResourceId, RecyclerView recyclerView,
                               RecyclerView.Adapter baseAdapter) {

        mSectionResourceId = sectionResourceId;
        mBaseAdapter = baseAdapter;
        mContext = context;

//        color = mContext.getColor(R.color.)

        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });

        final GridLayoutManager layoutManager = (GridLayoutManager) (recyclerView.getLayoutManager());
        assert layoutManager != null;
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (isSectionHeaderPosition(position)) ? layoutManager.getSpanCount() : 1;
            }
        });
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv;
        ImageView gmailIV;
        ImageView smsIV;
        public ImageView menu;
        RelativeLayout holderName;

        SectionViewHolder(View view) {
            super(view);
            titleTv = view.findViewById(R.id.section_text);
            gmailIV = view.findViewById(R.id.section_gmail_imageview);
            smsIV = view.findViewById(R.id.section_sms_imageview);
            menu = view.findViewById(R.id.section_more_imageView);
            holderName = view.findViewById(R.id.recycler_group_name_relative);
        }
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int typeView) {

        if (typeView == SECTION_TYPE) {
            final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
            view.setBackgroundResource(R.drawable.recycler_section);
            return new SectionViewHolder(view);
        } else {
            return mBaseAdapter.onCreateViewHolder(parent, typeView - 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder sectionViewHolder, final int position) {

        if (isSectionHeaderPosition(position)) {

            int i = position + 1;
            System.out.println("position section" + position);

            ((SectionViewHolder) sectionViewHolder).titleTv.setText(mSections.get(position).title);

            while (!isSectionHeaderPosition(i) && i < getItemCount()) {
                ContactWithAllInformation contact = ((GroupAdapter) mBaseAdapter).getItem(sectionedPositionToPosition(i));
                System.out.println("contact " + contact.getContactDB() + " de la section " + position);
                i++;
                if (contact.getFirstMail().isEmpty()) {
                    System.out.println("contact " + contact.getContactDB() + "n'as pas de mail " + contact.getFirstMail());
                    ((SectionViewHolder) sectionViewHolder).gmailIV.setVisibility(View.GONE);
                }
                if (contact.getFirstPhoneNumber().isEmpty()) {
                    System.out.println("contact " + contact.getContactDB() + "n'as pas de num " + contact.getFirstPhoneNumber());
                    ((SectionViewHolder) sectionViewHolder).smsIV.setVisibility(View.GONE);
                }
            }

            Drawable roundedLayout = mContext.getDrawable(R.drawable.rounded_button_color_grey);
            ContactsRoomDatabase contactsDatabase = ContactsRoomDatabase.Companion.getDatabase(mContext);
            //DbWorkerThread main_mDbWorkerThread;

            assert roundedLayout != null;
            assert contactsDatabase != null;

//            roundedLayout.setColorFilter(contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).randomColorGroup(mContext), PorterDuff.Mode.MULTIPLY);
            roundedLayout.setColorFilter(contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getSection_color(), PorterDuff.Mode.MULTIPLY);
            ((SectionViewHolder) sectionViewHolder).holderName.setBackground(roundedLayout);

            ((SectionViewHolder) sectionViewHolder).gmailIV.setOnClickListener(v -> {
                int i1 = position + 1;
                ArrayList<String> groupMail = new ArrayList<>();
                while (!isSectionHeaderPosition(i1) && i1 < getItemCount()) {
                    ContactWithAllInformation contact = ((GroupAdapter) mBaseAdapter).getItem(sectionedPositionToPosition(i1));
                    groupMail.add(contact.getFirstMail());
                    i1++;
                }
                monoChannelMailClick(groupMail);
            });

            ((SectionViewHolder) sectionViewHolder).smsIV.setOnClickListener(v -> {
                int i12 = position + 1;
                ArrayList<String> groupSms = new ArrayList<>();
                while (!isSectionHeaderPosition(i12) && i12 < getItemCount()) {
                    ContactWithAllInformation contact = ((GroupAdapter) mBaseAdapter).getItem(sectionedPositionToPosition(i12));
                    groupSms.add(contact.getFirstPhoneNumber());
                    i12++;
                }
                monoChannelSmsClick(groupSms);
            });

            ((SectionViewHolder) sectionViewHolder).menu.setOnClickListener(v -> {
                System.out.println("BUTTON CLICK");
                final PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.inflate(R.menu.section_menu_group_manager);
                popupMenu.setOnMenuItemClickListener(item -> {
                    System.out.println("VALUES = " + item.getItemId());
                    System.out.println("ok = " + R.id.menu_group_add_contacts);
                    System.out.println("ok = " + R.id.menu_group_delete_contacts);
                    System.out.println("ok = " + R.id.menu_group_delete_group);

                    switch (item.getItemId()) {
                        case R.id.menu_group_edit_group:
                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            @SuppressLint("InflateParams") View alertView = inflater.inflate(R.layout.alert_dialog_edit_group, null, true);

                            AppCompatEditText edit_group_name_EditText = alertView.findViewById(R.id.manager_group_edit_group_view_edit);
                            TextView edit_group_name_AlertDialogTitle = alertView.findViewById(R.id.manager_group_edit_group_alert_dialog_title);

                            AppCompatImageView edit_group_name_RedTag = alertView.findViewById(R.id.manager_group_edit_group_color_red);
                            AppCompatImageView edit_group_name_BlueTag = alertView.findViewById(R.id.manager_group_edit_group_color_blue);
                            AppCompatImageView edit_group_name_GreenTag = alertView.findViewById(R.id.manager_group_edit_group_color_green);
                            AppCompatImageView edit_group_name_YellowTag = alertView.findViewById(R.id.manager_group_edit_group_color_yellow);
                            AppCompatImageView edit_group_name_OrangeTag = alertView.findViewById(R.id.manager_group_edit_group_color_orange);
                            AppCompatImageView edit_group_name_PurpleTag = alertView.findViewById(R.id.manager_group_edit_group_color_purple);


                            edit_group_name_AlertDialogTitle.setText(mContext.getString(R.string.manager_group_edit_group_alert_dialog_title) + " "
                                    + contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getName());

                            edit_group_name_EditText.setHint(contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getName());

                            if (contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getSection_color() == R.color.red_tag_group) {
                                edit_group_name_RedTag.setImageResource(R.drawable.border_selected_image_view);

                            } else if (contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getSection_color() == R.color.blue_tag_group) {
                                edit_group_name_BlueTag.setImageResource(R.drawable.border_selected_image_view);

                            } else if (contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getSection_color() == R.color.green_tag_group) {
                                edit_group_name_GreenTag.setImageResource(R.drawable.border_selected_image_view);

                            } else if (contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getSection_color() == R.color.yellow_tag_group) {
                                edit_group_name_YellowTag.setImageResource(R.drawable.border_selected_image_view);

                            } else if (contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getSection_color() == R.color.orange_tag_group) {
                                edit_group_name_OrangeTag.setImageResource(R.drawable.border_selected_image_view);

                            } else if (contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getSection_color() == R.color.purple_tag_group) {
                                edit_group_name_PurpleTag.setImageResource(R.drawable.border_selected_image_view);
                            }

                            edit_group_name_RedTag.setOnClickListener(v1 -> {
                                edit_group_name_RedTag.setImageResource(R.drawable.border_selected_image_view);
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent);
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent);
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent);
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent);
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent);

                                color = mContext.getColor(R.color.red_tag_group);
                            });

                            edit_group_name_BlueTag.setOnClickListener(v1 -> {
                                edit_group_name_BlueTag.setImageResource(R.drawable.border_selected_image_view);
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent);
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent);
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent);
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent);
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent);

                                color = mContext.getColor(R.color.blue_tag_group);
                            });

                            edit_group_name_GreenTag.setOnClickListener(v1 -> {
                                edit_group_name_GreenTag.setImageResource(R.drawable.border_selected_image_view);
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent);
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent);
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent);
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent);
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent);

                                color = mContext.getColor(R.color.green_tag_group);
                            });

                            edit_group_name_YellowTag.setOnClickListener(v1 -> {
                                edit_group_name_YellowTag.setImageResource(R.drawable.border_selected_image_view);
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent);
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent);
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent);
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent);
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent);

                                color = mContext.getColor(R.color.yellow_tag_group);
                            });

                            edit_group_name_OrangeTag.setOnClickListener(v1 -> {
                                edit_group_name_OrangeTag.setImageResource(R.drawable.border_selected_image_view);
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent);
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent);
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent);
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent);
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent);

                                color = mContext.getColor(R.color.orange_tag_group);
                            });

                            edit_group_name_PurpleTag.setOnClickListener(v1 -> {
                                edit_group_name_PurpleTag.setImageResource(R.drawable.border_selected_image_view);
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent);
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent);
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent);
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent);
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent);

                                color = mContext.getColor(R.color.purple_tag_group);
                            });

                            new MaterialAlertDialogBuilder(mContext, R.style.AlertDialog)
                                    .setView(alertView)
                                    .setPositiveButton(R.string.alert_dialog_validate, (dialog, which) -> {
                                        System.out.println("Name : " + Objects.requireNonNull(edit_group_name_EditText.getText()).toString());
                                        System.out.println("Name : " + contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getName());
                                        System.out.println("Color : " + color);

                                        if (contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getName().equals("Favorites")||
                                                contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getName().equals("Favoris")) {
                                            Toast.makeText(mContext, "Vous ne pouvez pas modifier le nom du groupe Favorites", Toast.LENGTH_LONG).show();//mettre en string
                                        } else {
                                            if (edit_group_name_EditText.getText().toString().equals("")) {
                                                edit_group_name_EditText.setText(contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).getName());
                                            }
                                            contactsDatabase.GroupsDao().updateGroupNameById(mSections.get(position).getIdGroup().intValue(), edit_group_name_EditText.getText().toString());
//                                            Toast.makeText(mContext, "Vous avez modifié le nom de votre groupe", Toast.LENGTH_LONG).show();
                                        }

                                        if (color == 0) {
                                            Random r = new Random();
                                            int n = r.nextInt(7);

                                            switch (n) {
                                                case 0:
                                                    color = mContext.getColor(R.color.red_tag_group);
                                                case 1:
                                                    color = mContext.getColor(R.color.blue_tag_group);
                                                case 2:
                                                    color = mContext.getColor(R.color.green_tag_group);
                                                case 3:
                                                    color = mContext.getColor(R.color.orange_tag_group);
                                                case 4:
                                                    color = mContext.getColor(R.color.yellow_tag_group);
                                                case 5:
                                                    color = mContext.getColor(R.color.purple_tag_group);
                                                case 6:
                                                    color = mContext.getColor(R.color.red_tag_group);
                                                default:
                                                    color = mContext.getColor(R.color.blue_tag_group);
                                            }
                                        }

                                        contactsDatabase.GroupsDao().updateGroupSectionColorById(mSections.get(position).getIdGroup().intValue(), color);
                                        mContext.startActivity(new Intent(mContext, GroupManagerActivity.class));
                                    })
                                    .setNegativeButton(R.string.alert_dialog_cancel, (dialog, which) -> {
                                    })
                                    .show();

                            break;
                        case R.id.menu_group_add_contacts:

                            System.out.println("add contact");
                            Intent intentAddContacts = new Intent(mContext, AddContactToGroupActivity.class);
                            intentAddContacts.putExtra("GroupId", mSections.get(position).getIdGroup().intValue());
                            mContext.startActivity(intentAddContacts);
                            break;

                        case R.id.menu_group_delete_contacts:

                            Intent intentDelete = new Intent(mContext, DeleteContactFromGroupActivity.class);
                            intentDelete.putExtra("GroupId", mSections.get(position).getIdGroup().intValue());
                            mContext.startActivity(intentDelete);
                            System.out.println("delete contact");
                            break;

                        case R.id.menu_group_delete_group:

                            //groupManagerActivity = GroupManagerActivity;
                            ContactsRoomDatabase contactsDatabase1;
                            DbWorkerThread mDbWorkerThread;
                            mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
                            mDbWorkerThread.start();
                            contactsDatabase1 = ContactsRoomDatabase.Companion.getDatabase(mContext);
                            assert contactsDatabase1 != null;
                            alertDialog(mSections.get(position).getIdGroup().intValue(), contactsDatabase1);
                            break;

                        default:
                            System.out.println("always in default");
                            break;
                    }
                    return true;
                });
                popupMenu.show();
            });

            ((SectionViewHolder) sectionViewHolder).holderName.setOnClickListener(v -> {
                ((GroupAdapter)mBaseAdapter).SetGroupClick(getGroupPosition(position));
            });
        } else {
            // System.out.println("position non section"+position);
            mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
            //System.out.println("contact "+((GroupAdapter)mBaseAdapter).getItem(sectionedPositionToPosition(position)).getContactDB()+ " position "+position);
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < getItemCount(); i++) {
            if (isSectionHeaderPosition(i)) {
                list.add(sectionedPositionToPosition(i + 1));
            }
        }
        ((GroupAdapter) mBaseAdapter).setSectionPos(list);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
    }

    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;
        Long idGroup;

        public Section(int firstPosition, CharSequence title, Long idGroup) {
            this.firstPosition = firstPosition;
            this.title = title;
            this.idGroup = idGroup;
        }

        public CharSequence getTitle() {
            return title;
        }

        Long getIdGroup() {
            return idGroup;
        }
    }

    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, (o, o1) -> Integer.compare(o.firstPosition, o1.firstPosition));

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            mSections.append(section.sectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int getPositionSection(int position) {
        for (int i = position; i > 0; i--) {
            if (isSectionHeaderPosition(i)) {
                return i;
            }
        }
        return 0;
    }
    public int getGroupPosition(int position){
        int nbGroup =0;
        for(int i=position; i>0;i--){
            if(isSectionHeaderPosition(i)){
                nbGroup++;
            }
        }
        return nbGroup;
    }

    private int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    private boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
    }

    private void monoChannelSmsClick(ArrayList<String> listOfPhoneNumber) {

        String message = "smsto:" + listOfPhoneNumber.get(0);
        for (int i = 0; i < listOfPhoneNumber.size(); i++) {
            message += ";" + listOfPhoneNumber.get(i);
        }
        mContext.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(message)));
    }

    @SuppressLint("IntentReset")
    private void monoChannelMailClick(ArrayList<String> listOfMail) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, listOfMail.toArray(new String[listOfMail.size()]));
        intent.setData(Uri.parse("mailto:"));
        intent.setType("message/rfc822");

        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        mContext.startActivity(intent);
    }

    private void alertDialog(int idGroup, ContactsRoomDatabase contactsDatabase) {
        new MaterialAlertDialogBuilder(mContext, R.style.AlertDialog)
                .setTitle(R.string.section_alert_delete_group_title)
                .setMessage(String.format(contactsDatabase.GroupsDao().getGroup(idGroup).getName(), R.string.section_alert_delete_group_message))
                .setPositiveButton(android.R.string.yes, (dialog, id) -> {

                            int counter = 0;

                            while (counter < contactsDatabase.GroupsDao().getAllGroupsByNameAZ().size()) {
                                if (Objects.requireNonNull(contactsDatabase.GroupsDao().getAllGroupsByNameAZ().get(counter).getGroupDB()).getName().equals("Favorites")||
                                        Objects.requireNonNull(contactsDatabase.GroupsDao().getAllGroupsByNameAZ().get(counter).getGroupDB()).getName().equals("Favoris")) {
                                    int secondCounter = 0;
                                    while (secondCounter < contactsDatabase.GroupsDao().getAllGroupsByNameAZ().get(counter).getListContact(mContext).size()) {
                                        contactsDatabase.GroupsDao().getAllGroupsByNameAZ().get(counter).getListContact(mContext).get(secondCounter).setIsNotFavorite(contactsDatabase);

                                        secondCounter++;
                                    }
                                    break;
                                }
                                counter++;
                            }

                            contactsDatabase.GroupsDao().deleteGroupById(idGroup);
                            if (mContext instanceof GroupManagerActivity)
                                ((GroupManagerActivity) mContext).refreshList();
                        }
                )
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
