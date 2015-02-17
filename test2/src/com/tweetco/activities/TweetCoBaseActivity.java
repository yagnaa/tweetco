package com.tweetco.activities;

import java.net.MalformedURLException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.imagedisplay.util.AsyncTask;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.tweetco.TweetCo;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;

public abstract class TweetCoBaseActivity extends ActionBarActivity 
{
	private static final String TAG = "TweetCoBaseActivity";
	public static boolean isAppInForeground = false;
	public static Activity topActivity = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		topActivity =  this;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		isAppInForeground = true;
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancelAll();
		
		if(TweetCommonData.mClient == null)
		{
			new InitializeTask().execute();
		}
		else
		{
			onResumeCallback();
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		isAppInForeground = false;
	}
	
	public abstract void onResumeCallback();
	
	private class InitializeTask extends AsyncTask<Void, Void, Account>
	{
		@Override
		protected void onPreExecute()
		{
			Log.d("tag","onPreExecute");
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(Account result) 
		{
			Log.d(TAG,"AllInOneActivity post execute");
			setProgressBarIndeterminateVisibility(false);
			if(result == null)
			{
				Intent intent = new Intent(TweetCo.mContext,LauncherActivity.class);
				TweetCo.mContext.startActivity(intent);
				TweetCoBaseActivity.this.finish();
			}
			else
			{
				onResumeCallback();
			}
		}

		@Override
		protected Account doInBackground(Void... params) 
		{
			Account account = getAccount();
			if(account != null)
			{

				MobileServiceClient mobileServiceClient;
				try 
				{
					TweetCo.APP_URL = account.getServerAddress();
					TweetCo.APP_KEY = account.getAuthToken();
					mobileServiceClient = new MobileServiceClient(TweetCo.APP_URL, TweetCo.APP_KEY, TweetCo.mContext);
					MobileServiceUser user = new MobileServiceUser(account.getUsername());
					mobileServiceClient.setCurrentUser(user);
					TweetCommonData.mClient = mobileServiceClient;
					TweetCommonData.setAccount(account);
				} 
				catch (MalformedURLException e) 
				{
					e.printStackTrace();
				}

			}
			return account;
		}

		private Account getAccount()
		{
			Account account = null;

			Cursor c = TweetCo.mContext.getContentResolver().query(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, null, null, null, null);
			if(c.moveToFirst())
			{
				account = new Account();
				account.restoreFromCursor(c);
			}

			return account;
		}

	}
}
