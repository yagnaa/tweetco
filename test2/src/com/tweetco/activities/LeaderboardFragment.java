package com.tweetco.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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

import com.imagedisplay.util.AsyncTask;
import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.onefortybytes.R;
import com.tweetco.activities.adapter.LeaderboardAdapter;
import com.tweetco.activities.adapter.LeaderboardAdapter.OnProfilePicClick;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.asynctasks.GetLeaderboardTask;
import com.tweetco.asynctasks.GetLeaderboardTask.GetLeaderboardTaskCompletionCallback;
import com.tweetco.dao.LeaderboardUser;
import com.tweetco.dao.TweetUser;
import com.tweetco.datastore.AccountSingleton;
import com.tweetco.interfaces.OnChangeListener;
import com.tweetco.models.LeaderboardListModel;
import com.tweetco.tweets.TweetCommonData;

public class LeaderboardFragment extends ListFragmentWithSwipeRefreshLayout implements OnChangeListener<LeaderboardListModel>
{
	private LeaderboardAdapter mAdapter = null;
	private LeaderboardListModel model;
	ImageFetcher imageFetcher = null;


	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        imageFetcher = Utils.getImageFetcher(this.getActivity(), 60, 60);
		model = new LeaderboardListModel();
	}
	
	
	OnProfilePicClick onProfileClick =new OnProfilePicClick() {
		
		@Override
		public void onItemClick(int position) {
			//Show user profile view
			LeaderboardUser user = (LeaderboardUser)mAdapter.getItem(position);
    		if(user != null)
    		{
    			String owner = user.username;
    			if(!TextUtils.isEmpty(owner))
        		{
    				Intent intent = new Intent(LeaderboardFragment.this.getActivity(), UserProfileActivity.class);
					intent.putExtra(Constants.USERNAME_STR, owner);
					getActivity().startActivity(intent);
                }
    		}
    		
		}
	};
	
	@Override
	public void onResume() 
	{
		super.onResume();
		model.addListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		model.removeListener(this);
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) 
    {
        super.onActivityCreated(savedInstanceState);

		this.setListAdapter(null);

		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params) {
				try {
					model.loadLeaderboardUsersList();
				}
				catch (MalformedURLException e)
				{

				}

				return null;
			}

		}.execute();

		setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						try {
							model.refreshLeaderboardUsersFromServer();
						} catch (MalformedURLException e) {

						}

						return null;
					}

					@Override
					protected void onPostExecute(Void aVoid) {
						mSwipeRefreshLayout.setRefreshing(false);
					}

				}.execute();
			}
		});
	}

	@Override
	public void onChange(LeaderboardListModel model) {

		this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mAdapter == null) {
					mAdapter = new LeaderboardAdapter(LeaderboardFragment.this.getActivity(), R.layout.leaderview, imageFetcher, onProfileClick);

					LeaderboardFragment.this.setListAdapter(mAdapter);
				}

				mAdapter.notifyDataSetChanged();
			}
		});
	}
}
