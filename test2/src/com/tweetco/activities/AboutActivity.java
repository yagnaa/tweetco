package com.tweetco.activities;

import android.os.Bundle;

import com.onefortybytes.R;



public class AboutActivity extends TweetCoBaseActivity 
{
	private final static String TAG = "AboutActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posttweet);
	}

	@Override
	public void onResumeCallback() {
		// TODO Auto-generated method stub
		
	}
		
}
