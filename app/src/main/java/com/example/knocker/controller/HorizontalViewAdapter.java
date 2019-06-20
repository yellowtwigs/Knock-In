package com.example.knocker.controller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.knocker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HorizontalViewAdapter extends RecyclerView.Adapter<HorizontalViewAdapter.ViewHolder> {

    private static final String TAG = "HorizontalViewAdapter";

    //vars
    private ArrayList<Integer> list;
    private ArrayList<Boolean> alreadyGone;
    private Context context;
    private List<String> packageNameList;

    public void setPackageNameList(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent,0);
        this.packageNameList = new ArrayList<>();
        for(ResolveInfo resolveInfo: resolveInfoList){
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            this.packageNameList.add(activityInfo.applicationInfo.packageName);
        }
        alreadyGone = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false, false, false, false, false, false));
    }

    public HorizontalViewAdapter(Context context, ArrayList<Integer> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called."+position);

        Glide.with(context)
                .asBitmap()
                .load(list.get(position))
                .into(holder.image);

        //holder.image.setImageResource(list.get(position));
        holder.image.setId(position);
        Log.d(TAG, "onBindViewHolder: called. "+list);
        Log.d(TAG, "onBindViewHolder: called. "+list.get(position));
        System.out.print(packageNameList);
        //System.out.print(holder.image.getDrawable());
        System.out.print(list);

        if (position == 0 && !packageNameList.contains("com.facebook.katana")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "FACEBOOK");
            alreadyGone.set(0,true);
        }
        if (position == 1 && !packageNameList.contains("com.facebook.orca")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "ORCA");
            alreadyGone.set(1,true);
        }
        if (position == 2 && !packageNameList.contains("com.instagram.android") && !alreadyGone.get(2)) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "Insta");
            //newlist.remove(2);
            alreadyGone.set(2,true);
        }
        if (position == 3 && !packageNameList.contains("com.whatsapp")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "WHATSAPP");
        }
        if (position == 4 && !packageNameList.contains("com.google.android.gm")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "ANDROID");
        }
        if (position == 5 && !packageNameList.contains("com.microsoft.office.outlook")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "OUTLOOK");
        }
        if (position == 6 && !packageNameList.contains("com.spotify.music")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "MUSIC");
        }
        if (position == 7 && !packageNameList.contains("com.linkedin.android")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "LINKDIN");
        }
        if (position == 8 && !packageNameList.contains("org.telegram.messenger")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "TELEGRAM");
        }
        if (position == 9 && !packageNameList.contains("com.google.android.youtube")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "YOUTUBE");
        }
        if (position == 10 && !packageNameList.contains("com.twitter.android")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "TWITTER");
        }
        if (position == 11 && !packageNameList.contains("com.skype.raider")) {
            holder.image.setVisibility(View.GONE);
            Log.d(TAG, "FACEBOOK");
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (position) {
                    case 0:
                        goToFacebook();
                        break;
                    case 8:
                        goToTelegram();
                        break;
                    case 10:
                        goToTwitter();
                        break;
                    case 1:
                        break;
                    case 6:
                        goToSpotify();
                        break;
                    case 2:
                        goToInstagramPage();
                        break;
                    case 4:
                        goToGmail();
                        break;
                    case 5:
                        goToOutlook();
                        break;
                    case 3:
                        goToWhatsapp();
                        break;
                    case 7:
                        goToLinkedin();
                        break;
                    case 9:
                        goToYoutube();
                        break;
                    case 11:
                        goToSkype();
                        break;
                    default:
                }
            }
        });
    }

    private void goToWhatsapp() {
        Intent i = context.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        try {
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://whatsapp.com/")));
        }
    }

    private void goToInstagramPage() {
        Uri uri = Uri.parse("http://instagram.com/_u/therock/");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            context.startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/")));
        }
    }

    private void goToFacebook() {
        Uri uri = Uri.parse("facebook:/newsfeed");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://facebook.com/")));
        }
    }

    private void goToTelegram() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve"));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://web.telegram.org/")));
        }
    }

    private void goToGmail() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW);
        appIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://gmail.com/")));
        }
    }

    private void goToLinkedin() {
        /// don't work
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://you"));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://linkedin.com/")));
        }
    }

    private void goToOutlook() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("ms-outlook://emails"));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://outlook.com/")));
        }
    }

    private void goToSkype() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://skype.com/")));
        }
    }

    private void goToSpotify() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("spotify://spotify"));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://spotify.com/")));
        }
    }


    private void goToTwitter() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW);
        appIntent.setClassName("com.twitter.android", "com.twitter.android");
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/")));
        }
    }

    private void goToYoutube() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube"));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://youtube.com/")));
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        ConstraintLayout layout;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.horizontal_item_image);
            layout = itemView.findViewById(R.id.horizontal_item_layout);
        }
    }
}
