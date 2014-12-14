package com.tweetco;

import com.tweetco.database.dao.Account;

import android.app.Application;
import android.content.Context;

public class TweetCo extends Application 
{
	public static String APP_KEY = "PImqNtOVaoZFzGrQDAcrXwQnpLuZCf69";
	public static String APP_URL = "https://tweetcotest.azure-mobile.net/";
	
	public static Context mContext;
	private static Account mAccount;
	
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

	public static Account getAccount() {
		return mAccount;
	}

	public static void setAccount(Account mAccount) {
		TweetCo.mAccount = mAccount;
	}
	
}
