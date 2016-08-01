package com.example.umair_adil.inappbilling;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

/**
 * Created by Umair_Adil on 29/07/2016.
 */
public class BillingActivity extends UnityPlayerActivity implements BillingProcessor.IBillingHandler {

    String TAG = BillingActivity.class.getSimpleName();
    Context context;

    //In App Billing v3
    BillingProcessor bp;
    TransactionDetails td;
    String SKU_PURCHASE = "";
    String IAB_PUBLIC_KEY = "";
    private boolean readyToPurchase = false;
    private boolean isPurchased = false;
    private boolean IS_DEBUGGABLE = false;

    //For Unity
    private static BillingActivity instance;

    public BillingActivity() {
        this.instance = BillingActivity.this;
    }

    public static BillingActivity instance() {
        if (instance == null) {
            instance = new BillingActivity();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_DEBUGGABLE = true;
        bp = new BillingProcessor(BillingActivity.instance, IAB_PUBLIC_KEY, this);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.i(TAG, "Purchase Successful! " + productId);
        isPurchased = true;
    }

    @Override
    public void onPurchaseHistoryRestored() {
        for (String sku : bp.listOwnedSubscriptions()) {
            if (bp.isPurchased(sku)) {
                Log.i(TAG, "Already Purchased: " + sku);
                isPurchased = true;
            } else {
                isPurchased = false;
            }
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.i(TAG, "InAppBilling Error! Code: " + errorCode);
        if (errorCode == 102) {
            isPurchased = true;
        }
    }

    @Override
    public void onBillingInitialized() {
        Log.i(TAG, "InAppBilling Initialized!");
        readyToPurchase = true;
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
        Log.i(TAG, "Purchase Called! SKU: " + sku);
        if (!readyToPurchase) {
            Log.i(TAG, "InAppBilling Inactive!");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bp.purchase(BillingActivity.this, SKU_PURCHASE);
            }
        });

    }

    public void consume(String sku) {
        Log.i(TAG, "Consume Called! SKU: " + sku);
        if (!readyToPurchase) {
            Log.i(TAG, "InAppBilling Inactive!");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bp.consumePurchase(SKU_PURCHASE);
            }
        });

    }

    public boolean isInitialized() {
        return readyToPurchase;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public boolean isPurchased(String sku) {
        if (bp.isPurchased(sku)) {
            Log.i(TAG, "Purchased: " + sku);
            isPurchased = true;
        } else {
            isPurchased = false;
        }
        return isPurchased;
    }
}
