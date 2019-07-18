package com.example.knocker.controller;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.example.knocker.model.ModelDB.GroupDB;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class GroupEditAdapter extends RecyclerView.Adapter<GroupEditAdapter.ViewHolder> {
    ArrayList<GroupDB> listGroup;
    Context context;
    ContactWithAllInformation contact;
    public GroupEditAdapter(Context context, ArrayList<GroupDB> listGroup, ContactWithAllInformation contact){
        this.context=context;
        this.listGroup=listGroup;
        this.contact=contact;
    }
    @NonNull
    @Override
    public GroupEditAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(context);
        View view= inflater.inflate(R.layout.group_item_edit,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupEditAdapter.ViewHolder holder, final int position) {
        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("click close");
                listGroup.remove(position);
               // holder.layoutGroup.setVisibility(View.GONE);
                notifyDataSetChanged();
            }
        });
        Drawable drawable= context.getDrawable(R.drawable.rounded_rectangle_group);
        drawable.setColorFilter(listGroup.get(position).randomColorGroup(context), PorterDuff.Mode.MULTIPLY);
        holder.layoutGroup.setBackground(drawable);
        holder.groupName.setText(listGroup.get(position).getName());
    }
    public GroupDB getItem(int position){
        return listGroup.get(position);
    }
    @Override
    public int getItemCount() {
        return listGroup.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        ImageView close;
        ConstraintLayout layoutGroup;

        ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.adpater_edit_group_name);
            close = itemView.findViewById(R.id.adpater_edit_close);
            layoutGroup = itemView.findViewById(R.id.adpater_edit_constraint_layout);
        }

/*        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }*/
    }

}
