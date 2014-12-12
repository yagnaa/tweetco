package com.yagnasri.displayingbitmaps.ui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.scrolllist.InfiniteScrollListView;
import com.example.test.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
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

    private final Context mContext;
    private int mItemHeight = 0;
    private int mNumColumns = 1;
    private int mActionBarHeight = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    private ImageFetcher mImageFetcher; //Fetches the images
    
    private NewPageLoader mNewPageLoader; //Fetches the tweets
    
    protected InfiniteScrollListPageListener mInfiniteListPageListener; 
    
	// A lock to prevent another scrolling event to be triggered if one is already in session
	protected boolean canScroll = false;
	// A flag to enable/disable row clicks
	protected boolean rowEnabled = true;
    
    //All the tweets that we are currently holding in memory
    private List<Tweet> tweetsList = new ArrayList<Tweet>();
    
	public void lock() {
		canScroll = false;
	}	
	public void unlock() {
		canScroll = true;
	}
	
    @Override
    public boolean isEnabled(int position) {
    	return rowEnabled; 
    }
	public void setRowEnabled(boolean rowEabled) {
		this.rowEnabled = rowEabled;
	}


    public TweetAdapter(Context context,ImageFetcher imageFetcher) {
        super();
        mContext = context;
        mImageFetcher = imageFetcher;
        mImageViewLayoutParams = new ListView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
        }
        mNewPageLoader = new PageLoader(context, this);
    }
    
	public void addEntriesToTop(List<Tweet> entries) {
		// Add entries in reversed order to achieve a sequence used in most of messaging/chat apps
		if (entries != null) {
			Collections.reverse(entries);
		}
		// Add entries to the top of the list
		this.tweetsList.addAll(0, entries);
		notifyDataSetChanged();
	}
	
	public void addEntriesToBottom(List<Tweet> entries) {
		// Add entries to the bottom of the list
		this.tweetsList.addAll(entries);
		notifyDataSetChanged();
	}
	
	public void clearEntries() {
		// Clear all the data points
		this.tweetsList.clear();
		notifyDataSetChanged();
	}

    @Override
    public int getCount() {
        // If columns have yet to be determined, return no items
//        if (getNumColumns() == 0) {
//            return 0;
//        }

        // Size + number of columns for top empty row
        return tweetsList.size() + mNumColumns;
    }

    @Override
    public Object getItem(int position) {
        return position < mNumColumns ?
                null : tweetsList.get(position - mNumColumns);
    }

    @Override
    public long getItemId(int position) {
        return position < mNumColumns ? 0 : position - mNumColumns;
    }

    @Override
    public int getViewTypeCount() {
        // Two types of views, the normal ImageView and the top row of empty views
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < mNumColumns) ? 1 : 0;
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
		
		
		
        //BEGIN_INCLUDE(load_gridview_item)
        // First check if this is the top row
        if (position < mNumColumns) {
            if (convertView == null) {
                convertView = new View(mContext);
            }
            // Set empty view with height of ActionBar
            convertView.setLayoutParams(new AbsListView.LayoutParams(
                    LayoutParams.MATCH_PARENT, mActionBarHeight));
            return convertView;
        }

        // Now handle the main ImageView thumbnails
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, instantiate and initialize
        	
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.tweet, null);
        	
        	
            imageView = (ImageView)convertView.findViewById(R.id.imageView1);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(mImageViewLayoutParams);
        } else { // Otherwise re-use the converted view
        	imageView = (ImageView)convertView.findViewById(R.id.imageView1);
        }
        
        //Load TextFields here
		Tweet tweet = (Tweet) getItem(position);
		if (tweet != null) {
			
			TextView handle = (TextView) convertView.findViewById(R.id.textView1);
			TextView userName = (TextView) convertView.findViewById(R.id.textView2);
			TextView tweetContent = (TextView) convertView.findViewById(R.id.textView3);
			
			handle.setText(tweet.tweetowner);
			userName.setText(tweet.tweetowner);
			tweetContent.setText(tweet.tweetcontent);		
		}

        // Check the height matches our calculated column width
        if (imageView.getLayoutParams().height != mItemHeight) {
            imageView.setLayoutParams(mImageViewLayoutParams);
        }

        // Finally load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
        mImageFetcher.loadImage(tweetsList.get(position - mNumColumns).imageurl, imageView);
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
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLayoutParams =
                new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        mImageFetcher.setImageSize(height);
        notifyDataSetChanged();
    }

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
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
		if (view instanceof InfiniteScrollListView) {

			// In scroll-to-bottom-to-load mode, when the sum of first visible position and visible count equals the total number
			// of items in the adapter it reaches the bottom
			if (firstVisibleItem + visibleItemCount - 1 == getCount() && canScroll) {
				onScrollNext();
			}
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
		
		public PageLoader(Context context,TweetAdapter tweetAdapter)
		{
			mContext = context;
			this.tweetAdapter =  tweetAdapter;
			mClient = AllInOneActivity.mClient;
		}

			@Override
			public void onScrollNext() {
				
				// Loading lock to allow only one instance of loading
				tweetAdapter.lock();
				
				List<Tweet> result = null;
				
	    		JsonObject obj = new JsonObject();
	    		obj.addProperty("requestingUser", "tweetbot");
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

							tweetAdapter.addEntriesToBottom(list);
							
							// Add or remove the loading view depend on if there might be more to load
							if (list.size() < SEVER_SIDE_BATCH_SIZE) {
								tweetAdapter.notifyEndOfList();
							} else {
								tweetAdapter.notifyHasMore();
							}
							
						}
						else
						{
							Log.e("Item clicked","Exception fetching tweets received") ;
						}
						
					}
				});
				
				
//				new AsyncTask<Void, Void, List<Tweet>>() {
//					@Override
//					protected void onPreExecute() {
//						// Loading lock to allow only one instance of loading
//						mDemoListAdapter.lock();
//					}
//					@Override
//					protected List<Tweet> doInBackground(Void ... params) {
//						List<Tweet> result = null;
//						
//		        		JsonObject obj = new JsonObject();
//		        		obj.addProperty("requestingUser", "tweetbot");
//		        		obj.addProperty("feedtype", "homefeed");
//		        		mClient.invokeApi("gettweetsforuser", obj, new ApiJsonOperationCallback() {
//							
//							@Override
//							public void onCompleted(JsonElement arg0, Exception arg1,
//									ServiceFilterResponse arg2) {
//								if(arg1 == null)
//								{
//									Gson gson = new Gson();
//									Tweet[] tweetArray = gson.fromJson(arg0, Tweet[].class);
//							      Log.e("Item clicked","Json received") ;
//								}
//								else
//								{
//									Log.e("Item clicked","Json received") ;
//								}
//								
//								
//								
//							}
//						});
	//
//						return result;
//					}
//					@Override
//					protected void onPostExecute(List<Tweet> result) {
//						if (isCancelled() || result == null || result.isEmpty()) {
//							mDemoListAdapter.notifyEndOfList();
//						} else {
//							// Add data to the placeholder
	//
//							mDemoListAdapter.addEntriesToBottom(result);
//							
//							// Add or remove the loading view depend on if there might be more to load
//							if (result.size() < SEVER_SIDE_BATCH_SIZE) {
//								mDemoListAdapter.notifyEndOfList();
//							} else {
//								mDemoListAdapter.notifyHasMore();
//							}
//						}
//					};
//					@Override
//					protected void onCancelled() {
//						// Tell the adapter it is end of the list when task is cancelled
//						mDemoListAdapter.notifyEndOfList();
//					}
//				}.execute();
			}
			
			

		
		
	}
}
