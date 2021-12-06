package com.yellowtwigs.knockin.controller.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;
import com.google.android.material.button.MaterialButton;
import com.yellowtwigs.knockin.R;
import com.yellowtwigs.knockin.controller.IProductClickListener;
import com.yellowtwigs.knockin.controller.activity.PremiumActivity;

import java.util.List;


public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.MyViewHolder> {

    private PremiumActivity premiumActivity;
    private List<SkuDetails> skuDetailsList;
    private BillingClient billingClient;

    public MyProductAdapter(PremiumActivity premiumActivity, List<SkuDetails> skuDetailsList, BillingClient billingClient) {
        this.premiumActivity = premiumActivity;
        this.skuDetailsList = skuDetailsList;
        this.billingClient = billingClient;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(premiumActivity.getBaseContext())
                .inflate(R.layout.layout_product_item, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {

        SharedPreferences sharedNotifJazzySoundInAppPreferences = premiumActivity.getSharedPreferences("Notif_Jazzy_Sound_IsBought", Context.MODE_PRIVATE);
        boolean notifJazzySoundIsBought = sharedNotifJazzySoundInAppPreferences.getBoolean("Notif_Jazzy_Sound_IsBought", false);

        SharedPreferences sharedNotifFunkySoundInAppPreferences = premiumActivity.getSharedPreferences("Notif_Funky_Sound_IsBought", Context.MODE_PRIVATE);
        boolean notifFunkySoundIsBought = sharedNotifFunkySoundInAppPreferences.getBoolean("Notif_Funky_Sound_IsBought", false);

        SharedPreferences sharedNotifRelaxationSoundInAppPreferences = premiumActivity.getSharedPreferences("Notif_Relaxation_Sound_IsBought", Context.MODE_PRIVATE);
        boolean notifRelaxationSoundIsBought = sharedNotifRelaxationSoundInAppPreferences.getBoolean("Notif_Relaxation_Sound_IsBought", false);

        SharedPreferences sharedAlarmNotifInAppPreferences = premiumActivity.getSharedPreferences("Alarm_Contacts_Unlimited_IsBought", Context.MODE_PRIVATE);
        boolean contactsUnlimitedIsBought = sharedAlarmNotifInAppPreferences.getBoolean("Alarm_Contacts_Unlimited_IsBought", false);

        SharedPreferences sharedNotifCustomSoundInAppPreferences = premiumActivity.getSharedPreferences("Notif_Custom_Sound_IsBought", Context.MODE_PRIVATE);
        boolean notifCustomSoundIsBought = sharedNotifCustomSoundInAppPreferences.getBoolean("Notif_Custom_Sound_IsBought", false);


        String productName = skuDetailsList.get(i).getTitle(); // Contact VIP Illimités (Knock In Notifications)
        String[] text = productName.split("\\(");

        myViewHolder.myProductName.setText(text[0]);
//        myViewHolder.myProductPrice.setText(skuDetailsList.get(i).getPrice());

        if (skuDetailsList.get(i).getTitle().contains("Contacts") || skuDetailsList.get(i).getTitle().contains("Contatti") || skuDetailsList.get(i).getTitle().contains("Contactos ")) {
            myViewHolder.myProductImage.setImageResource(R.drawable.ic_circular_vip_icon);
        } else if (skuDetailsList.get(i).getTitle().contains("Jazzy") || skuDetailsList.get(i).getTitle().contains("jazzy")) {
            myViewHolder.myProductImage.setImageResource(R.drawable.ic_circular_jazz_trumpet);
        } else if (skuDetailsList.get(i).getTitle().contains("Funky") || skuDetailsList.get(i).getTitle().contains("funky")) {
            myViewHolder.myProductImage.setImageResource(R.drawable.ic_circular_music_icon);
        } else if (skuDetailsList.get(i).getTitle().contains("Relaxation") || skuDetailsList.get(i).getTitle().contains("Relajación") || skuDetailsList.get(i).getTitle().contains("Relajación")) {
            myViewHolder.myProductImage.setImageResource(R.drawable.ic_circular_relax);
        } else if (skuDetailsList.get(i).getTitle().contains("Custom") || skuDetailsList.get(i).getTitle().contains("custom")) {
            myViewHolder.myProductImage.setImageResource(R.drawable.ic_circular_music_icon);
        }

        myViewHolder.myProductBuyImage.setIcon(premiumActivity.getDrawable(R.drawable.ic_buying_in_app));

        if (skuDetailsList.get(i).getTitle().contains("Contacts") && contactsUnlimitedIsBought) {
            myViewHolder.myProductBuyImage.setBackgroundColor(premiumActivity.getColor(R.color.textColorDarkGrey));
            myViewHolder.myProductBuyImage.setEnabled(false);
        } else if (skuDetailsList.get(i).getTitle().contains("Jazzy") && notifJazzySoundIsBought) {
            myViewHolder.myProductBuyImage.setBackgroundColor(premiumActivity.getColor(R.color.textColorDarkGrey));
            myViewHolder.myProductBuyImage.setEnabled(false);
        } else if (skuDetailsList.get(i).getTitle().contains("Funky") && notifFunkySoundIsBought) {
            myViewHolder.myProductBuyImage.setBackgroundColor(premiumActivity.getColor(R.color.textColorDarkGrey));
            myViewHolder.myProductBuyImage.setEnabled(false);
        } else if (skuDetailsList.get(i).getTitle().contains("Relaxation") && notifRelaxationSoundIsBought) {
            myViewHolder.myProductBuyImage.setBackgroundColor(premiumActivity.getColor(R.color.textColorDarkGrey));
            myViewHolder.myProductBuyImage.setEnabled(false);
        } else if (skuDetailsList.get(i).getTitle().contains("Custom") && notifCustomSoundIsBought) {
            myViewHolder.myProductBuyImage.setBackgroundColor(premiumActivity.getColor(R.color.textColorDarkGrey));
            myViewHolder.myProductBuyImage.setEnabled(false);
        }

        myViewHolder.myProductLayout.setOnClickListener(v -> Toast.makeText(premiumActivity, skuDetailsList.get(i).getDescription() + " " +
                skuDetailsList.get(i).getPrice(), Toast.LENGTH_LONG).show());

        //Product click
        myViewHolder.setIProductClickListener((view, position) -> {
            //Launch Billing flow
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetailsList.get(i))
                    .build();
            billingClient.launchBillingFlow(premiumActivity, billingFlowParams);
        });
    }

    @Override
    public int getItemCount() {
        return skuDetailsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RelativeLayout myProductLayout;
        AppCompatImageView myProductImage;
        TextView myProductName;
        //        AppCompatImageView myProductBuyImage;
        MaterialButton myProductBuyImage;

        IProductClickListener iProductClickListener;

        void setIProductClickListener(IProductClickListener iProductClickListener) {
            this.iProductClickListener = iProductClickListener;
        }

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myProductLayout = itemView.findViewById(R.id.product_item_layout);
            myProductImage = itemView.findViewById(R.id.product_item_image);
            myProductName = itemView.findViewById(R.id.product_item_name);
            myProductBuyImage = itemView.findViewById(R.id.product_item_buy_image);
            myProductBuyImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iProductClickListener.onProductClickListener(view, getAdapterPosition());
        }
    }
}