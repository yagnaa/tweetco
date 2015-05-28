package com.tweetco.activities;

import java.lang.reflect.Type;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.games.multiplayer.Invitations.LoadInvitationsResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.onefortybytes.R;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.UiUtility;

public class TrendingFragment extends ListFragmentWithSwipeRefreshLayout
{
	
	public static class TrendingTag
	{
		String hashtag;
		String eventcount;
	}
		
	private String mUserName = null;
	
	private TrendingAdapter mAdapter = null;
	
	public TrendingFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		TrendingTag tag = (TrendingTag)l.getItemAtPosition(position);
		
		if(tag!=null && !TextUtils.isEmpty(tag.hashtag))
		{
			Intent intent = new Intent(getActivity(), TrendingFragmentActivity.class);
			intent.putExtra(Constants.TREND_TAG_STR, tag.hashtag);
			startActivity(intent);
		}
		
		
	}
	
	private void loadTrendingTags()
	{
		mSwipeRefreshLayout.post(new Runnable() {
			@Override public void run() {
			     mSwipeRefreshLayout.setRefreshing(true);
			}
			});
		
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		mClient.invokeApi(ApiInfo.TRENDING, obj, new ApiJsonOperationCallback() {
			
			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) 
			{
				mSwipeRefreshLayout.post(new Runnable() {
					@Override public void run() {
					     mSwipeRefreshLayout.setRefreshing(false);
					}
					});
				
				if(arg1 == null)
				{
					Gson gson = new Gson();
					
					Type collectionType = new TypeToken<List<TrendingTag>>(){}.getType();
					List<TrendingTag> trendingTagList = gson.fromJson(arg0, collectionType);
					
					if(trendingTagList!=null && !trendingTagList.isEmpty())
					{
						TweetCommonData.trendingTagLists.addAll(trendingTagList);
						mAdapter.clear();
						mAdapter.addAll(trendingTagList);
						mAdapter.notifyDataSetChanged();
					}
				}
				else
				{
					Log.e("Item clicked","Exception while loading Trending Tags") ;
					arg1.printStackTrace();
				}
				
				
				
			}
		},false);
	
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new TrendingAdapter(this.getActivity(), android.R.layout.simple_list_item_1);
		
		this.setListAdapter(mAdapter);
		
		setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				
				if(TweetCommonData.mClient != null)
				{
					loadTrendingTags();
				}
				else
				{
					Log.e("TrendingFragment", "MobileServiceClient is null");
					setRefreshing(false);
				}
			}
		});
		
		if(TweetCommonData.mClient != null)
		{
			loadTrendingTags();
		}
		else
		{
			Log.e("TrendingFragment", "MobileServiceClient is null");
		}
	}
	
 
	
	private class TrendingAdapter extends ArrayAdapter<TrendingTag>
	{
        Context mContext = null;
		public TrendingAdapter(Context context, int resource) 
		{
			super(context, resource);
			mContext = context;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			if(convertView == null)
			{
				LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
				convertView = inflator.inflate(R.layout.trending, parent, false);
			}
			
			TrendingTag tag = getItem(position);
			if(tag!=null && !TextUtils.isEmpty(tag.hashtag))
			{
				TextView trend = (TextView)convertView.findViewById(R.id.trend);
				trend.setText("#" + tag.hashtag);
			}
			return convertView;
		}	
	}
}
