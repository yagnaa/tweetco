package com.tweetco.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class TweetCoBaseActivity extends ActionBarActivity 
{
	public static boolean isActivityVisible = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		isActivityVisible = true;
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		isActivityVisible = false;
	}
}
