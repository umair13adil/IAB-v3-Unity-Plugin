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
public class BillingMessageActivity extends UnityPlayerActivity implements BillingProcessor.IBillingHandler {

    static String TAG = BillingMessageActivity.class.getSimpleName();
    Context context;
    Handler mHandler;

    private static String BILLING_HELPER = "BillingHelper";

    //In App Billing v3
    static BillingProcessor bp;
    TransactionDetails td;
    static String SKU_PURCHASE = "";
    static String RESTORE_ID = "";
    static String ERROR_CODE = "";
    String IAB_PUBLIC_KEY = "";
    private static boolean readyToPurchase = false;
    private static boolean isRestored = false;
    private boolean IS_DEBUGGABLE = false;

    //For Unity
    private static BillingMessageActivity instance;

    public BillingMessageActivity() {
        this.instance = BillingMessageActivity.this;
    }

    public static BillingMessageActivity instance() {
        if (instance == null) {
            instance = new BillingMessageActivity();
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
        bp = new BillingProcessor(BillingMessageActivity.instance, IAB_PUBLIC_KEY, this);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.i(TAG, "Purchase Successful! " + productId);
        billingSuccess(productId,BILLING_HELPER,"");
    }

    @Override
    public void onPurchaseHistoryRestored() {
        for (String sku : bp.listOwnedSubscriptions()) {
            if (bp.isPurchased(sku)) {
                Log.i(TAG, "Already Purchased: " + sku);
                this.RESTORE_ID = sku;
                isRestored = true;
                billingRestored(BILLING_HELPER,"");
            }
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.i(TAG, "InAppBilling Error! Code: " + errorCode);
        if (errorCode == 102) {
            billingSuccess("true",BILLING_HELPER,"");
        } else {
            this.ERROR_CODE = errorCode+"";
            billingError(BILLING_HELPER,"");
        }
    }

    @Override
    public void onBillingInitialized() {
        Log.i(TAG, "InAppBilling Initialized!");
        readyToPurchase = true;
        billingInitialized(BILLING_HELPER,"");
        try {
            boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
            if (!isAvailable) {
                readyToPurchase = false;
                Log.e(TAG, "InAppBilling Not Available!");
                billingInitialized(BILLING_HELPER,"");
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
                bp.purchase(BillingMessageActivity.this, SKU_PURCHASE);
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

    public static boolean isPurchased(String sku) {
        boolean isPurchased = false;
        if (bp.isPurchased(sku)) {
            isPurchased = true;
            Log.i(TAG, "Purchased: " + sku);
            isPurchased(sku,BILLING_HELPER,"");
        } else {
            isPurchased = false;
            isPurchased("false",BILLING_HELPER,"");
        }
        return isPurchased;
    }

    //Public Methods for UNITY
    static public void billingInitialized(String gameObject, String methodName) {
        if (readyToPurchase)
            UnityPlayer.UnitySendMessage(gameObject, methodName, "true");
        else
            UnityPlayer.UnitySendMessage(gameObject, methodName, "false");

    }

    static public void billingError(String gameObject, String methodName) {
        UnityPlayer.UnitySendMessage(gameObject, methodName, ERROR_CODE);
    }

    static public void billingSuccess(String productID,String gameObject, String methodName) {
        SKU_PURCHASE = productID;
        UnityPlayer.UnitySendMessage(gameObject, methodName, productID);
    }

    static public void billingRestored(String gameObject, String methodName) {
        if(isRestored)
            UnityPlayer.UnitySendMessage(gameObject, methodName, RESTORE_ID);
    }

    static public void isPurchased(String productID,String gameObject, String methodName) {
        SKU_PURCHASE = productID;
        Boolean isPurchased = isPurchased(SKU_PURCHASE);
        if(isPurchased)
            UnityPlayer.UnitySendMessage(gameObject, methodName, "true");
        else
            UnityPlayer.UnitySendMessage(gameObject, methodName, "false");
    }
}
