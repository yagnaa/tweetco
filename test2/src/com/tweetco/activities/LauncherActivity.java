package com.tweetco.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.tweetco.R;
import com.tweetco.TweetCo;
import com.tweetco.database.dao.Account;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.displayingbitmaps.ui.AllInOneActivity;

public class LauncherActivity extends TweetCoBaseActivity 
{
	private boolean bActvityDestroyed = false;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
		new Thread(new Runnable() {
			
			@Override
			public void run() 
			{
				try 
				{
					Thread.sleep(1000);
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
							TweetCommonData.setAccount(account);
							startActivity(new Intent(getApplicationContext(), AllInOneActivity.class));
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}).start();
    }
    
    protected void launchFtu() 
    {
		Intent intent = new Intent(getApplicationContext(), ServiceDirectedLoginFTUActivity.class);
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
