package com.yagnasri.displayingbitmaps.ui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.R;
import com.tweetco.TweetCo;
import com.tweetco.activities.UserProfileFragment;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;



/**
 * The main adapter that backs the GridView. This is fairly standard except the number of
 * columns in the GridView is used to create a fake top row of empty views as we use a
 * transparent ActionBar and don't want the real top row of images to start off covered by it.
 */
public class TweetAdapter extends BaseAdapter implements OnScrollListener {
	
	// A demo listener to pass actions from view to adapter
	public static abstract class NewPageLoader {
		public abstract void onScrollNext();
	}
	
	public interface OnProfilePicClick
	{
		void onItemClick(int position);
	}

    private final Context mContext;
    private int mItemHeight = 0;
    private int mActionBarHeight = 0;
    private ImageFetcher mImageFetcher; //Fetches the images
    public String mUsername;
    
    private NewPageLoader mNewPageLoader; //Fetches the tweets
    
    private TweetUserLoader tweetUserLoader; //Loads user data
    
    private OnProfilePicClick mOnProfilePicClickCallback;
    
    protected InfiniteScrollListPageListener mInfiniteListPageListener; 
    
	// A lock to prevent another scrolling event to be triggered if one is already in session
	protected boolean canScroll = false;
	// A flag to enable/disable row clicks
	protected boolean rowEnabled = true;
    
    public void lock() {
		canScroll = false;
	}	
	public void unlock() {
		canScroll = true;
	}
	
    public TweetAdapter(Context context, String username, boolean bGetTweetsByUser, ImageFetcher imageFetcher, OnProfilePicClick onProfilePicClickCallback) 
    {
        super();
        mContext = context;
        mImageFetcher = imageFetcher;
        mUsername = username;
        mOnProfilePicClickCallback = onProfilePicClickCallback;
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
        }
        mNewPageLoader = new PageLoader(context, this, bGetTweetsByUser);
        tweetUserLoader = new TweetUserLoader(this, mUsername);
        
        TweetCommonData.tweetsMap.get(mUsername).clear();
    }
    
	public void addEntriesToTop(List<Tweet> entries) {
		// Add entries in reversed order to achieve a sequence used in most of messaging/chat apps
		if (entries != null) {
			Collections.reverse(entries);
		}
		// Add entries to the top of the list
		TweetCommonData.tweetsMap.get(mUsername).addAll(0, entries);
		notifyDataSetChanged();
	}
	
	public void addEntriesToBottom(List<Tweet> entries) {
		// Add entries to the bottom of the list
		TweetCommonData.tweetsMap.get(mUsername).addAll(entries);
		notifyDataSetChanged();
	}
	
	public void clearEntries() {
		// Clear all the data points
		TweetCommonData.tweetsMap.get(mUsername).clear();
		notifyDataSetChanged();
	}
	
	public void addUsers(Map<String,TweetUser> tweetUsers) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.putAll(tweetUsers);
		notifyDataSetChanged();
	}
	
	public void addUser(String user,TweetUser userInfo) 
	{
		// Clear all the data points
		TweetCommonData.tweetUsers.put(user, userInfo);
	}

    @Override
    public int getCount() {
        // If columns have yet to be determined, return no items
//        if (getNumColumns() == 0) {
//            return 0;
//        }

        // Size + number of columns for top empty row
    	return TweetCommonData.tweetsMap.get(mUsername).size();
    }

    @Override
    public Object getItem(int position) {
        return TweetCommonData.tweetsMap.get(mUsername).get(position);
    }

    @Override
    public long getItemId(int position) {
        return position < 0 ? 0 : position;
    }

    @Override
    public int getViewTypeCount() {
        // Two types of views, the normal ImageView and the top row of empty views
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
    	
    	
    	
    	
//		// Customize the row for list view
//		if(convertView == null) {
//			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			convertView = layoutInflater.inflate(R.layout.tweet, null);
//		}
//		Tweet tweet = (Tweet) tweetAdapter.getItem(position);
//		if (tweet != null) {
//			
//			TextView handle = (TextView) convertView.findViewById(R.id.textView1);
//			ImageView rowPhoto = (ImageView) convertView.findViewById(R.id.imageView1);
//			TextView userName = (TextView) convertView.findViewById(R.id.textView2);
//			TextView tweetContent = (TextView) convertView.findViewById(R.id.textView3);
//			
//			handle.setText(tweet.tweetowner);
//			handle.setText(tweet.tweetowner);
//			handle.setText(tweet.tweetcontent);
//			rowPhoto.setImageResource(R.drawable.ic_launcher);
//
//			
//		}
//		return convertView;
		


        // Now handle the main ImageView thumbnails
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, instantiate and initialize
        	
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.tweet, null);
        	
        	
            imageView = (ImageView)convertView.findViewById(R.id.imageView1);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else { // Otherwise re-use the converted view
        	imageView = (ImageView)convertView.findViewById(R.id.imageView1);
        }
        
        imageView.setTag(position);
        imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int position = Integer.parseInt(v.getTag().toString());
        		mOnProfilePicClickCallback.onItemClick(position);
			}
		});
        //Load TextFields here
		Tweet tweet = (Tweet) getItem(position);
		TweetUser tweeter = null;
		if (tweet != null) {
			tweeter = (TweetUser) TweetCommonData.tweetUsers.get(tweet.tweetowner);
			TextView handle = (TextView) convertView.findViewById(R.id.handle);
			TextView userName = (TextView) convertView.findViewById(R.id.username);
			TextView tweetContent = (TextView) convertView.findViewById(R.id.tweetcontent);
			
			handle.setText(tweet.tweetowner);
			userName.setText(tweet.tweetowner);
			tweetContent.setText(tweet.tweetcontent);		
		}

