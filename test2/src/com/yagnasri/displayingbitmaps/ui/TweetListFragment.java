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


import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.LinearLayout;

import com.tweetco.R;
import com.tweetco.activities.PostTweetActivity;
import com.tweetco.activities.QuickReturnListView;
import com.tweetco.activities.UserProfileFragment;
import com.tweetco.tweets.TweetCommonData;
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
	public static final String USERNAME = "username";//Constant to be used in intent for username
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private TweetAdapter mAdapter;

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



	private String mUsername;


	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public TweetListFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		mUsername = getArguments().getString("username");


		ImageCache.ImageCacheParams cacheParams =
				new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		mDetector = new GestureDetectorCompat(this.getActivity().getApplicationContext(), new MyGestureListener());

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		TweetCommonData.mImageFetcher = new ImageFetcher(getActivity(), 60,60, true);
		TweetCommonData.mImageFetcher.setLoadingImage(R.drawable.ic_launcher);
		TweetCommonData.mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
		mAdapter = new TweetAdapter(getActivity(), mUsername, TweetCommonData.mImageFetcher, new OnProfilePicClick() {

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
						intent.putExtra("username", owner);
						getActivity().startActivity(intent);
					}
				}

			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
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
		TweetCommonData.tweetsList.clear();
		mAdapter.onScrollNext();
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
				mAdapter.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);

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
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@TargetApi(VERSION_CODES.JELLY_BEAN)
					@Override
					public void onGlobalLayout() 
					{
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
		TweetCommonData.mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	public void onPause() 
	{
		super.onPause();
		TweetCommonData.mImageFetcher.setPauseWork(false);
		TweetCommonData.mImageFetcher.setExitTasksEarly(true);
		TweetCommonData.mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		TweetCommonData.mImageFetcher.closeCache();
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


	public  class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		private static final String DEBUG_TAG = "Gestures"; 

		@Override
		public boolean onDown(MotionEvent event) { 
			Log.d(DEBUG_TAG,"onDown: " + event.toString()); 
			return true;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, 
				float velocityX, float velocityY) {
			mQuickReturnView.setTranslationY(mQuickReturnHeight);
			mState = STATE_OFFSCREEN;
			Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent event1, MotionEvent event2,
				float distanceX, float distanceY) {
			Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
			return false;
		}
	}


}
