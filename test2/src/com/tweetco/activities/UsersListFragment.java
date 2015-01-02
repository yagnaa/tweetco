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

		if(TweetCommonData.mClient != null)
		{
			mTweetUsersTable = TweetCommonData.mClient.getTable("tweetusers",TweetUser.class);
			mUserName = TweetCommonData.getUserName();
			
			if(usersList == null || usersList.isEmpty())
			{
				loadUsers();
			}
		}
		else
		{
			Log.e("UsersListFragment", "MobileServiceClient is null");
		}

		
	}
	
	


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
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
							Log.e("tag", "msg");
							userListAdapter.clear();
							for (TweetUser tweetUser : result) 
							{
								TweetCommonData.tweetUsers.put(tweetUser.username, tweetUser);
								if(!mUserName.equals(tweetUser.username))
								{
									//Don't add the same user
									userListAdapter.add(tweetUser);
								}
							}
							userListAdapter.notifyDataSetChanged();
						} else {
							//createAndShowDialog(exception, "Error");
						}
					}
				});
				
				return null;
			}
		}.execute();
	}

	
	public void followOrUnfollowUser(final View followButton,final String requestingUser, final String userToFollowOrUnfollow,boolean follow)
	{
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kApiRequesterKey, requestingUser);
		if(follow)
		{
			obj.addProperty(ApiInfo.kUserToFollowKey, userToFollowOrUnfollow);
		}
		else
		{
			obj.addProperty(ApiInfo.kUserToUnFollowKey, userToFollowOrUnfollow);
		}
		
		String customApiName = follow?ApiInfo.FOLLOW_USER: ApiInfo.UN_FOLLOW_USER;
		mClient.invokeApi(customApiName, obj, new ApiJsonOperationCallback() {
			
			@Override
			public void onCompleted(JsonElement arg0, Exception arg1,
					ServiceFilterResponse arg2) {
				if(arg1 == null)
				{
					if(followButton!=null && (followButton instanceof Button))
					{
						Log.d("Item clicked","Follow/Unfollow succeeded") ;
						loadUsers();
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
							intent.putExtra(Constants.USERNAME_STR, user.username);
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
		        boolean isCurrentUserAFollower  = TweetUtils.isStringPresent(user.followers, mUserName);
		        if(isCurrentUserAFollower)
		        {
		        	button.setText("Unfollow");
		        }
		        else
		        {
		        	button.setText("Follow");
		        }
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
		        			followOrUnfollowUser(v, mUserName, user.username, !TweetUtils.isStringPresent(user.followers, mUserName));
		        		}						
					}
				});
				
			}
			
			return convertView;
		}
		
		
		
	}
}
