package com.tweetco.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;

import com.tweetco.R;
import com.tweetco.activities.adapter.LeaderboardAdapter;
import com.tweetco.activities.adapter.LeaderboardAdapter.OnProfilePicClick;
import com.tweetco.activities.progress.AsyncTaskEventHandler;
import com.tweetco.asynctasks.GetLeaderboardTask;
import com.tweetco.asynctasks.GetLeaderboardTask.GetLeaderboardTaskCompletionCallback;
import com.yagnasri.dao.LeaderboardUser;

public class LeaderboardFragment extends ListFragment 
{
	private AsyncTaskEventHandler asyncTaskEventHandler = null;
	private LeaderboardAdapter mAdapter = null;
	
	public LeaderboardFragment()
	{
		
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
		mAdapter = new LeaderboardAdapter(LeaderboardFragment.this.getActivity(), R.layout.leaderview, onProfileClick);
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
    				
    				Intent intent = new Intent(LeaderboardFragment.this.getActivity(), UserProfileFragment.class);
    				intent.putExtra(Constants.USERNAME_STR, owner);
    				LeaderboardFragment.this.getActivity().startActivity(intent);
                }
    		}
    		
		}
	};
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) 
    {
        super.onActivityCreated(savedInstanceState);
		
        loadLeaderBoard();
		asyncTaskEventHandler = new AsyncTaskEventHandler(this.getActivity(), "Getting Leaderboard");
		this.setListAdapter(mAdapter);
	}
    
    public void loadLeaderBoard()
    {		
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
			}
			
			@Override
			public void onGetLeaderboardTaskFailure() 
			{
				Log.e("LeaderboardTask", "Failed");
			}
			
			@Override
			public void onGetLeaderboardTaskCancelled() 
			{
				
			}
		}).execute();
    }

 
}
