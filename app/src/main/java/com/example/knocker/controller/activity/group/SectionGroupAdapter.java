package com.example.knocker.controller.activity.group;


import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.Arrays;
import java.util.Comparator;

public class SectionGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private static final int SECTION_TYPE = 0;

    private boolean mValid = true;
    private int mSectionResourceId;
    private LayoutInflater mLayoutInflater;
    private RecyclerView.Adapter mBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<Section>();
    private RecyclerView mRecyclerView;


    public SectionGroupAdapter(Context context, int sectionResourceId, RecyclerView recyclerView,
                               RecyclerView.Adapter baseAdapter) {

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSectionResourceId = sectionResourceId;
        mBaseAdapter = baseAdapter;
        mContext = context;
        mRecyclerView = recyclerView;


        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });

        final GridLayoutManager layoutManager = (GridLayoutManager)(mRecyclerView.getLayoutManager());
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (isSectionHeaderPosition(position))? layoutManager.getSpanCount() : 1 ;
            }
        });
    }


    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTv;
        public ImageView gmailIV;
        public ImageView smsIV;

        public SectionViewHolder(View view) {
            super(view);
            titleTv = (TextView) view.findViewById(R.id.section_text);
            gmailIV = (ImageView) view.findViewById(R.id.section_gmail_imageview);
            smsIV=(ImageView) view.findViewById(R.id.section_sms_imageview);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
            view.setBackgroundResource(R.drawable.recycler_section);
            return new SectionViewHolder(view);
        }else{
            return mBaseAdapter.onCreateViewHolder(parent, typeView -1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {

        if (isSectionHeaderPosition(position)) {
            int i=position+1;
            System.out.println("position section"+position);
            ((SectionViewHolder)sectionViewHolder).titleTv.setText(mSections.get(position).title);
            while (!isSectionHeaderPosition(i) && i<getItemCount()){
                ContactWithAllInformation contact =((GroupAdapter)mBaseAdapter).getItem(sectionedPositionToPosition(i));
                System.out.println("contact "+contact.getContactDB()+ " de la section "+position);
                i++;
                if(contact.getFirstMail().isEmpty()){
                    System.out.println("contact "+contact.getContactDB()+ "n'as pas de mail "+contact.getFirstMail());
                    ((SectionViewHolder)sectionViewHolder).gmailIV.setVisibility(View.GONE);
                }
                if(contact.getPhoneNumber().isEmpty()){
                    System.out.println("contact "+contact.getContactDB()+ "n'as pas de num "+contact.getPhoneNumber());
                    ((SectionViewHolder)sectionViewHolder).smsIV.setVisibility(View.GONE);
                }
            }

        }else{
           // System.out.println("position non section"+position);
            mBaseAdapter.onBindViewHolder(sectionViewHolder,sectionedPositionToPosition(position));
            //System.out.println("contact "+((GroupAdapter)mBaseAdapter).getItem(sectionedPositionToPosition(position)).getContactDB()+ " position "+position);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) +1 ;
    }


    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public CharSequence getTitle() {
            return title;
        }
    }


    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
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

}
