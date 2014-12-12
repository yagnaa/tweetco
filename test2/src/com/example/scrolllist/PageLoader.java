package com.example.scrolllist;
import java.lang.reflect.Type;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.yagnasri.displayingbitmaps.ui.Tweet;
import com.yagnasri.displayingbitmaps.ui.TweetAdapter.NewPageLoader;



public class PageLoader extends NewPageLoader
{
	private static final int SEVER_SIDE_BATCH_SIZE = 10;
	DemoListAdapter mDemoListAdapter = null;
	Context mContext = null;
	private MobileServiceClient mClient;
	
	public PageLoader(Context context,DemoListAdapter demoListAdapter,MobileServiceClient client)
	{
		mDemoListAdapter = demoListAdapter;
		mContext = context;
		mClient = client;
	}

		@Override
		public void onScrollNext() {
			
			// Loading lock to allow only one instance of loading
			mDemoListAdapter.lock();
			
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

						mDemoListAdapter.addEntriesToBottom(list);
						
						// Add or remove the loading view depend on if there might be more to load
						if (list.size() < SEVER_SIDE_BATCH_SIZE) {
							mDemoListAdapter.notifyEndOfList();
						} else {
							mDemoListAdapter.notifyHasMore();
						}
						
					}
					else
					{
						Log.e("Item clicked","Exception fetching tweets received") ;
					}
					
				}
			});
			
			
//			new AsyncTask<Void, Void, List<Tweet>>() {
//				@Override
//				protected void onPreExecute() {
//					// Loading lock to allow only one instance of loading
//					mDemoListAdapter.lock();
//				}
//				@Override
//				protected List<Tweet> doInBackground(Void ... params) {
//					List<Tweet> result = null;
//					
//	        		JsonObject obj = new JsonObject();
//	        		obj.addProperty("requestingUser", "tweetbot");
//	        		obj.addProperty("feedtype", "homefeed");
//	        		mClient.invokeApi("gettweetsforuser", obj, new ApiJsonOperationCallback() {
//						
//						@Override
//						public void onCompleted(JsonElement arg0, Exception arg1,
//								ServiceFilterResponse arg2) {
//							if(arg1 == null)
//							{
//								Gson gson = new Gson();
//								Tweet[] tweetArray = gson.fromJson(arg0, Tweet[].class);
//						      Log.e("Item clicked","Json received") ;
//							}
//							else
//							{
//								Log.e("Item clicked","Json received") ;
//							}
//							
//							
//							
//						}
//					});
//
//					return result;
//				}
//				@Override
//				protected void onPostExecute(List<Tweet> result) {
//					if (isCancelled() || result == null || result.isEmpty()) {
//						mDemoListAdapter.notifyEndOfList();
//					} else {
//						// Add data to the placeholder
//
//						mDemoListAdapter.addEntriesToBottom(result);
//						
//						// Add or remove the loading view depend on if there might be more to load
//						if (result.size() < SEVER_SIDE_BATCH_SIZE) {
//							mDemoListAdapter.notifyEndOfList();
//						} else {
//							mDemoListAdapter.notifyHasMore();
//						}
//					}
//				};
//				@Override
//				protected void onCancelled() {
//					// Tell the adapter it is end of the list when task is cancelled
//					mDemoListAdapter.notifyEndOfList();
//				}
//			}.execute();
		}
		
		
//		@Override
//		public View getInfiniteScrollListView(int position, View convertView, ViewGroup parent) {
//			// Customize the row for list view
//			if(convertView == null) {
//				LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				convertView = layoutInflater.inflate(R.layout.tweet, null);
//			}
//			Tweet tweet = (Tweet) mDemoListAdapter.getItem(position);
//			if (tweet != null) {
//				
//				TextView handle = (TextView) convertView.findViewById(R.id.textView1);
//				ImageView rowPhoto = (ImageView) convertView.findViewById(R.id.imageView1);
//				TextView userName = (TextView) convertView.findViewById(R.id.textView2);
//				TextView tweetContent = (TextView) convertView.findViewById(R.id.textView3);
//				
//				handle.setText(tweet.tweetowner);
//				handle.setText(tweet.tweetowner);
//				handle.setText(tweet.tweetcontent);
//				rowPhoto.setImageResource(R.drawable.ic_launcher);
//	
//				
//			}
//			return convertView;
//		}
	
	
}
