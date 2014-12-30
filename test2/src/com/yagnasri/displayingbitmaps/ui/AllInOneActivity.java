package com.yagnasri.displayingbitmaps.ui;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.tweetco.R;
import com.tweetco.activities.Constants;
import com.tweetco.activities.TweetCoBaseActivity;
import com.tweetco.notifications.PushNotificationHandler;
import com.yagnasri.dao.Tweet;



public class AllInOneActivity extends TweetCoBaseActivity
{

	public static final String SENDER_ID = "721884328218";
	
	
	private static final int SEVER_SIDE_BATCH_SIZE = 10; //Number of tweets fetched from server at one time
    private static final String IMAGE_CACHE_DIR = "thumbs"; //Name of directory where images are saved

	
	private Handler handler;
	
	private ActionBar m_actionbar;
	
	private static final String TAG = "AllInOneActivity";
	
	private ViewPager mViewPager;
	private static CustomFragmentPagerAdapter mPagerAdapter = null;
	
	private Controller mController = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mController = new Controller();

		setContentView(R.layout.all_in_one_activity_layout);
		
		NotificationsManager.handleNotifications(this, SENDER_ID, PushNotificationHandler.class);
        
        initializePager();
        
        customizeActionBar();
	}
	
	
	public void customizeActionBar()
	{
		m_actionbar = getActionBar();
		m_actionbar.setDisplayShowHomeEnabled(false);
		m_actionbar.setDisplayShowTitleEnabled(false);
		View customView =  LayoutInflater.from(this).inflate(R.layout.custom_action_bar, null);
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		m_actionbar.setCustomView(customView, params);
		m_actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ~ActionBar.DISPLAY_SHOW_HOME);
		m_actionbar.setDisplayShowCustomEnabled(true);
	}
	
	private void hideKeyboard() 
	{   
	    // Check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	public void initializePager()
	{	
		// init pager
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageScrollStateChanged(int state)
			{
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}

			@Override
			public void onPageSelected(int position)
			{
				hideKeyboard();
			}
		});
		
		mPagerAdapter = new CustomFragmentPagerAdapter(this.getApplicationContext(), getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
		mViewPager.setCurrentItem(0);
	}
	
	public FragmentStatePagerAdapter getPagerAdapter()
	{
		return mPagerAdapter;
	}

	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
//	
//	public static void tweetsListRefresh()
//	{
//		mController.
//	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == Constants.POSTED_TWEET_REQUEST_CODE)
		{
			if(resultCode == RESULT_OK)
			{
				Tweet tweet =  data.getParcelableExtra(Constants.POSTED_TWEET);
				mController.tweetsListRefresh(tweet);
			}
		}
	}
	
	public Controller getController()
	{
		return mController;
	}
	
	
	/**
	 * All the actions that has to be done on the fragments will be done here.
	 *
	 */
	public class Controller
	{
		public void tweetsListRefresh(Tweet tweet)
		{
			//Ideally we should call mPagerAdapter.getFragmentByClass(classname);
			TweetListFragment twwetListFragment = (TweetListFragment)mPagerAdapter.getRegisteredFragment(0);
			twwetListFragment.refresh();
		}
	}
	
}