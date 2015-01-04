package com.tweetco;

import android.app.Application;
import android.content.Context;

public class TweetCo extends Application 
{
	public static String APP_KEY = "VUEWlsSVIAIzDgOARiSbWZGKzbAZKP43";
	public static String APP_URL = "https://tweetcoahwr.azure-mobile.net/";
	
	public static Context mContext;
	
	private static void init(Context context)
	{
		mContext = context;
	}
	
	@Override
    public void onCreate() 
	{
        super.onCreate();
        init(this.getApplicationContext());
	}
}
