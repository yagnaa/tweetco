package com.tweetco.activities;

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

import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.onefortybytes.R;
import com.tweetco.activities.adapter.LeaderboardAdapter;
import com.tweetco.activities.adapter.LeaderboardAdapter.OnProfilePicClick;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.asynctasks.GetLeaderboardTask;
import com.tweetco.asynctasks.GetLeaderboardTask.GetLeaderboardTaskCompletionCallback;
import com.tweetco.dao.LeaderboardUser;
import com.tweetco.tweets.TweetCommonData;

public class LeaderboardFragment extends ListFragmentWithSwipeRefreshLayout 
{
	private AsyncTaskEventHandler asyncTaskEventHandler = null;
	private LeaderboardAdapter mAdapter = null;
	ImageFetcher imageFetcher = null;
	public LeaderboardFragment()
	{
		
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        imageFetcher = Utils.getImageFetcher(this.getActivity(), 60, 60);
		mAdapter = new LeaderboardAdapter(LeaderboardFragment.this.getActivity(), R.layout.leaderview, imageFetcher,onProfileClick);
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
//        			UserProfileFragment fragment = new UserProfileFragment();
//        			Bundle bundle = new Bundle();
//        			bundle.putString("username", owner);
//        			fragment.setArguments(bundle);
//                    final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                    ft.add(android.R.id.content, fragment, TAG);
//                    ft.commit();
    				
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
		mAdapter.notifyDataSetChanged();
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) 
    {
        super.onActivityCreated(savedInstanceState);
		
		asyncTaskEventHandler = new AsyncTaskEventHandler(this.getActivity(), "Getting Leaderboard");
		this.setListAdapter(mAdapter);
		
		setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				loadLeaderBoard();				
			}
		});
		
		loadLeaderBoard();
	}
    
    public void loadLeaderBoard()
    {		
    	mSwipeRefreshLayout.post(new Runnable() {
			@Override public void run() {
			     mSwipeRefreshLayout.setRefreshing(true);
			}
			});
    	
		new GetLeaderboardTask(this.getActivity(), null, new GetLeaderboardTaskCompletionCallback() 
		{	
			@Override
			public void onGetLeaderboardTaskSuccess(List<LeaderboardUser> users) 
			{
				if(users!=null && !users.isEmpty())
				{
					mAdapter.clear();
					mAdapter.addAll(users);
					mAdapter.notifyDataSetChanged();
				}
				
				mSwipeRefreshLayout.post(new Runnable() {
					@Override public void run() {
					     mSwipeRefreshLayout.setRefreshing(false);
					}
					});
			}
			
			@Override
			public void onGetLeaderboardTaskFailure() 
			{
				Log.e("LeaderboardTask", "Failed");
				mSwipeRefreshLayout.post(new Runnable() {
					@Override public void run() {
					     mSwipeRefreshLayout.setRefreshing(false);
					}
					});
			}
			
			@Override
			public void onGetLeaderboardTaskCancelled() 
			{
				mSwipeRefreshLayout.post(new Runnable() {
					@Override public void run() {
					     mSwipeRefreshLayout.setRefreshing(false);
					}
					});
			}
		}).execute();
    }

}
