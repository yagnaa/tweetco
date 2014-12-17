package com.yagnasri.displayingbitmaps.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tweetco.activities.LeaderboardFragment;

public class CustomFragmentPagerAdapter extends FragmentStatePagerAdapter
{
	public static final int FRAGMENT_COUNT = 3;
	

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
	public Fragment getItem(int i)
	{
		switch(i)
		{
		case 0:
			return new TweetListFragment();
		case 1:
			return new LeaderboardFragment();
		case 2:
		default:	
			return new TweetListFragment();
		}
	}

	@Override
	public int getCount()
	{
		return FRAGMENT_COUNT;
	}

}