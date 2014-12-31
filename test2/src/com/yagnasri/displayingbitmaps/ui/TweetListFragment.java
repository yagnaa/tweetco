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

package com.yagnasri.displayingbitmaps.ui;


import java.lang.reflect.Type;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.R;
import com.tweetco.activities.Constants;
import com.tweetco.activities.PostTweetActivity;
import com.tweetco.activities.QuickReturnListView;
import com.tweetco.activities.UserProfileFragment;
import com.tweetco.tweetlist.TweetListMode;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.dao.Tweet;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.TweetAdapter.NewPageLoader;
import com.yagnasri.displayingbitmaps.ui.TweetAdapter.OnProfilePicClick;
import com.yagnasri.displayingbitmaps.util.ImageCache;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;
import com.yagnasri.displayingbitmaps.util.Utils;

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
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private TweetAdapter mAdapter;


	private TweetUserLoader tweetUserLoader; //Loads user data


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


	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public TweetListFragment() {
		Log.v(TAG, "created");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate && savedInstanceState=" + (savedInstanceState!=null?"true":"false"));
		setHasOptionsMenu(true); 

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		TweetListMode mode = (TweetListMode)getArguments().getParcelable(Constants.TWEET_LIST_MODE);

		ImageCache.ImageCacheParams cacheParams =
				new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		mDetector = new GestureDetectorCompat(this.getActivity().getApplicationContext(), new MyGestureListener());

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		Resources r = getResources();
		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());

		TweetCommonData.mImageFetcher = new ImageFetcher(getActivity(), px,px, true);
		TweetCommonData.mImageFetcher.setLoadingImage(R.drawable.ic_launcher);
		TweetCommonData.mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

		ImageFetcher mImageFetcher2 = new ImageFetcher(getActivity(), px,px, true);
		mImageFetcher2.setLoadingImage(R.drawable.ic_launcher);
		mImageFetcher2.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

		mAdapter = new TweetAdapter(getActivity(), TweetCommonData.mImageFetcher,mImageFetcher2, mode, new OnProfilePicClick() {

			@Override
			public void onItemClick(int position) {
				//Show user profile view
				Tweet tweet = (Tweet)mAdapter.getItem(position);
				if(tweet != null)
				{
					String owner = tweet.tweetowner;
					if(!TextUtils.isEmpty(owner))
					{
						Intent intent = new Intent(getActivity(), UserProfileFragment.class);
						intent.putExtra(Constants.USERNAME_STR, owner);
						getActivity().startActivity(intent);
					}
				}

			}
		});

		//		Handler handler = new Handler();
		//		handler.postDelayed(new Runnable() {
		//			
		//			@Override
		//			public void run() 
		//			{
		//				ActionBar actionbar = TweetListFragment.this.getActivity().getActionBar();
		//				ImageView imageView = (ImageView)actionbar.getCustomView().findViewById(R.id.imageView1);
		//				
		//				TweetUser tweetUser = TweetCommonData.tweetUsers.get(TweetCommonData.getUserName());
		//				if(tweetUser!=null && tweetUser.profileimageurl!=null)
		//				{
		//				TweetCommonData.mImageFetcher.loadImage(tweetUser.profileimageurl, imageView);
		//				}
		//				
		//			}
		//		}, 10000);

		mNewPageLoader = new PageLoader();

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
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(TAG, "onStop");
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
		Log.v(TAG, "onActivityCreated savedInstanceState=" + (savedInstanceState!=null?"true":"false"));
		this.getView().findViewById(R.id.typeTweet).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) 
			{
				launchPostTweetActivity();
			}
		});

		this.getView().findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) 
			{
				launchPostTweetActivity();
			}
		});

		this.getView().findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) 
			{
				launchPostTweetActivity();
			}
		});
		

		tweetUserLoader = new TweetUserLoader(mAdapter);

	}

	/**
	 * This should be called when there is new data to load. ie. Push Notification or a tweet being posted.
	 */
	public void refreshTop()
	{
		TweetListMode mode = mAdapter.getTweetListMode();
		//TODO Build the tweetRequest and give it to loader
		JsonObject tweetRequest = mode.getPreviousTweetRequest();
		//TODO Build the request for the load
		mNewPageLoader.load(tweetRequest, mode.getApi());
	}

	public void launchPostTweetActivity()
	{
		Intent intent = new Intent(this.getActivity().getApplicationContext(),PostTweetActivity.class);
		this.startActivity(intent);
	}


	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



		//Set the Layout
		//		setContentView(R.layout.tweetlist);
		//		demoListView = (InfiniteScrollListView) this.findViewById(R.id.infinite_listview_infinitescrolllistview);
		//		handler = new Handler();
		//
		//		demoListView = (InfiniteScrollListView) this.findViewById(R.id.infinite_listview_infinitescrolllistview);
		//
		//		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//		demoListView.setLoadingView(layoutInflater.inflate(R.layout.loading_view_demo, null));
		//		demoListAdapter = new DemoListAdapter();
		//		PageLoader loader = new PageLoader(this, demoListAdapter, mClient);
		//		demoListAdapter.setPageListener(loader);
		//		
		//		
		//		demoListView.setAdapter(demoListAdapter);
		//		// Display a toast when a list item is clicked
		//		demoListView.setOnItemClickListener(new OnItemClickListener() {
		//			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		//				handler.post(new Runnable() {
		//					@Override
		//					public void run() {
		//						Toast.makeText(AllInOneActivity.this, demoListAdapter.getItem(position) + " " + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
		//					}
		//				});
		//			}
		//		});





		final View v = inflater.inflate(R.layout.tweetlist, container, false);


		mQuickReturnView = (LinearLayout) v.findViewById(R.id.footer);
		if(getArguments().getBoolean("hideFooter", false))
		{
			mQuickReturnView.setVisibility(View.GONE);

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
						TweetCommonData.mImageFetcher.setPauseWork(true);
					}
				} else {
					TweetCommonData.mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				//Start Loading tweets in the backing adapter
				loadDataOnScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);

				//End Loading tweets in the backing adapter

				//Loading the Quick return View - begin

				mScrollY = 0;



				mScrollY = mListView.getComputedScrollY();
				int diff = mScrollY - mMinRawY;
				boolean abrupt = Math.abs(mScrollY - mMinRawY) > 50;
				boolean currentSign = (mScrollY < mMinRawY) ;

				if(!abrupt)
				{
					switch (mState) {

					case STATE_ONSCREEN:
						if((diff > 0) )
						{
							mState = STATE_RETURNING;
							translationY = diff;
						}
						else
						{
							translationY = 0;
						}
						break;

					case STATE_OFFSCREEN:
						if(currentSign!=prevSign)
						{
							mState = STATE_RETURNING;
						}
						translationY = mQuickReturnHeight;
						break;


					case STATE_RETURNING:

						translationY += diff;

						if (translationY < 0) 
						{
							translationY = 0;
							mState = STATE_ONSCREEN;
						}


						if (translationY > mQuickReturnHeight) 
						{
							mState = STATE_OFFSCREEN;
						}
						break;
					}
					prevSign = currentSign;
				}

				mMinRawY = mScrollY;

				/** this can be used if the build is below honeycomb **/
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) 
				{
					anim = new TranslateAnimation(0, 0, translationY,
							translationY);
					anim.setFillAfter(true);
					anim.setDuration(0);
					mQuickReturnView.startAnimation(anim);
				} else {
					mQuickReturnView.setTranslationY(translationY);
				}

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
		TweetCommonData.mImageFetcher.setExitTasksEarly(false);
		
		boolean launchedFromNotification = this.getActivity().getIntent().getBooleanExtra(Constants.LAUNCHED_FROM_NOTIFICATIONS, false);
		if(launchedFromNotification)
		{
			refreshTop();
		}
	}

	@Override
	public void onPause() 
	{
		super.onPause();
		Log.v(TAG, "onPause");
		TweetCommonData.mImageFetcher.setPauseWork(false);
		TweetCommonData.mImageFetcher.setExitTasksEarly(true);
		TweetCommonData.mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		Log.v(TAG, "onDestroy ");
		TweetCommonData.mImageFetcher.closeCache();
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
			TweetListMode mode = mAdapter.getTweetListMode();
			JsonObject tweetRequest = mode.getNextTweetRequest();
			//TODO Build the request for the load
			mNewPageLoader.load(tweetRequest,mode.getApi());
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
				float velocityX, float velocityY) {
			mQuickReturnView.setTranslationY(mQuickReturnHeight);
			mState = STATE_OFFSCREEN;
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


	//TODO this has to be moved to a separate class
	private class PageLoader extends NewPageLoader
	{

		public static final int TWEET_LOAD_BUFFER = 10; //When the user is slowly scrolling throught he tweets, if the backing adapter has
		//fewer than TWEET_LOAD_BUFFER tweets to show, we start loading the next batch
		private static final int SEVER_SIDE_BATCH_SIZE = 10;
		private MobileServiceClient mClient;
		private boolean isLoadInProgress = false;

		public PageLoader()
		{
			mClient = TweetCommonData.mClient;
		}

		@Override
		public void load(final JsonObject tweetRequest, String api ) 
		{
			if(isLoadInProgress)
			{
				return;
			}
			
			final TweetAdapter adapter = (TweetAdapter)mListView.getAdapter();

			final TweetListMode tweetListMode = adapter.getTweetListMode();
			// Loading lock to allow only one instance of loading
			adapter.lock();


			//			JsonObject obj = new JsonObject();
			//			TweetListMode mode = getTweetListMode();

			//			if(mode == TweetListMode.HOME_FEED)
			//			{
			//				obj.addProperty(ApiInfo.kRequestingUserKey, mUserName);
			//				obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kHomeFeedTypeValue);
			//				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
			//				obj.addProperty(ApiInfo.kTweetRequestTypeKey, ApiInfo.kOldTweetRequest);
			//			}
			//			else if(mode == TweetListMode.USER_FEED)
			//			{
			//				obj.addProperty(ApiInfo.kRequestingUserKey, mUserName);
			//				obj.addProperty(ApiInfo.kFeedTypeKey, ApiInfo.kUserFeedTypeValue);
			//				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
			//			}
			//			else if(mode == TweetListMode.TRENDING_FEED)
			//			{
			//				obj.addProperty(ApiInfo.kTrendingTopicKey, mTrendTag);
			//				obj.addProperty(ApiInfo.kLastTweetIterator, getLastTweetIterator());
			//				api = ApiInfo.GET_TWEETS_FOR_TREND;
			//			}

			Log.d(TAG, "Trying to load the next set of tweets");
			isLoadInProgress = true;
			mClient.invokeApi(tweetListMode.getApi() , tweetRequest, new ApiJsonOperationCallback() {

				@Override
				public void onCompleted(JsonElement response, Exception arg1,
						ServiceFilterResponse arg2) {
					isLoadInProgress = false;
					if(arg1 == null)
					{



						//The teceived data contains an inner join of tweets and tweet users. 
						//Read them both.
						Gson gson = new Gson();

						Type collectionType = new TypeToken<List<Tweet>>(){}.getType();
						List<Tweet> list = gson.fromJson(response, collectionType);

						Type tweetusertype = new TypeToken<List<TweetUser>>(){}.getType();
						List<TweetUser> tweetUserlist = gson.fromJson(response, tweetusertype);



						// save index and top position
						int index = mListView.getFirstVisiblePosition();
						View v = mListView.getChildAt(0);
						int top = (v == null) ? 0 : v.getTop();

						int positionOfList = tweetListMode.processReceivedTweets(list,tweetUserlist,response,tweetRequest,index);
						adapter.notifyDataSetChanged();

						mListView.setSelectionFromTop(positionOfList, top);

						// Add or remove the loading view depend on if there might be more to load
						//TODO spinner at the bottom
						if (list.size() < SEVER_SIDE_BATCH_SIZE) 
						{
							mAdapter.notifyEndOfList();
						} else {
							mAdapter.notifyHasMore();
						}

						tweetUserLoader.load();

					}
					else
					{
						Log.e(TAG,"Exception fetching tweets received") ;
					}

				}
			},false);
		}	
	}


}
