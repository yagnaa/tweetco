package com.yagnasri.displayingbitmaps.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tweetco.TweetCo;
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
			Fragment fragment = new TweetListFragment();
			Bundle bundle = new Bundle();
            bundle.putString("username", TweetCo.getAccount().getUsername());
            bundle.putBoolean("gettweetsbyuser", false);
            fragment.setArguments(bundle);
			return fragment;
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