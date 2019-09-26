package com.yellowtwigs.knockin.controller.adapter;

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
import com.yellowtwigs.knockin.controller.IProductClickListener;
import com.yellowtwigs.knockin.R;
import com.yellowtwigs.knockin.controller.activity.PremiumActivity;

import java.util.List;


public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.MyViewHolder> {

    PremiumActivity premiumActivity;
    List<SkuDetails> skuDetailsList;
    BillingClient billingClient;

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
        myViewHolder.myProductName.setText(skuDetailsList.get(i).getTitle());
        myViewHolder.myProductPrice.setText(skuDetailsList.get(i).getPrice());
        myViewHolder.myProductLayout.setOnLongClickListener(v -> {
            Toast.makeText(premiumActivity, skuDetailsList.get(i).getDescription(), Toast.LENGTH_LONG).show();
            return true;
        });

        if(skuDetailsList.get(i).getTitle().contains("Contacts")){
            myViewHolder.myProductImage.setImageResource(R.drawable.ic_app_image);
        }else if(skuDetailsList.get(i).getTitle().contains("Sons")){
            myViewHolder.myProductImage.setImageResource(R.drawable.ic_icons8_tennis);
        }

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
        TextView myProductPrice;
        AppCompatImageView myProductBuyImage;

        IProductClickListener iProductClickListener;

        void setIProductClickListener(IProductClickListener iProductClickListener) {
            this.iProductClickListener = iProductClickListener;
        }

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myProductLayout = itemView.findViewById(R.id.product_item_layout);
            myProductImage = itemView.findViewById(R.id.product_item_image);
            myProductName = itemView.findViewById(R.id.product_item_name);
            myProductPrice = itemView.findViewById(R.id.product_item_price);
            myProductBuyImage = itemView.findViewById(R.id.product_item_buy_image);
            myProductBuyImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iProductClickListener.onProductClickListener(view, getAdapterPosition());
        }
    }
}