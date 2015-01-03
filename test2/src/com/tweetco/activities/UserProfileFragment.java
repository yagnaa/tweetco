package com.tweetco.activities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tweetco.BuildConfig;
import com.tweetco.R;
import com.tweetco.tweetlist.UserFeedMode;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.UiUtility;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.CustomUserProfileFragmentPagerAdapter;
import com.yagnasri.displayingbitmaps.ui.TweetListFragment;
import com.yagnasri.displayingbitmaps.util.AsyncTask;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;
import com.yagnasri.displayingbitmaps.util.RecyclingBitmapDrawable;
import com.yagnasri.displayingbitmaps.util.Utils;

public class UserProfileFragment extends FragmentActivity 
{
	private static final String TAG = "UserProfileFragment";
	private static final int EDIT_PROFILE_REQUEST = 100;
	private String mUserName = null;
	private static final String HTTP_CACHE_DIR = "http";
	private static final int IO_BUFFER_SIZE = 8 * 1024;
	
	private View mBackgroundImage = null;
	private ImageView mUserProfilePic = null;
	private TextView mUserProfileDisplayName = null;
	private TextView mUserProfileHandleName = null;
	private TextView mFollowerCount = null;
	private TextView mFolloweeCount = null;
	private Button mEditProfileButton = null;
	
	private ViewPager mViewPager;
	private static CustomUserProfileFragmentPagerAdapter mPagerAdapter = null;
	
