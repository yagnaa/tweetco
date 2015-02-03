package com.tweetco.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TextView;

import com.onefortybytes.R;
import com.tweetco.utility.UiUtility;

public class UsersListActivity extends TweetCoBaseActivity 
{
	TextView mTitle;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userslist);
		
		mTitle = UiUtility.getView(this, R.id.usersListTitle);
		
		String title = getIntent().getStringExtra("title");
		String usersList = getIntent().getStringExtra("usersList");
		
		if(!TextUtils.isEmpty(title))
		{
			mTitle.setText(title);
		}
		
		if(!TextUtils.isEmpty(usersList))
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        UsersListFragment usersListFragment = new UsersListFragment();
	        Bundle newBundle = new Bundle();
	        newBundle.putBoolean("hideFooter", true);
	        usersListFragment.setArguments(newBundle);
	        usersListFragment.addUser(usersList);
	        ft.replace(R.id.usersListFragmentContainer, usersListFragment);
	        ft.commit();
		}
		
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onResumeCallback() {
		// TODO Auto-generated method stub

	}

}
