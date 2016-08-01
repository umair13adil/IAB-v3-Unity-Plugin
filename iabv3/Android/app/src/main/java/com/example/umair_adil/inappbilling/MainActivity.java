package com.example.umair_adil.inappbilling;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class MainActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    String TAG = MainActivity.class.getSimpleName();

    Handler mHandler;
    //In App Billing v3
    BillingProcessor bp;
    String SKU_PURCHASE = "";
    String IAB_PUBLIC_KEY = "";

    private boolean readyToPurchase = false;
    private boolean IS_DEBUGGABLE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IS_DEBUGGABLE = true;
        bp = new BillingProcessor(MainActivity.this, IAB_PUBLIC_KEY, this);
        bp.consumePurchase(SKU_PURCHASE);

        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                bp.purchase(MainActivity.this, SKU_PURCHASE);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }).start();

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.i(TAG, "Purchased! " + productId);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        for (String sku : bp.listOwnedSubscriptions()) {
            Log.i(TAG, "Products: " + sku);
            if (bp.isPurchased(sku)) {
                Log.i(TAG, "Already Purchased: " + sku);
            } else {

            }
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.e(TAG, "InAppBilling Error! Code: " + errorCode);
    }

    @Override
    public void onBillingInitialized() {
        Log.i(TAG, "InAppBilling Initialized!");
        readyToPurchase = true;

        bp.purchase(MainActivity.this, SKU_PURCHASE);
        try {
            boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
            if (!isAvailable) {
                Log.e(TAG, "InAppBilling Not Available!");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "InAppBilling Destroyed!");
        if (bp != null)
            bp.release();
        super.onDestroy();
    }


    public void setPublicKey(String publicKey) {
        this.IAB_PUBLIC_KEY = publicKey;
        if (IS_DEBUGGABLE)
            Log.i(TAG, "Public Key: " + publicKey);
    }

    public void setSKU(String sku) {
        this.SKU_PURCHASE = sku;
        if (IS_DEBUGGABLE)
            Log.i(TAG, "Product: " + sku);
    }

    public void setDeuggable(Boolean deuggable) {
        this.IS_DEBUGGABLE = deuggable;
    }

    public void purchase(String sku) {
        if (!readyToPurchase) {
            if (IS_DEBUGGABLE)
                Log.i(TAG, "InAppBilling Inactive!");
            return;
        }
        bp.purchase(this, SKU_PURCHASE);
    }


}
