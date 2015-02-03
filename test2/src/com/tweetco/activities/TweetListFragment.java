/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tweetco.activities;


import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.onefortybytes.R;
import com.tweetco.TweetCo;
import com.tweetco.activities.PageLoader.OnLoadCompletedCallback;
import com.tweetco.activities.TweetAdapter.NewPageLoader;
import com.tweetco.activities.TweetAdapter.OnProfilePicClick;
import com.tweetco.activities.TweetAdapter.OnTweetClick;
import com.tweetco.dao.Tweet;
import com.tweetco.tweetlist.TrendingFeedMode;
import com.tweetco.tweetlist.TweetListMode;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class TweetListFragment extends Fragment implements AdapterView.OnItemClickListener
{
	private static final String TAG = "TweetListFragment";


	private TweetAdapter mAdapter;

	//The first imageFetcher loads profileImages and the second one loads the tweetcontent images.
	ImageFetcher mImageFetcher;
	ImageFetcher mImageFetcher2;


	private View popupView;
	private PopupWindow popupWindow;



	private TweetListMode tweetListMode = null;


	private NewPageLoader mNewPageLoader; //Fetches the tweets

	private GestureDetectorCompat mDetector; 

	//Scrolling from bottom
	private QuickReturnListView mListView;
	private LinearLayout mQuickReturnView;
	private int mQuickReturnHeight;
	private static final int STATE_ONSCREEN = 0;
	private static final int STATE_OFFSCREEN = 1;
	private static final int STATE_RETURNING = 2;
	private int mState = STATE_ONSCREEN;
	private int mScrollY= 0;
	private int mMinRawY = 0;
	int translationY = 0;
	private boolean prevSign = false;
	private TranslateAnimation anim;

	Timer timer = null;
	
	ResponseReceiver responseReceiver = null;


	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public TweetListFragment() 
	{
		Log.v(TAG, "created");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate && savedInstanceState=" + (savedInstanceState!=null?"true":"false"));
		setHasOptionsMenu(true);

		tweetListMode = (TweetListMode)getArguments().getParcelable(Constants.TWEET_LIST_MODE);

		mDetector = new GestureDetectorCompat(this.getActivity().getApplicationContext(), new MyGestureListener());
		
		
		responseReceiver = new ResponseReceiver();

		mImageFetcher = Utils.getImageFetcher(getActivity(), 60, 60);

		mImageFetcher2 = Utils.getImageFetcher(getActivity(), 60, 60);

		mAdapter = new TweetAdapter(getActivity(), mImageFetcher,mImageFetcher2, tweetListMode, new OnProfilePicClick() {

			@Override
			public void onItemClick(int position) {
				//Show user profile view
				Tweet tweet = (Tweet)mAdapter.getItem(position);
				if(tweet != null)
				{
					String owner = tweet.tweetowner;
					if(!TextUtils.isEmpty(owner))
					{
						Intent intent = new Intent(getActivity(), UserProfileActivity.class);
						intent.putExtra(Constants.USERNAME_STR, owner);
						getActivity().startActivity(intent);
					}
				}

			}
		}, new OnTweetClick() {
			
			@Override
			public void onItemClick(int position) 
			{
				Tweet tweet = (Tweet)mAdapter.getItem(position);
				if(tweet != null)
				{
					Intent intent = new Intent(getActivity(), TweetDetailActivity.class);
					intent.putExtra("Tweet", tweet);
					getActivity().startActivity(intent);
				}
			}
		});

		mNewPageLoader = new PageLoader(tweetListMode);

	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Log.v(TAG, "onAttach");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.v(TAG, "onConfigurationChanged");
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		Log.v(TAG, "onDetach");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.v(TAG, "onSaveInstanceState");
	}

	@Override
	public void onStart() 
	{
		super.onStart();
		Log.v(TAG, "onStart");
		IntentFilter mStatusIntentFilter = new IntentFilter(
                com.tweetco.activities.PageLoader.Constants.BROADCAST_ACTION);
		
		 LocalBroadcastManager.getInstance(TweetCo.mContext).registerReceiver(responseReceiver, mStatusIntentFilter);
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(TAG, "onStop");
		LocalBroadcastManager.getInstance(TweetCo.mContext).unregisterReceiver(responseReceiver);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.v(TAG, "onViewCreated savedInstanceState=" + (savedInstanceState!=null?"true":"false"));
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {

		super.onViewStateRestored(savedInstanceState);
		Log.v(TAG, "onViewStateRestored savedInstanceState=" + (savedInstanceState!=null?"true":"false"));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		LayoutInflater layoutInflater = (LayoutInflater)TweetCo.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup activityViewRoot = ((ViewGroup)this.getView().findViewById(R.id.listView));
		popupView = layoutInflater.inflate(R.layout.popup, activityViewRoot,false);
		popupWindow = new PopupWindow(popupView,android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
		popupWindow.setOutsideTouchable(true);

		Log.v(TAG, "onActivityCreated savedInstanceState=" + (savedInstanceState!=null?"true":"false"));
		EditText typeTweet = (EditText) mQuickReturnView.findViewById(R.id.typeTweet);
		typeTweet.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				EditText typeTweet = (EditText) mQuickReturnView.findViewById(R.id.typeTweet);
				launchPostTweetActivity(typeTweet.getText().toString());
			}
		});

		mQuickReturnView.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				EditText typeTweet = (EditText) mQuickReturnView.findViewById(R.id.typeTweet);
				launchPostTweetActivity(typeTweet.getText().toString());
			}
		});
	}

	/**
	 * This should be called when there is new data to load. ie. Push Notification or a tweet being posted.
	 */
	public void refreshTop()
	{

		//TODO Build the request for the load
		mNewPageLoader.loadTop(new OnLoadCompletedCallback() {

			@Override
			public void onLoadCompleted(int numOfTweetsLoaded, boolean endOfList) {


				int index = mListView.getFirstVisiblePosition();
				View v = mListView.getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();

				int positionOfList = index + numOfTweetsLoaded;
				if(numOfTweetsLoaded>0)
				{
					mAdapter.notifyDataSetChanged(); //TODO correct this.
				}

				mListView.setSelectionFromTop(positionOfList, top);

				if(index!=positionOfList)
				{
					showNewTweetPopup();
				}

				if (endOfList) 
				{
					mAdapter.notifyEndOfList();
				} 
				else 
				{
					mAdapter.notifyHasMore();
				}

			}
		});
	}

	public void launchPostTweetActivity(String existingString)
	{
		Intent intent = new Intent(this.getActivity().getApplicationContext(),PostTweetActivity.class);
		intent.putExtra(Constants.EXISTING_STRING, existingString);
		this.getActivity().startActivityForResult(intent, Constants.POSTED_TWEET_REQUEST_CODE);
	}


	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.tweetlist, container, false);


		mQuickReturnView = (LinearLayout) v.findViewById(R.id.footer);
		if(getArguments().getBoolean(Constants.HIDE_FOOTER, false))
		{
			mQuickReturnView.setVisibility(View.GONE);

		}
		else if(getArguments().getString(Constants.FOOTER_TAG)!=null)
		{
			((TextView)mQuickReturnView.findViewById(R.id.typeTweet)).setText(getArguments().getString(Constants.FOOTER_TAG));
		}

		mListView = (QuickReturnListView) v.findViewById(R.id.listView);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// Before Honeycomb pause image loading on scroll to help with performance
					if (!Utils.hasHoneycomb()) {
						mImageFetcher.setPauseWork(true);
					}
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				//Start Loading tweets in the backing adapter
				loadDataOnScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);

				//End Loading tweets in the backing adapter

				//Loading the Quick return View - begin

				//				mScrollY = 0;
				//
				//
				//
				//				mScrollY = mListView.getComputedScrollY();
				//				int diff = mScrollY - mMinRawY;
				//				boolean abrupt = Math.abs(mScrollY - mMinRawY) > 50;
				//				boolean currentSign = (mScrollY < mMinRawY) ;
				//
				//				if(!abrupt)
				//				{
				//					switch (mState) {
				//
				//					case STATE_ONSCREEN:
				//						if((diff > 0) )
				//						{
				//							mState = STATE_RETURNING;
				//							translationY = diff;
				//						}
				//						else
				//						{
				//							translationY = 0;
				//						}
				//						break;
				//
				//					case STATE_OFFSCREEN:
				//						if(currentSign!=prevSign)
				//						{
				//							mState = STATE_RETURNING;
				//						}
				//						translationY = mQuickReturnHeight;
				//						break;
				//
				//
				//					case STATE_RETURNING:
				//
				//						translationY += diff;
				//
				//						if (translationY < 0) 
				//						{
				//							translationY = 0;
				//							mState = STATE_ONSCREEN;
				//						}
				//
				//
				//						if (translationY > mQuickReturnHeight) 
				//						{
				//							mState = STATE_OFFSCREEN;
				//						}
				//						break;
				//					}
				//					prevSign = currentSign;
				//				}
				//
				//				mMinRawY = mScrollY;
				//
				//				/** this can be used if the build is below honeycomb **/
				//				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) 
				//				{
				//					anim = new TranslateAnimation(0, 0, translationY,
				//							translationY);
				//					anim.setFillAfter(true);
				//					anim.setDuration(0);
				//					mQuickReturnView.startAnimation(anim);
				//				} else {
				//					mQuickReturnView.setTranslationY(translationY);
				//				}

				//Loading the Quick return View - end

			}
		});

		// This listener is used to get the final width of the GridView and then calculate the
		// number of columns and the width of each column. The width of each column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used to set the height
		// of each view so we get nice square thumbnails.
		mListView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() 
				{

					@TargetApi(VERSION_CODES.JELLY_BEAN)
					@Override
					public void onGlobalLayout() 
					{
						Log.v(TAG, "onGlobalLayout layout Done");
						int visibleChildCount = (mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition()) + 1;
						loadDataOnScroll(mListView, mListView.getFirstVisiblePosition(), visibleChildCount, mListView.getAdapter().getCount());
						mQuickReturnHeight = mQuickReturnView.getHeight();
					}
				});

		mListView.setOnTouchListener(new OnTouchListener() 
		{
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				mDetector.onTouchEvent(event);
				return false;
			}
		});
		return v;
	}

	@Override
	public void onResume() 
	{
		super.onResume();
		Log.v(TAG, "onResume");
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();

		boolean launchedFromNotification = this.getActivity().getIntent().getBooleanExtra(Constants.LAUNCHED_FROM_NOTIFICATIONS, false);
		if(launchedFromNotification)
		{
			refreshTop();
		}

		trendingTimerTask();
	}

	@Override
	public void onPause() 
	{
		super.onPause();
		Log.v(TAG, "onPause");
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		Log.v(TAG, "onDestroy ");
		mImageFetcher.closeCache();
	}


	private void loadDataOnScroll(AbsListView absListView, int firstVisibleItem,
			int visibleItemCount, int totalItemCount)
	{
		// In scroll-to-bottom-to-load mode, when the sum of first visible position and visible count equals the total number
		// of items in the adapter it reaches the bottom
		int bufferItemsToShow = mAdapter.getCount() -(firstVisibleItem + visibleItemCount);
		Log.d(TAG, "There are getCount()="+ mAdapter.getCount()+" firstVisibleItem="+firstVisibleItem+ " visibleItemCount="+visibleItemCount);
		if((bufferItemsToShow < PageLoader.TWEET_LOAD_BUFFER  && mAdapter.canScroll()))
		{
			onScrollNext();
		}
	}


	public void onScrollNext() 
	{
		if (mNewPageLoader != null) 
		{
			//TODO Build the request for the load
			mNewPageLoader.loadNext(new OnLoadCompletedCallback() {

				@Override
				public void onLoadCompleted(int numOfTweetsLoaded, boolean endOfList) 
				{
					// save index and top position
					int index = mListView.getFirstVisiblePosition();
					View v = mListView.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();

					int positionOfList = index;
					if(numOfTweetsLoaded>0)
					{
						mAdapter.notifyDataSetChanged(); //TODO correct this.
					}

					mListView.setSelectionFromTop(positionOfList, top);

					if(index!=positionOfList)
					{
						showNewTweetPopup();
					}

					// Add or remove the loading view depend on if there might be more to load
					//TODO spinner at the bottom
					if (endOfList) 
					{
						mAdapter.notifyEndOfList();
					} 
					else 
					{
						mAdapter.notifyHasMore();
					}

				}
			});
		}
	}

	@TargetApi(VERSION_CODES.JELLY_BEAN)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		//        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
		//        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
		//        if (Utils.hasJellyBean()) {
		//            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
		//            // show plus the thumbnail image in GridView is cropped. so using
		//            // makeScaleUpAnimation() instead.
		//            ActivityOptions options =
		//                    ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
		//            getActivity().startActivity(i, options.toBundle());
		//        } else {
		//            startActivity(i);
		//        }
		//TODO when a tweet is clicked
		if(v.getTag() != null)
		{
			String tag = v.getTag().toString();
			if("image".equals(tag))
			{

			}
		}
		else
		{
			//Show tweet's detailed view
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// inflater.inflate(R.menu.main_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//        switch (item.getItemId()) {
		//            case R.id.clear_cache:
		//                mImageFetcher.clearCache();
		//                Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
		//                        Toast.LENGTH_SHORT).show();
		//                return true;
		//        }
		return super.onOptionsItemSelected(item);
	}


	public  class MyGestureListener extends GestureDetector.SimpleOnGestureListener 
	{
		private static final String DEBUG_TAG = "Gestures"; 

		@Override
		public boolean onDown(MotionEvent event) { 
			if(event != null)
			{
				Log.d(DEBUG_TAG,"onDown: " + event.toString()); 
			}

			return true;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, 
				float velocityX, float velocityY) 
		{
			//	mQuickReturnView.setTranslationY(mQuickReturnHeight);
			//	mState = STATE_OFFSCREEN;
			if(event1 != null && event2 != null)
			{
				Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent event1, MotionEvent event2,
				float distanceX, float distanceY) 
		{
			if(event1 != null && event2 != null)
			{
				Log.d(DEBUG_TAG, "onScroll: " + event1.toString()+event2.toString());
			}

			return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == Constants.POSTED_TWEET_REQUEST_CODE)
		{
			if(resultCode == Activity.RESULT_OK)
			{
				refreshTop();
			}
		}
	}

	public void showNewTweetPopup()
	{
		View view =  this.getView();
		
		if(view != null)
		{
			View activityViewRoot = (view.findViewById(R.id.listView));
			if(!popupWindow.isShowing())
			{

				View v = mListView.getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();
				popupWindow.showAtLocation(activityViewRoot, Gravity.CENTER, 0, -300);
			}
		}
		
	}


	public void trendingTimerTask()
	{
		if(tweetListMode instanceof TrendingFeedMode)
		{
			timer = new Timer("Trending TimedTask");
			timer.scheduleAtFixedRate(new TimerTask() 
			{		
				@Override
				public void run() 
				{
					refreshTop();
				}
			}, 15000, 30000);
		}
	}

	public void cancelLoadTweetTask()
	{
		if(timer!=null)
		{
			timer.cancel();
		}
	}
	
	// Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver
	{
	    // Prevents instantiation
	    private ResponseReceiver() {
	    }

	    @Override
	    public void onReceive(Context context, Intent intent) 
	    {
	    	TweetListMode mTweetlistMode = intent.getParcelableExtra(Constants.TWEETMODE_UPDATED);
	    	if(tweetListMode!=null && (tweetListMode.getClass().equals(mTweetlistMode.getClass())))
	    	{
	    		mAdapter.notifyDataSetChanged();
	    	}
	    	
	    }
	}
}
