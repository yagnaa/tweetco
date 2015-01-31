package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import com.onefortybytes.R;
import com.tweetco.dao.Tweet;
import com.tweetco.tweetlist.TrendingFeedMode;
import com.tweetco.utility.UiUtility;

public class TrendingFragmentActivity extends TweetCoBaseActivity 
{
	private String mTag = null;
	private TweetListFragment tweetListFragment = null;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public TrendingFragmentActivity() {}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.trendingfragmentactivity);

		mTag = getIntent().getExtras().getString(Constants.TREND_TAG_STR);

		ActionBar actionbar = this.getSupportActionBar();
		actionbar.setTitle(mTag);

		if(UiUtility.getView(this, R.id.trendingTweetsListFragmentContainer) != null)
		{
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			tweetListFragment = new TweetListFragment();
			Bundle bundle = new Bundle();
			TrendingFeedMode mode = new TrendingFeedMode(mTag);
			bundle.putParcelable(Constants.TWEET_LIST_MODE, mode);
			bundle.putString(Constants.FOOTER_TAG, "#"+mTag+" ");
			tweetListFragment.setArguments(bundle);
			ft.replace(R.id.trendingTweetsListFragmentContainer, tweetListFragment);
			ft.commit();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == Constants.POSTED_TWEET_REQUEST_CODE)
		{
			if(resultCode == RESULT_OK)
			{
				if(tweetListFragment!=null && tweetListFragment.isVisible())
				{
					Tweet tweet =  data.getParcelableExtra(Constants.POSTED_TWEET);
					tweetListFragment.refreshTop();
				}
			}
		}
		else
		{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onResumeCallback() {
		
	}
}
