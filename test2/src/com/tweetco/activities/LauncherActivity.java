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
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;

public class LauncherActivity extends ActionBarActivity 
{
	private boolean bActvityDestroyed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.splash_layout);
		new Thread(new Runnable() {

			@Override
			public void run() 
			{
					//Thread.sleep(1000);
					if(!bActvityDestroyed)
					{
						Account account = getAccount();
						if(account == null)
						{
							launchFtu();
							finish();
						}
						else
						{
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

	private Account getAccount()
	{
		Account account = null;

		Cursor c = getContentResolver().query(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, null, null, null, null);
		if(c.moveToFirst())
		{
			account = new Account();
			account.restoreFromCursor(c);
		}

		return account;
	}
}
