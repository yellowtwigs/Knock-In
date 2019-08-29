package com.yellowtwigs.knocker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;
import com.yellowtwigs.knocker.Interface.IProductClickListener;
import com.yellowtwigs.knocker.R;
import com.yellowtwigs.knocker.controller.activity.PremiumActivity;

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
                .inflate(R.layout.layout_product_item,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        myViewHolder.txt_product.setText(skuDetailsList.get(i).getTitle());

        //Product click
        myViewHolder.setiProductClickListener(new IProductClickListener() {
            @Override
            public void onProductClickListener(View view, int position) {
                //Launch Billing flow
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList.get(i))
                        .build();
                billingClient.launchBillingFlow(premiumActivity,billingFlowParams);
            }
        });
    }

    @Override
    public int getItemCount() {
        return skuDetailsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_product;

        IProductClickListener iProductClickListener;

        public void setiProductClickListener(IProductClickListener iProductClickListener) {
            this.iProductClickListener = iProductClickListener;
        }

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            txt_product = (TextView)itemView.findViewById(R.id.txt_product_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
             iProductClickListener.onProductClickListener(view,getAdapterPosition());
        }
    }
}