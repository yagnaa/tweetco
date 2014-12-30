package com.tweetco.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class TweetCoBaseActivity extends ActionBarActivity 
{
	public static boolean isAppInForeground = false;
	public static Activity topActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancelAll();
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
		// TODO Auto-generated method stub
		super.onResume();
		isAppInForeground = true;
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		isAppInForeground = false;
	}
}
