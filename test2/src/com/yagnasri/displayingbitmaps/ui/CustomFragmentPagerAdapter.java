package com.yagnasri.displayingbitmaps.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tweetco.TweetCo;
import com.tweetco.activities.LeaderboardFragment;
import com.tweetco.activities.TrendingFragment;
import com.tweetco.activities.UsersListFragment;

public class CustomFragmentPagerAdapter extends FragmentStatePagerAdapter
{
	public static final int FRAGMENT_COUNT = 4;
	

	private Context m_Context = null;

	public CustomFragmentPagerAdapter(Context context, FragmentManager fm)
	{
		super(fm);
		m_Context = context.getApplicationContext();
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		try {
			super.notifyDataSetChanged();
		} catch (Exception e) {
    		e.printStackTrace();
		}
	}
	
	


	@Override
	public CharSequence getPageTitle(int position) {
		
		switch(position)
		{
		case 0:
			return "Tweets";
		case 1:
			return "LeaderBoard";
		case 2:
			return "Users";
		case 3:
			return "Trending";
		}
		return null;
	}

	@Override
	public Fragment getItem(int i)
	{
		switch(i)
		{
		case 0:
			Fragment fragment = new TweetListFragment();
			Bundle bundle = new Bundle();
            bundle.putString("username", TweetCo.getAccount().getUsername());
            bundle.putBoolean("gettweetsbyuser", false);
            fragment.setArguments(bundle);
			return fragment;
		case 1:
			return new LeaderboardFragment();
		case 2:	
			fragment = new UsersListFragment();
			return fragment;
		case 3:
			return new TrendingFragment();
		}
		return null;
	}

	@Override
	public int getCount()
	{
		return FRAGMENT_COUNT;
	}

}