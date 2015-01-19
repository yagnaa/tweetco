package com.tweetco.activities;
import java.net.MalformedURLException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.imagedisplay.util.AsyncTask;
import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.onefortybytes.R;
import com.tweetco.TweetCo;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.dao.Tweet;
import com.tweetco.database.dao.Account;
import com.tweetco.notifications.PushNotificationHandler;
import com.tweetco.provider.TweetCoProviderConstants;
import com.tweetco.tweets.TweetCommonData;



public class AllInOneActivity extends TweetCoBaseActivity
{
	private static final int IO_BUFFER_SIZE = 8 * 1024;

	@Override
	protected void onNewIntent(Intent intent) 
	{
		super.onNewIntent(intent);
		this.setIntent(intent);
	}


	public static final String SENDER_ID = "721884328218";


	private static final int SEVER_SIDE_BATCH_SIZE = 10; //Number of tweets fetched from server at one time
	private static final String IMAGE_CACHE_DIR = "thumbs"; //Name of directory where images are saved


	private Handler handler;

	private ActionBar m_actionbar;

	private static final String TAG = "AllInOneActivity";

	private ViewPager mViewPager;
	private static CustomFragmentPagerAdapter mPagerAdapter = null;
	private AsyncTaskEventHandler asyncTaskEventHandler = null;
	private Controller mController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Loading...");
		mController = new Controller();

		setContentView(R.layout.all_in_one_activity_layout);

		NotificationsManager.handleNotifications(this, SENDER_ID, PushNotificationHandler.class);
		

		customizeActionBar();
		
		if(TweetCommonData.getAccount()!=null && TweetCommonData.mClient!=null)
		{
			initializePager();
		}
		else
		{
			(new InitializeTask()).execute();
		}
	}


	public void customizeActionBar()
	{
		m_actionbar = getSupportActionBar();
		m_actionbar.setDisplayShowHomeEnabled(false);
		m_actionbar.setDisplayShowTitleEnabled(false);
		View customView =  LayoutInflater.from(this).inflate(R.layout.custom_action_bar, null);
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		m_actionbar.setCustomView(customView, params);
		m_actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ~ActionBar.DISPLAY_SHOW_HOME);
		m_actionbar.setDisplayHomeAsUpEnabled(false);
		
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
		super.onResume();
		
		
		if(TweetCommonData.getAccount()!=null && TweetCommonData.mClient!=null)
		{
			initializePager();
			
			ImageView imageView = (ImageView)m_actionbar.getCustomView().findViewById(R.id.imageView1);
			ImageFetcher imageFetcher = Utils.getImageFetcher(this, 50, 50);
			
			imageFetcher.loadImage(TweetCommonData.getAccount().profileimageurl, imageView);
			
			imageView.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					Intent intent = new Intent(AllInOneActivity.this , UserProfileActivity.class);
					intent.putExtra(Constants.USERNAME_STR, TweetCommonData.getUserName());
					AllInOneActivity.this.startActivityForResult(intent, Constants.POSTED_TWEET_REQUEST_CODE);
				}
			});

		}
		else
		{
			(new InitializeTask()).execute();
		}
		
		
	}

	@Override
	protected void onPause() 
	{

		super.onPause();
	}


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
		else
		{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}


	private class InitializeTask extends AsyncTask<Void, Void, Account>
	{

		
		@Override
		protected void onPreExecute()
		{
			Log.d("tag","onPreExecute");
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(Account result) 
		{
			Log.e(TAG,"AllInOneActivity post execute");
			setProgressBarIndeterminateVisibility(false);
			if(result == null)
			{
				Intent intent = new Intent(TweetCo.mContext,LauncherActivity.class);
				TweetCo.mContext.startActivity(intent);
				AllInOneActivity.this.finish();
			}
			else
			{
				initializePager();
				
				ImageView imageView = (ImageView)m_actionbar.getCustomView().findViewById(R.id.imageView1);
				ImageFetcher imageFetcher = Utils.getImageFetcher(AllInOneActivity.this, 50, 50);
				
				imageFetcher.loadImage(TweetCommonData.getAccount().profileimageurl, imageView);
				
				imageView.setOnClickListener(new View.OnClickListener() 
				{	
					@Override
					public void onClick(View v) 
					{
						Intent intent = new Intent(AllInOneActivity.this , UserProfileActivity.class);
						intent.putExtra(Constants.USERNAME_STR, TweetCommonData.getUserName());
						AllInOneActivity.this.startActivityForResult(intent, Constants.POSTED_TWEET_REQUEST_CODE);
					}
				});
			}
		}

		@Override
		protected Account doInBackground(Void... params) 
		{
			Account account = getAccount();
			if(account != null)
			{

				MobileServiceClient mobileServiceClient;
				try 
				{
					mobileServiceClient = new MobileServiceClient(TweetCo.APP_URL, TweetCo.APP_KEY, TweetCo.mContext);
					MobileServiceUser user = new MobileServiceUser(account.getUsername());
					user.setAuthenticationToken(account.getAuthToken());
					mobileServiceClient.setCurrentUser(user);
					TweetCommonData.mClient = mobileServiceClient;
					TweetCommonData.setAccount(account);
				} 
				catch (MalformedURLException e) 
				{
					e.printStackTrace();
				}

			}
			return account;
		}

		private Account getAccount()
		{
			Account account = null;

			Cursor c = TweetCo.mContext.getContentResolver().query(TweetCoProviderConstants.ACCOUNT_CONTENT_URI, null, null, null, null);
			if(c.moveToFirst())
			{
				account = new Account();
				account.restoreFromCursor(c);
			}

			return account;
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
			twwetListFragment.refreshTop();
		}
	}

}
