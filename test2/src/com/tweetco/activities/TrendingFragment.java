package com.tweetco.activities;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.tweetco.R;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.AllInOneActivity;
import com.yagnasri.displayingbitmaps.ui.ApiInfo;
import com.yagnasri.displayingbitmaps.ui.Tweet;
import com.yagnasri.displayingbitmaps.ui.TweetUtils;
import com.yagnasri.displayingbitmaps.util.AsyncTask;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;

public class TrendingFragment extends ListFragment
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

		loadTrendingTags();
		
	}
	
	
	private void loadTrendingTags()
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		mClient.invokeApi(ApiInfo.TRENDING, obj, new ApiJsonOperationCallback() {
			
			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) {
				if(arg1 == null)
				{
					Gson gson = new Gson();
					
					Type collectionType = new TypeToken<List<TrendingTag>>(){}.getType();
					List<TrendingTag> trendingTagList = gson.fromJson(arg0, collectionType);
					
					if(trendingTagList!=null && !trendingTagList.isEmpty())
					{
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
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mAdapter = new TrendingAdapter(this.getActivity(), android.R.layout.simple_list_item_1);
		
		this.setListAdapter(mAdapter);
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
