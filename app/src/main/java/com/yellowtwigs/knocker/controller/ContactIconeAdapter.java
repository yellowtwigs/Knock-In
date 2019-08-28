package com.yellowtwigs.knocker.controller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.yellowtwigs.knocker.R;
import com.yellowtwigs.knocker.controller.activity.AddNewContactActivity;
import com.yellowtwigs.knocker.controller.activity.EditContactActivity;

public class ContactIconeAdapter extends RecyclerView.Adapter<ContactIconeAdapter.ViewHolder> {
    private Context context;
    private int[] iconeList;

    public ContactIconeAdapter(Context context) {
        this.context = context;
        TypedArray array = context.getResources().obtainTypedArray(R.array.icone_ressource);
        iconeList = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            iconeList[i] = array.getResourceId(i, 0);
            ;
        }
    }

    @NonNull
    @Override
    public ContactIconeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        System.out.println("test into contact adapter");
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.icone_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactIconeAdapter.ViewHolder holder, final int position) {

        for (int i1 : iconeList) {
            System.out.println("icone list" + i1);
        }
        holder.imageViewIcone.setImageResource(iconeList[position]);
        holder.iconeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof EditContactActivity) {
                    Drawable drawable = context.getDrawable(iconeList[position]);
                    Bitmap bitmap = null;
                    if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                        bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
                    } else {
                        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    }

                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                    ((EditContactActivity) context).addContactIcone(bitmap);
                } else if (context instanceof AddNewContactActivity) {
                    Drawable drawable = context.getDrawable(iconeList[position]);
                    Bitmap bitmap = null;
                    if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                        bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
                    } else {
                        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    }

                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                    ((AddNewContactActivity) context).addContactIcone(bitmap);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        System.out.println("icone list length" + iconeList.length);
        return iconeList.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout iconeLayout;
        AppCompatImageView imageViewIcone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconeLayout = itemView.findViewById(R.id.icone_adapter_relativelayout);
            imageViewIcone = itemView.findViewById(R.id.icone_adapter_imageView);
        }
    }
}
