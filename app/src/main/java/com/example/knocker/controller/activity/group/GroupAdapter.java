package com.example.knocker.controller.activity.group;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;
import com.example.knocker.controller.CircularImageView;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;

import java.util.List;
import java.util.Random;


public class GroupAdapter  extends RecyclerView.Adapter<GroupAdapter.SimpleViewHolder> {
    private final Context context;
    private final List<ContactWithAllInformation> contactList;
    private final Integer len;
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView firstName;
        public final TextView lastName;
        public final CircularImageView circularImageView;

        public SimpleViewHolder(View view) {
            super(view);
            firstName = (TextView) view.findViewById(R.id.grid_adapter_contactFirstName);
            lastName = (TextView) view.findViewById(R.id.grid_adapter_contactLastName);
            circularImageView= (CircularImageView) view.findViewById(R.id.contactRoundedImageView);
        }
    }

    public GroupAdapter(Context context,List<ContactWithAllInformation> contactList,Integer len) {
        this.context = context;
        this.contactList=contactList;
        this.len=len;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.grid_contact_item_layout, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        ContactDB contact=contactList.get(position).getContactDB();
        holder.firstName.setText(contact.getFirstName());
        holder.lastName.setText(contactList.get(position).getContactDB().getLastName());

        ConstraintLayout.LayoutParams layoutParamsTV = (ConstraintLayout.LayoutParams) holder.firstName.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParamsIV = (ConstraintLayout.LayoutParams) holder.circularImageView.getLayoutParams();


        int height = holder.circularImageView.getLayoutParams().height;
        int width = holder.circularImageView.getLayoutParams().width;
        if (len == 3) {
            holder.circularImageView.getLayoutParams().height -= height * 0.05;
            holder.circularImageView.getLayoutParams().width -= height * 0.05;
            layoutParamsTV.topMargin = 30;
            layoutParamsIV.topMargin = 10;
        } else if (len == 4) {
            holder.circularImageView.getLayoutParams().height -= height * 0.25;
            holder.circularImageView.getLayoutParams().width -= width * 0.25;
            layoutParamsTV.topMargin = 10;
            layoutParamsIV.topMargin = 10;
        } else if (len == 5) {
            holder.circularImageView.getLayoutParams().height -= height * 0.40;
            holder.circularImageView.getLayoutParams().width -= width * 0.40;
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        } else if (len == 6) {
            holder.circularImageView.getLayoutParams().height -= height * 0.50;
            holder.circularImageView.getLayoutParams().width -= width * 0.50;
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        }
        if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());

            holder.circularImageView.setImageBitmap(bitmap);
        } else {
            holder.circularImageView.setImageResource(randomDefaultImage(contact.getProfilePicture(), "Get")); //////////////
        }
    }



    public void removeItem(int position) {
        contactList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }





    private int randomDefaultImage(int avatarId, String createOrGet) {
        if (createOrGet.equals("Create")) {
            return new Random().nextInt(7);
        } else if (createOrGet.equals("Get")) {
            switch (avatarId) {
                case 0:
                    return R.drawable.ic_user_purple;
                case 1:
                    return R.drawable.ic_user_blue;
                case 2:
                    return R.drawable.ic_user_knocker;
                case 3:
                    return R.drawable.ic_user_green;
                case 4:
                    return R.drawable.ic_user_om;
                case 5:
                    return R.drawable.ic_user_orange;
                case 6:
                    return R.drawable.ic_user_pink;
                default:
                    return R.drawable.ic_user_blue;
            }
        }
        return -1;
    }
    private Bitmap base64ToBitmap(String base64) {

        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }
}