//        // Check the height matches our calculated column width
//        if (imageView.getLayoutParams().height != mItemHeight) {
//            imageView.setLayoutParams(mImageViewLayoutParams);
//        }

        // Finally load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
		if(tweeter!=null)
		{
			mImageFetcher.loadImage(tweeter.profileimageurl, imageView);
		}
        return convertView;
        //END_INCLUDE(load_gridview_item)
    }

    /**
     * Sets the item height. Useful for when we know the column width so the height can be set
     * to match.
     *
     * @param height
     */
    public void setItemHeight(int height) {
//        if (height == mItemHeight) {
//            return;
//        }
//        mItemHeight = height;
//        mImageViewLayoutParams =
//                new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
//        mImageFetcher.setImageSize(height);
        notifyDataSetChanged();
    }
    
    //This notifies that the list has ended
	public void notifyEndOfList() {
		// When there is no more to load use the lock to prevent loading from happening
		lock();
		// More actions when there is no more to load
		if (mInfiniteListPageListener != null) {
			mInfiniteListPageListener.endOfList();
		}
	}

	//This notifies that there are more tweets to be loaded
	public void notifyHasMore() {
		// Release the lock when there might be more to load
		unlock();
		// More actions when it might have more to load
		if (mInfiniteListPageListener != null) {
			mInfiniteListPageListener.hasMore();
		}
	}
	
	/**
	 * This function decides when to load the next set of tweets.
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


			// In scroll-to-bottom-to-load mode, when the sum of first visible position and visible count equals the total number
			// of items in the adapter it reaches the bottom
			if (firstVisibleItem + visibleItemCount - 1 == getCount() && canScroll) {
				onScrollNext();
			}
		
	}
	
	
	public void onScrollNext() {
		if (mNewPageLoader != null) {
			mNewPageLoader.onScrollNext();
		}
	}
	
	public void setInfiniteListPageListener(InfiniteScrollListPageListener infiniteListPageListener) {
		this.mInfiniteListPageListener = infiniteListPageListener;
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	
	
	//TODO this has to be moved to a separate class
	public static class PageLoader extends NewPageLoader
	{
		private static final int SEVER_SIDE_BATCH_SIZE = 10;
		Context mContext = null;
		private MobileServiceClient mClient;
		TweetAdapter tweetAdapter;
		private boolean mbGetTweetsByUser = false;
		
		public PageLoader(Context context,TweetAdapter tweetAdapter, boolean bGetTweetsByUser)
		{
			mContext = context;
			this.tweetAdapter =  tweetAdapter;
			mClient = AllInOneActivity.mClient;
			mbGetTweetsByUser = bGetTweetsByUser;
		}

			@Override
			public void onScrollNext() {
				
				// Loading lock to allow only one instance of loading
				tweetAdapter.lock();
				
				List<Tweet> result = null;
				
	    		JsonObject obj = new JsonObject();
	    		obj.addProperty("requestingUser", tweetAdapter.mUsername);
	    		obj.addProperty("feedtype", "homefeed");
	    		mClient.invokeApi("gettweetsforuser", obj, new ApiJsonOperationCallback() {
					
					@Override
					public void onCompleted(JsonElement arg0, Exception arg1,
							ServiceFilterResponse arg2) {
						if(arg1 == null)
						{
							Gson gson = new Gson();
							
							Type collectionType = new TypeToken<List<Tweet>>(){}.getType();
							List<Tweet> list = gson.fromJson(arg0, collectionType);
							List<Tweet> removeTweetsList = new ArrayList<Tweet>();
							if(mbGetTweetsByUser)
							{
								for (Tweet tweet : list) 
								{
									if(!tweet.tweetowner.equalsIgnoreCase(tweetAdapter.mUsername))
									{
										removeTweetsList.add(tweet);
									}
								}
							}
							
							if(!removeTweetsList.isEmpty())
							{
								list.removeAll(removeTweetsList);
							}

							tweetAdapter.addEntriesToBottom(list);
							
							// Add or remove the loading view depend on if there might be more to load
							if (list.size() < SEVER_SIDE_BATCH_SIZE) {
								tweetAdapter.notifyEndOfList();
							} else {
								tweetAdapter.notifyHasMore();
							}
							
							tweetAdapter.tweetUserLoader.load();
							
						}
						else
						{
							Log.e("Item clicked","Exception fetching tweets received") ;
						}
						
					}
				},false);
			}	
	}
}
