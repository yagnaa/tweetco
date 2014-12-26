package com.tweetco.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.yagnasri.displayingbitmaps.ui.TweetUtils;
import com.yagnasri.displayingbitmaps.util.AsyncTask;

public class UsersListFragment extends ListFragment
{
	List<TweetUser> usersList = new ArrayList<TweetUser>();

	private  MobileServiceTable<TweetUser> mTweetUsersTable;
	
	private UserListAdapter userListAdapter = null;
		
	private String mUserName = null;

	public UsersListFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);


		mTweetUsersTable = TweetCommonData.mClient.getTable("tweetusers",TweetUser.class);

		if(usersList == null || usersList.isEmpty())
		{
			loadUsers();
		}
		
		


//		mUserName = getIntent().getExtras().getString("username");
//		TweetUser user = TweetCommonData.tweetUsers.get(mUserName);
//		if(user != null)
//		{
//			mUserProfileBg = UiUtility.getView(this, R.id.userProfileBg);
//			mUserProfilePic = UiUtility.getView(this, R.id.userProfilePic);
//			mUserProfileDisplayName = UiUtility.getView(this, R.id.userProfileDisplayName);
//			mUserProfileHandleName = UiUtility.getView(this, R.id.userProfileHandle);
//			mFolloweeCount = UiUtility.getView(this, R.id.followeesCount);
//			mFollowerCount = UiUtility.getView(this, R.id.followersCount);
//
//			mUserProfileDisplayName.setText((TextUtils.isEmpty(user.displayname))?mUserName:user.displayname);
//			mUserProfileHandleName.setText("@"+mUserName);
//			int followeesCount = 0;
//			if(!TextUtils.isEmpty(user.followees))
//			{
//				followeesCount = user.followees.split(";").length - 1;
//			}
//			mFolloweeCount.setText(String.valueOf(followeesCount));
//
//			int followersCount = 0;
//			if(!TextUtils.isEmpty(user.followers))
//			{
//				followersCount = user.followers.split(";").length - 1;
//			}
//			mFollowerCount.setText(String.valueOf(followersCount));
//
//			if(UiUtility.getView(this, R.id.tweetsListFragmentContainer) != null)
//			{
//				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//				TweetListFragment tweetListFragment = new TweetListFragment();
//				Bundle bundle = new Bundle();
//				bundle.putString("username", mUserName);
//				bundle.putBoolean("gettweetsbyuser", true);
//				tweetListFragment.setArguments(bundle);
//				ft.add(R.id.tweetsListFragmentContainer, tweetListFragment);
//				ft.commit();
//			}
//
//		}
	}
	
	


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		userListAdapter = new UserListAdapter(this.getActivity(), R.layout.users_list_row, usersList);
		
		this.setListAdapter(userListAdapter);
		userListAdapter.notifyDataSetChanged();
	}

	public void loadUsers()
	{
		// Get the items that weren't marked as completed and add them in the adapter
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				

				// Get the items that weren't marked as completed and add them in the
				// adapter
				mTweetUsersTable.execute(new TableQueryCallback<TweetUser>() {

					public void onCompleted(List<TweetUser> result, int count, Exception exception, ServiceFilterResponse response) {
						if (exception == null) {
//							mAdapter.clear();
//
//							for (TweetUser item : result) {
//								mAdapter.add(item);
//							}
							Log.e("tag", "msg");
							userListAdapter.clear();
							userListAdapter.addAll(result);
							for (TweetUser tweetUser : result) 
							{
								TweetCommonData.tweetUsers.put(tweetUser.username, tweetUser);
							}
							userListAdapter.notifyDataSetChanged();
						} else {
							//createAndShowDialog(exception, "Error");
						}
					}
				});
				
//				try {
//					final MobileServiceList<TweetUser> result = mTweetUsersTable.where().field("complete").eq(false).execute().get();
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							mAdapter.clear();
//
//							for (ToDoItem item : result) {
//								mAdapter.add(item);
//							}
//						}
//					});
//				} catch (Exception exception) {
//					createAndShowDialog(exception, "Error");
//				}
				return null;
			}
		}.execute();
	}

	
	public void followUser(final View followButton,final String requestingUser,String userToFollow,boolean follow)
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kRequestingUserKey, requestingUser);
		obj.addProperty(ApiInfo.kUserToFollowKey, userToFollow);
		String customApiName = follow?ApiInfo.FOLLOW_USER: ApiInfo.UN_FOLLOW_USER;
		mClient.invokeApi(customApiName, obj, new ApiJsonOperationCallback() {
			
			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) {
				if(arg1 == null)
				{
					if(followButton!=null && (followButton instanceof Button))
					{
						//setUpVoteFlag( (ImageView)upvoteView, requestingUser);
						Log.e("Item clicked","Exception upVoting a tweet") ;
					}
				}
				else
				{
					Log.e("Item clicked","Exception upVoting a tweet") ;
					arg1.printStackTrace();
				}
				
			}
		},false);
	}
	
    private void setFollowButton(Button followButton,String userName)
    {
		Integer position = (Integer)followButton.getTag();
		TweetUser user = userListAdapter.getItem(position);

		if(user != null && followButton!=null)
		{
	        boolean isCurrentUserAFOllower  = TweetUtils.isStringPresent(user.followers, userName);
	        followButton.setPressed(isCurrentUserAFOllower);
		}
    }
	
	private class UserListAdapter extends ArrayAdapter<TweetUser>
	{
        Context mContext = null;
		public UserListAdapter(Context context, int resource,List<TweetUser> objects) {
			super(context, resource, objects);
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{

			if(convertView==null)
			{
				LayoutInflater inflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflator.inflate(R.layout.users_list_row, parent,false);
			}
			
			TweetUser user = getItem(position);
			
			if(user!=null)
			{
		        // Now handle the main ImageView thumbnails
		        ImageView imageView =  (ImageView)convertView.findViewById(R.id.profile_pic);
		        
		        imageView.setTag(position);
		        imageView.setOnClickListener(new OnClickListener() 
		        {	
					@Override
					public void onClick(View v) 
					{
						Integer position = (Integer)v.getTag();
						TweetUser user = UserListAdapter.this.getItem(position);
						if(user!=null)
						{
							Intent intent = new Intent(UserListAdapter.this.getContext(), UserProfileFragment.class);
							intent.putExtra("username", user.username);
							UsersListFragment.this.startActivity(intent);
						}
					}
				});
		        
		        TweetCommonData.mImageFetcher .loadImage(user.profileimageurl, imageView);
		        
		        TextView textView = (TextView)convertView.findViewById(R.id.user_name);
		        textView.setText(user.displayname);
		        
		        TextView handle = (TextView)convertView.findViewById(R.id.handle);
		        handle.setText(user.username);
		        
		        
		        Button button = (Button)convertView.findViewById(R.id.follow_button);
		        button.isPressed();
		        button.setTag(position);
		        button.setOnClickListener(new View.OnClickListener() 
		        {	
					@Override
					public void onClick(View v) 
					{
						Integer position = (Integer)v.getTag();
						//Show user profile view
						TweetUser user = (TweetUser)getItem(position);
		        		if(user != null)
		        		{
		        			followUser(v, mUserName, user.username, ((Button)v).isPressed());
		        		}						
					}
				});
				
			}
			
			return convertView;
		}
		
		
		
	}
}
