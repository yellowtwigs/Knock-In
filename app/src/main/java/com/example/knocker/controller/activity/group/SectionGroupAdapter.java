package com.example.knocker.controller.activity.group;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;
import com.example.knocker.model.ContactsRoomDatabase;
import com.example.knocker.model.DbWorkerThread;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SectionGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private final Context mContext;
    private static final int SECTION_TYPE = 0;

    private boolean mValid = true;
    private int mSectionResourceId;
    private RecyclerView.Adapter mBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<>();


    public SectionGroupAdapter(Context context, int sectionResourceId, RecyclerView recyclerView,
                               RecyclerView.Adapter baseAdapter) {

        mSectionResourceId = sectionResourceId;
        mBaseAdapter = baseAdapter;
        mContext = context;


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
            roundedLayout.setColorFilter(contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).randomColorGroup(mContext), PorterDuff.Mode.MULTIPLY);
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
            ((SectionViewHolder) sectionViewHolder).smsIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = position + 1;
                    ArrayList<String> groupSms = new ArrayList<>();
                    while (!isSectionHeaderPosition(i) && i < getItemCount()) {
                        ContactWithAllInformation contact = ((GroupAdapter) mBaseAdapter).getItem(sectionedPositionToPosition(i));
                        groupSms.add(contact.getFirstPhoneNumber());
                        i++;
                    }
                    monoChannelSmsClick(groupSms);
                }
            });

            ((SectionViewHolder) sectionViewHolder).menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("BUTTON CLICK");
                    PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.inflate(R.menu.menu_manage_group);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            System.out.println("VALUES = " + item.getItemId());
                            System.out.println("ok = " + R.id.menu_group_add_contacts);
                            System.out.println("ok = " + R.id.menu_group_delete_contacts);
                            System.out.println("ok = " + R.id.menu_group_delete_group);
                            switch (item.getItemId()) {
                                case R.id.menu_group_add_contacts:
                                    System.out.println("add contact");
                                    Intent intent = new Intent(mContext, AddContactToGroup.class);
                                    intent.putExtra("GroupId", mSections.get(position).getIdGroup().intValue());
                                    mContext.startActivity(intent);
                                    break;
                                case R.id.menu_group_delete_contacts:
                                    Intent intentdelete = new Intent(mContext, DeleteContactToGroup.class);
                                    intentdelete.putExtra("GroupId", mSections.get(position).getIdGroup().intValue());
                                    mContext.startActivity(intentdelete);
                                    System.out.println("delete contact");
                                    break;
                                case R.id.menu_group_delete_group:
                                    //groupManagerActivity = GroupManagerActivity;
                                    ContactsRoomDatabase contactsDatabase;
                                    DbWorkerThread mDbWorkerThread;
                                    mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
                                    mDbWorkerThread.start();
                                    contactsDatabase = ContactsRoomDatabase.Companion.getDatabase(mContext);
                                    assert contactsDatabase != null;
                                    System.out.println("id group" + mSections.get(position).getIdGroup().intValue() + " voici le groupe concerné" + contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()));
                                    contactsDatabase.GroupsDao().deleteGroupById(mSections.get(position).getIdGroup().intValue());
                                    if (mContext instanceof GroupManagerActivity)
                                        ((GroupManagerActivity) mContext).refreshList();
                                    break;
                                default:
                                    System.out.println("always in default");
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        } else {
            // System.out.println("position non section"+position);
            mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
            //System.out.println("contact "+((GroupAdapter)mBaseAdapter).getItem(sectionedPositionToPosition(position)).getContactDB()+ " position "+position);
        }

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

        public Long getIdGroup() {
            return idGroup;
        }
    }


    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return Integer.compare(o.firstPosition, o1.firstPosition);
            }
        });

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

    public int sectionedPositionToPosition(int sectionedPosition) {
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

    public boolean isSectionHeaderPosition(int position) {
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
}
