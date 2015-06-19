package com.tweetco.activities;

import java.net.MalformedURLException;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.onefortybytes.R;
import com.tweetco.TweetCo;
import com.tweetco.account.AccountSingleton;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;

public class LauncherActivity extends ActionBarActivity 
{
	private boolean bActvityDestroyed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new Thread(new Runnable() {

			@Override
			public void run() 
			{
				Account account = AccountSingleton.INSTANCE.getAccountModel().getAccountCopy();
				if(account == null)
				{
					if(!bActvityDestroyed) {
						launchFtu();
						finish();
					}
				}
				else
				{
					if(!bActvityDestroyed) {
						startActivity(new Intent(getApplicationContext(), AllInOneActivity.class));
						finish();
					}
				}

			}
		}).start();
	}

	protected void launchFtu() 
	{
		Intent intent = new Intent(getApplicationContext(), FTUActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onDestroy()
	{
		bActvityDestroyed = true;
		super.onDestroy();
	}
}
