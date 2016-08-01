using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class SimpleExample : MonoBehaviour
{


	AndroidJavaObject currentActivity;

	string PUBLIC_KEY = "YOUR_PUBLIC_KEY";
	string SKU = "android.test.purchased";

	bool check = false;
	bool isInitialized = false;
	bool isPurchased = false;

	public Text txt_log;

	// Use this for initialization
	void Start ()
	{


		AndroidJavaClass unity = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		currentActivity = unity.GetStatic<AndroidJavaObject> ("currentActivity");
		currentActivity.Call ("setPublicKey", PUBLIC_KEY);
		currentActivity.Call<bool> ("isInitialized");

		//To check if Item is already purchased
		bool purchased = currentActivity.Call<bool> ("isPurchased",SKU);

	}




	void Update ()
	{

		if (check) {
			isPurchased = currentActivity.Call<bool> ("isPurchased");
			if (isPurchased) {
				check = false;
				Debug.Log ("Is Purchased!");
				txt_log.text = "Is Purchased!";
			}
		}

	}

	public void purchase()
	{
		Debug.Log ("Purchase Clicked!");
		isInitialized = currentActivity.Call<bool> ("isInitialized");
		if (isInitialized) {
			check = true;
			currentActivity.Call ("purchase", SKU);
		} else {
			txt_log.text = "IAB Not Initialized!";
		}
	}

	public void consume()
	{
		Debug.Log ("consume Clicked!");
		isInitialized = currentActivity.Call<bool> ("isInitialized");
		if (isInitialized) {
			currentActivity.Call ("consume", SKU);
		}else {
			txt_log.text = "IAB Not Initialized!";
		}
	}
}
