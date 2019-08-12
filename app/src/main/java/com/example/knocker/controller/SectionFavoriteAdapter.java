package com.example.knocker.controller;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;

import org.jetbrains.annotations.NotNull;

public class SectionFavoriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private final Context mContext;
    private static final int SECTION_TYPE = 0;
    private boolean mValid = true;
    private RecyclerView.Adapter mRecyclerBaseAdapter;
    private ContactGridViewAdapter mGridBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<>();


    public SectionFavoriteAdapter(Context context, RecyclerView recyclerView,
                                  RecyclerView.Adapter baseAdapter) {
        mRecyclerBaseAdapter = baseAdapter;
        mContext = context;
        mRecyclerBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mRecyclerBaseAdapter.getItemCount() > 0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mRecyclerBaseAdapter.getItemCount() > 0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mRecyclerBaseAdapter.getItemCount() > 0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mRecyclerBaseAdapter.getItemCount() > 0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });

        final GridLayoutManager layoutManager = (GridLayoutManager) (recyclerView.getLayoutManager());
        assert layoutManager != null;
    }

    public SectionFavoriteAdapter(Context context, GridView gridView,
                                  ContactGridViewAdapter baseAdapter) {
        mGridBaseAdapter = baseAdapter;
        mContext = context;
        mGridBaseAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });

//        final GridLayoutManager layoutManager = (GridLayoutManager) (gridView.getLa());
//        assert layoutManager != null;
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv;

        SectionViewHolder(View view) {
            super(view);
            titleTv = view.findViewById(R.id.section_favorite_text);
        }
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.favorite_recycler_adapter_section, parent, false);
            view.setBackgroundResource(R.drawable.recycler_section);
            return new SectionViewHolder(view);
        } else {
            return mRecyclerBaseAdapter.onCreateViewHolder(parent, typeView - 1);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder sectionViewHolder, final int position) {
        ((SectionViewHolder) sectionViewHolder).titleTv.setText("Favoris");
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }


    public static class Section {

        public Section() {
        }
    }

    @Override
    public int getItemCount() {
        if (mRecyclerBaseAdapter != null) {
            return (mValid ? mRecyclerBaseAdapter.getItemCount() + mSections.size() : 0);
        } else {
            return (mValid ? mGridBaseAdapter.getCount() + mSections.size() : 0);
        }
    }
}

