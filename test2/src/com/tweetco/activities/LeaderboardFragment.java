package com.tweetco.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tweetco.R;
import com.tweetco.activities.adapter.LeaderboardAdapter;
import com.tweetco.activities.adapter.LeaderboardAdapter.OnProfilePicClick;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.asynctasks.GetLeaderboardTask;
import com.tweetco.asynctasks.GetLeaderboardTask.GetLeaderboardTaskCompletionCallback;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.UiUtility;
import com.yagnasri.dao.LeaderboardUser;

public class LeaderboardFragment extends Fragment 
{
	ListView mListView = null;
	AsyncTaskEventHandler asyncTaskEventHandler = null;
	private LeaderboardAdapter mAdapter = null;
	
	public LeaderboardFragment()
	{
		
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		
		mListView = UiUtility.getView(this, R.id.leaderboardList);
		asyncTaskEventHandler = new AsyncTaskEventHandler(this.getActivity(), "Getting Leaderboard");
		
		new GetLeaderboardTask(this.getActivity(), asyncTaskEventHandler, new GetLeaderboardTaskCompletionCallback() {
			
			@Override
			public void onGetLeaderboardTaskSuccess(LeaderboardUser[] users) 
			{
				asyncTaskEventHandler.dismiss();
				
				mAdapter = new LeaderboardAdapter(LeaderboardFragment.this.getActivity(), TweetCommonData.mImageFetcher, users, new OnProfilePicClick() {
					
					@Override
					public void onItemClick(int position) {
						//Show user profile view
						LeaderboardUser user = (LeaderboardUser)mAdapter.getItem(position);
		        		if(user != null)
		        		{
		        			String owner = user.username;
		        			if(!TextUtils.isEmpty(owner))
		            		{
//		            			UserProfileFragment fragment = new UserProfileFragment();
//		            			Bundle bundle = new Bundle();
//		            			bundle.putString("username", owner);
//		            			fragment.setArguments(bundle);
//		                        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//		                        ft.add(android.R.id.content, fragment, TAG);
//		                        ft.commit();
		        				
		        				Intent intent = new Intent(LeaderboardFragment.this.getActivity(), UserProfileFragment.class);
		        				intent.putExtra("username", owner);
		        				LeaderboardFragment.this.getActivity().startActivity(intent);
		                    }
		        		}
		        		
					}
				});
				
				mListView.setAdapter(mAdapter);
			}
			
			@Override
			public void onGetLeaderboardTaskFailure() 
			{
				asyncTaskEventHandler.dismiss();
				Log.e("LeaderboardTask", "Failed");
			}
			
			@Override
			public void onGetLeaderboardTaskCancelled() 
			{
				asyncTaskEventHandler.dismiss();
				
			}
		}).execute();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.leaderboard, container, false);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
 
}
