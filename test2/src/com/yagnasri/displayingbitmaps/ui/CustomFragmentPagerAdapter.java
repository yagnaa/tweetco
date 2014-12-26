package com.yagnasri.displayingbitmaps.ui;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tweetco.TweetCo;
import com.tweetco.activities.LeaderboardFragment;
import com.tweetco.activities.TrendingFragment;
import com.tweetco.activities.UsersListFragment;
import com.tweetco.tweets.TweetCommonData;

public class CustomFragmentPagerAdapter extends FragmentStatePagerAdapter
{
	public static final int FRAGMENT_COUNT = 4;
	

	private Context m_Context = null;
	private Map<Integer, Fragment> mFragmentsMap;

	public CustomFragmentPagerAdapter(Context context, FragmentManager fm)
	{
		super(fm);
		m_Context = context.getApplicationContext();
		mFragmentsMap = new HashMap<Integer, Fragment>();
		
		Fragment fragment = new TweetListFragment();
		Bundle bundle = new Bundle();
        bundle.putString(TweetListFragment.USERNAME, TweetCommonData.getUserName());
        fragment.setArguments(bundle);
        mFragmentsMap.put(0, fragment);
        
        mFragmentsMap.put(1, new LeaderboardFragment());
        mFragmentsMap.put(2, new UsersListFragment());
        mFragmentsMap.put(3, new TrendingFragment());
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
		if(i <= 3)
		{
			return mFragmentsMap.get(i);
		}
		return null;
	}

	@Override
	public int getCount()
	{
		return FRAGMENT_COUNT;
	}

}