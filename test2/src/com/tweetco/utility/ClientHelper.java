package com.tweetco.utility;

import java.net.MalformedURLException;

import android.app.Activity;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tweetco.TweetCo;

public class ClientHelper 
{
	public static MobileServiceClient getMobileClient(Activity activity) throws MalformedURLException
	{
		return new MobileServiceClient(TweetCo.APP_URL, TweetCo.APP_KEY, activity);
		
	}

}
