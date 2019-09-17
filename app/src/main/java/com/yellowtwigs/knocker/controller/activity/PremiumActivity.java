package com.yellowtwigs.knocker.controller.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.yellowtwigs.knocker.Adapter.MyProductAdapter;
import com.yellowtwigs.knocker.R;

import java.util.Arrays;
import java.util.List;

public class PremiumActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    BillingClient billingClient;

    Button loadProduct;
    RecyclerView recyclerProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        setupBillingClient();

        //View
        loadProduct = (Button)findViewById(R.id.btn_load_product);
        recyclerProduct = (RecyclerView)findViewById(R.id.recycler_product);
        recyclerProduct.setHasFixedSize(true);
        recyclerProduct.setLayoutManager(new LinearLayoutManager(this));

        //Event
        loadProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(billingClient.isReady())
                {
                    SkuDetailsParams params = SkuDetailsParams.newBuilder()
                            .setSkusList(Arrays.asList("premium_acces"))
                            .setType(BillingClient.SkuType.INAPP)
                            .build();

                    billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                            if(responseCode == BillingClient.BillingResponse.OK)
                            {
                                loadProductToRecyclerView(skuDetailsList);
                            }
                            else {
                                Toast.makeText(PremiumActivity.this, "Cannot query product", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                    {
                        Toast.makeText(PremiumActivity.this, "Billing client not ready", Toast.LENGTH_SHORT).show();
                }
                }
            });
        }

    private void loadProductToRecyclerView(List<SkuDetails> skuDetailsList) {
        MyProductAdapter adapter = new MyProductAdapter(this,skuDetailsList,billingClient);
        recyclerProduct.setAdapter(adapter);
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
               if(responseCode == BillingClient.BillingResponse.OK)
                   Toast.makeText(PremiumActivity.this, "Success to connect Billing", Toast.LENGTH_SHORT).show();
               else
                   Toast.makeText(PremiumActivity.this, ""+responseCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(PremiumActivity.this, "You are disconnected from Billing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {

        Toast.makeText(this, "Purchases item: "+purchases.size(), Toast.LENGTH_SHORT).show();
    }
}
