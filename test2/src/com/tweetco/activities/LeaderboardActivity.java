package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.yagnasri.displayingbitmaps.ui.Tweet;

public class LeaderboardActivity extends TweetCoBaseActivity 
{
	ListView mListView = null;
	AsyncTaskEventHandler asyncTaskEventHandler = null;
	private LeaderboardAdapter mAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard);
		
		mListView = UiUtility.getView(this, R.id.leaderboardList);
		asyncTaskEventHandler = new AsyncTaskEventHandler(this, "Getting Leaderboard");
		
//		new GetLeaderboardTask(this, asyncTaskEventHandler, new GetLeaderboardTaskCompletionCallback() {
//			
//			@Override
//			public void onGetLeaderboardTaskSuccess(LeaderboardUser[] users) 
//			{
//				asyncTaskEventHandler.dismiss();
//				
////				mAdapter = new LeaderboardAdapter(LeaderboardActivity.this, TweetCommonData.mImageFetcher, users, new OnProfilePicClick() {
////					
////					@Override
////					public void onItemClick(int position) {
////						//Show user profile view
////						LeaderboardUser user = (LeaderboardUser)mAdapter.getItem(position);
////		        		if(user != null)
////		        		{
////		        			String owner = user.username;
////		        			if(!TextUtils.isEmpty(owner))
////		            		{
//////		            			UserProfileFragment fragment = new UserProfileFragment();
//////		            			Bundle bundle = new Bundle();
//////		            			bundle.putString("username", owner);
//////		            			fragment.setArguments(bundle);
//////		                        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//////		                        ft.add(android.R.id.content, fragment, TAG);
//////		                        ft.commit();
////		        				
////		        				Intent intent = new Intent(LeaderboardActivity.this, UserProfileFragment.class);
////		        				intent.putExtra("username", owner);
////		        				LeaderboardActivity.this.startActivity(intent);
////		                    }
////		        		}
////		        		
////					}
////				});
//				
//				mListView.setAdapter(mAdapter);
//			}
//			
//			@Override
//			public void onGetLeaderboardTaskFailure() 
//			{
//				asyncTaskEventHandler.dismiss();
//				Log.e("LeaderboardTask", "Failed");
//			}
//			
//			@Override
//			public void onGetLeaderboardTaskCancelled() 
//			{
//				asyncTaskEventHandler.dismiss();
//				
//			}
//		}).execute();
		
		
		
		
	}
}
