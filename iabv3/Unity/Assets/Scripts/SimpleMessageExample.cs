using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class SimpleMessageExample : MonoBehaviour
{


	AndroidJavaObject currentActivity;

	string PUBLIC_KEY = "YOUR_PUBLIC_KEY";
	string SKU = "android.test.purchased";


	bool is_initialized = false;
	bool check_is_purchased = false;

	public Text txt_log;

	AndroidJavaClass androidClass;

	// Use this for initialization
	void Start ()
	{


		AndroidJavaClass unity = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		currentActivity = unity.GetStatic<AndroidJavaObject> ("currentActivity");
		currentActivity.Call ("setPublicKey", PUBLIC_KEY);

		AndroidJNI.AttachCurrentThread ();
		androidClass = new AndroidJavaClass ("com.example.umair_adil.inappbilling.BillingActivity");

	}


	void Update ()
	{
		androidClass.CallStatic ("billingInitialized", this.gameObject.name, "onBillingInitialized");
		androidClass.CallStatic ("billingError",  this.gameObject.name, "onBillingError");
		androidClass.CallStatic ("billingSuccess", SKU, this.gameObject.name, "onBillingSuccess");
		androidClass.CallStatic ("billingRestored",  this.gameObject.name, "onBillingRestored");
		if (check_is_purchased) {
			androidClass.CallStatic ("isPurchased", SKU, this.gameObject.name, "isPurchased");
		}
	}

	public void purchase ()
	{
		if (is_initialized) {
			currentActivity.Call ("purchase", SKU);
		}
	}

	public void consume ()
	{
		if (is_initialized) {
			currentActivity.Call ("consume", SKU);
			check_is_purchased = true;
		}
	}


	public void onBillingInitialized (string message)
	{
		if (message == "true") {
			is_initialized = true;
			txt_log.text = "IAB is Initialized!";
		}
	}

	public void onBillingError (string message)
	{
		txt_log.text = "Error: " + message;

	}

	public void onBillingSuccess (string message)
	{
		txt_log.text = "Purchased! Product ID: " + message;
		check_is_purchased = true;
	}

	public void onBillingRestored (string message)
	{
		txt_log.text = "Restored! Product ID: " + message;

	}

	public void isPurchased (string message)
	{
		if (message == "true") {
			txt_log.text = "Is Purchased:" + message;
		} else {
			txt_log.text = "Is Purchased:" + message;
		}
		check_is_purchased = false;
	}
}
