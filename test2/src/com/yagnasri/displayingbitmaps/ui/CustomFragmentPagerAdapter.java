package com.yagnasri.displayingbitmaps.ui;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.tweetco.activities.Constants;
import com.tweetco.activities.LeaderboardFragment;
import com.tweetco.activities.TrendingFragment;
import com.tweetco.activities.UsersListFragment;
import com.tweetco.tweetlist.HomeFeedMode;
import com.tweetco.tweets.TweetCommonData;

public class CustomFragmentPagerAdapter extends FragmentStatePagerAdapter
{
	public static final int FRAGMENT_COUNT = 4;
	
	 SparseArray<WeakReference<Fragment>> mFragmentsMap = new SparseArray<WeakReference<Fragment>>();

	public CustomFragmentPagerAdapter(Context context, FragmentManager fm)
	{
		super(fm);
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
			return "Home";
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
			HomeFeedMode mode = new HomeFeedMode(TweetCommonData.getUserName());
			bundle.putParcelable(Constants.TWEET_LIST_MODE, mode);
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
	
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mFragmentsMap.put(position, new WeakReference<Fragment>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) 
    {
    	mFragmentsMap.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) 
    {
    	WeakReference<Fragment> weakReference = mFragmentsMap.get(position);
        return weakReference.get();
    }

}