	/**
     * Empty constructor as per the Fragment documentation
     */
    public UserProfileFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.userprofilefragment);
       
        mUserName = getIntent().getExtras().getString(Constants.USERNAME_STR);
        TweetUser user = TweetCommonData.tweetUsers.get(mUserName.toLowerCase());
    	if(user != null)
    	{
    		mBackgroundImage = UiUtility.getView(this, R.id.backgroundImage);
    		mUserProfilePic = UiUtility.getView(this, R.id.userProfilePic);
    		mUserProfileDisplayName = UiUtility.getView(this, R.id.userProfileDisplayName);
    		mUserProfileHandleName = UiUtility.getView(this, R.id.userProfileHandle);
    		mFolloweeCount = UiUtility.getView(this, R.id.followeesCount);
    		mFollowerCount = UiUtility.getView(this, R.id.followersCount);
    		
    		ImageFetcher imageFectcher = Utils.getImageFetcher(this, 50, 50);
    		imageFectcher.loadImage(user.profileimageurl, mUserProfilePic);
    		
    		BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(user.profilebgurl, mBackgroundImage, this);
    		bitmapWorkerTask.execute();
    		
    		mUserProfileDisplayName.setText((TextUtils.isEmpty(user.displayname))?mUserName:user.displayname);
    		mUserProfileHandleName.setText("@"+mUserName);
    		int followeesCount = 0;
    		if(!TextUtils.isEmpty(user.followees))
    		{
    			followeesCount = user.followees.split(";").length - 1;
    		}
    		mFolloweeCount.setText(String.valueOf(followeesCount));
    		
    		int followersCount = 0;
    		if(!TextUtils.isEmpty(user.followers))
    		{
    			followersCount = user.followers.split(";").length - 1;
    		}
    		mFollowerCount.setText(String.valueOf(followersCount));
    		
    		if(mUserName.equals((TweetCommonData.getUserName())))
    		{
    			mEditProfileButton = UiUtility.getView(this, R.id.editProfileButton);
    			mEditProfileButton.setVisibility(View.VISIBLE);
    			mEditProfileButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) 
					{
						startActivityForResult(new Intent(getApplicationContext(), EditProfileActivity.class),EDIT_PROFILE_REQUEST);
					}
				});
    		}
    		
    		if(mUserName.equals(TweetCommonData.getUserName()))
    		{
    			initializePager();
    		}
    		else
    		{
    			FrameLayout layout = UiUtility.getView(this, R.id.tweetsListFragmentContainer);
    			layout.setVisibility(View.VISIBLE);
    			
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                TweetListFragment tweetListFragment = new TweetListFragment();
                Bundle bundle = new Bundle();
                UserFeedMode mode = new UserFeedMode(mUserName);
                bundle.putParcelable(Constants.TWEET_LIST_MODE, mode);
                bundle.putBoolean("hideFooter", true);
                tweetListFragment.setArguments(bundle);
                ft.replace(R.id.tweetsListFragmentContainer, tweetListFragment);
                ft.commit();
    		}
    		
    		
    	}
    	
    	
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
		mViewPager = (ViewPager) findViewById(R.id.userProfilePager);
		mViewPager.setVisibility(View.VISIBLE);
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
		
		mPagerAdapter = new CustomUserProfileFragmentPagerAdapter(this.getApplicationContext(), getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
		mViewPager.setCurrentItem(0);
	}
    
    
    private static class ImageDownloadAsycnTask extends AsyncTask<Void, Void, BitmapDrawable>
    {

		@Override
		protected BitmapDrawable doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }
    
    private class BitmapWorkerTask extends AsyncTask<Void, Void, BitmapDrawable> 
    {
        private String mUrl;
        private final WeakReference<View> imageViewReference;
        Resources mResources = null;

        public BitmapWorkerTask(String url, View imageView, Activity activity) 
        {
        	mUrl = url;
            mResources = activity.getResources();
            imageViewReference = new WeakReference<View>(imageView);
        }

        /**
         * Background processing.
         */
        @Override
        
        protected BitmapDrawable doInBackground(Void... params) {

            Bitmap bitmap = null;
            BitmapDrawable drawable = null;


            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            View imageView = imageViewReference.get();
            if (bitmap == null && !isCancelled() && imageView != null) 
            {

        		HttpURLConnection urlConnection = null;
        		BufferedInputStream in = null;

        		try {
        			final URL url = new URL(mUrl);
        			urlConnection = (HttpURLConnection) url.openConnection();
        			in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);

        				byte[] bytes = IOUtils.toByteArray(in);
        				String s = new String(bytes);
        				byte[] decodeByteArray = Base64.decode(s, Base64.DEFAULT);
        				bitmap = BitmapFactory.decodeByteArray(decodeByteArray, 0, decodeByteArray.length);

        		} catch (final IOException e) 
        		{
        			Log.e(TAG, "Error in downloadBitmap - " + e);
        		} finally {
        			if (urlConnection != null) {
        				urlConnection.disconnect();
        			}
        			try {
        				if (in != null) {
        					in.close();
        				}
        			} catch (final IOException e) {}
        		}
            }

            if (bitmap != null) 
            {
                if (Utils.hasHoneycomb()) {
                    // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
                    drawable = new BitmapDrawable(mResources, bitmap);
                } else {
                    // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
                    // which will recycle automagically
                    drawable = new RecyclingBitmapDrawable(mResources, bitmap);
                }
            }

            return drawable;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) 
        {
            if (isCancelled()) 
            {
                value = null;
            }
            
            View imageView = imageViewReference.get();
            if (value != null && imageView != null) 
            {
                if (BuildConfig.DEBUG) 
                {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                imageView.setBackground(value);
            }
        }
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		 Uri mProfilePicUri;
		 Uri mHeaderPicUri;
		
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == EDIT_PROFILE_REQUEST || resultCode == RESULT_OK)
			{
				mProfilePicUri = (Uri) intent.getParcelableExtra(Constants.PROFILE_PIC_URI);
				mHeaderPicUri = (Uri) intent.getParcelableExtra(Constants.PROFILE_BG_PIC_URI);
				
				if(mProfilePicUri!=null)
				{
	    	        Bitmap bitmap;
					try {
						bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mProfilePicUri);
						mUserProfilePic.setBackground( new BitmapDrawable(this.getResources(), bitmap));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	    		
	    		if(mHeaderPicUri!=null)
	    		{
	    	        Bitmap bitmap;
					try {
						bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mHeaderPicUri);
		    	        mBackgroundImage.setBackground( new BitmapDrawable(this.getResources(), bitmap));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
			}
		}
	}

}
