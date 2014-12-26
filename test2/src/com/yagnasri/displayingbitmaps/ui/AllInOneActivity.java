package com.yagnasri.displayingbitmaps.ui;
import java.net.MalformedURLException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tweetco.R;



public class AllInOneActivity extends FragmentActivity
{
	private static final int SEVER_SIDE_BATCH_SIZE = 10; //Number of tweets fetched from server at one time
    private static final String IMAGE_CACHE_DIR = "thumbs"; //Name of directory where images are saved
	
	public static MobileServiceClient mClient;
	
	private Handler handler;
	
	private static final String TAG = "ImageGridActivity";
	
	private ViewPager mViewPager;
	private static CustomFragmentPagerAdapter mPagerAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.all_in_one_activity_layout);

        // Initialize the demo client here and use it all over.
        
		try {
			mClient = new MobileServiceClient(
				      "https://tweetcosap.azure-mobile.net/",
				      "GeDacEmoZtCjASmINhXiLWpBujCtJI48", 
				      this
				);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        initializePager();

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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public static void tweetsListRefresh()
	{
		((TweetListFragment)mPagerAdapter.getItem(0)).refresh();
	}
	
}