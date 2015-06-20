package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.tweetco.datastore.AccountSingleton;
import com.tweetco.database.dao.Account;

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
