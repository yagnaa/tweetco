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
		mUserName = TweetCommonData.getUserName();

		if(usersList == null || usersList.isEmpty())
		{
			loadUsers();
		}
	}




	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
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

	//TODO this is not complete yet
	public void loadUser(final TweetUser user)
	{
		// Get the items that weren't marked as completed and add them in the adapter
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {

				mTweetUsersTable.where().field("username").eq(user.username).execute(new TableQueryCallback<TweetUser>() 
				{

					public void onCompleted(List<TweetUser> result, int count, Exception exception, ServiceFilterResponse response) 
					{
						if (exception == null) 
						{
							userListAdapter.getPosition(user);
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
						} else 
						{
							//TODO Do we need to roll back the changes here???
						}
					}
				});
				// Get the items that weren't marked as completed and add them in the
				// adapter
				//				mTweetUsersTable.setQueryText("select * from TweetUsers where username ='"+userName+"'");
				//				mTweetUsersTable.execute(new TableQueryCallback<TweetUser>() {
				//
				//					public void onCompleted(List<TweetUser> result, int count, Exception exception, ServiceFilterResponse response) {
				//						if (exception == null) {
				//							Log.e("tag", "msg");
				//							userListAdapter.clear();
				//							for (TweetUser tweetUser : result) 
				//							{
				//								TweetCommonData.tweetUsers.put(tweetUser.username, tweetUser);
				//								if(!mUserName.equals(tweetUser.username))
				//								{
				//									//Don't add the same user
				//									userListAdapter.add(tweetUser);
				//								}
				//							}
				//							userListAdapter.notifyDataSetChanged();
				//						} else {
				//							//createAndShowDialog(exception, "Error");
				//						}
				//					}
				//				});
				//				
				return null;
			}
		}.execute();
	}


	public void followOrUnfollowUser(final Button followButton,final String userWhomShouldBeFollowed, final boolean requestForFollow)
	{
		String customApiName = null;
		MobileServiceClient mClient = TweetCommonData.mClient;
		JsonObject obj = new JsonObject();
		obj.addProperty(ApiInfo.kApiRequesterKey, TweetCommonData.getUserName());
		if(requestForFollow)
		{
			obj.addProperty(ApiInfo.kUserToFollowKey, userWhomShouldBeFollowed);
			customApiName = ApiInfo.FOLLOW_USER;
		}
		else
		{
			obj.addProperty(ApiInfo.kUserToUnFollowKey, userWhomShouldBeFollowed);
			customApiName =  ApiInfo.UN_FOLLOW_USER;
		}

		mClient.invokeApi(customApiName, obj, new ApiJsonOperationCallback() 
		{	
			@Override
			public void onCompleted(JsonElement arg0, Exception arg1, ServiceFilterResponse arg2) 
			{
				if(arg1 == null)
				{
					//TODO update the adapter
					TweetUser userToFollowOrUnFollow = (TweetUser)followButton.getTag();
					if(followButton!=null && userToFollowOrUnFollow.username.equalsIgnoreCase(userWhomShouldBeFollowed))
					{
						setFollowButton(followButton, requestForFollow);
						Log.d("Item clicked","Follow/Unfollow succeeded") ;
						loadUsers();
					}
				}
				else
				{
					setFollowButton(followButton,!requestForFollow);
					Log.e("Item clicked","Exception upVoting a tweet") ;
					arg1.printStackTrace();
				}

			}
		},false);
	}

	private void setFollowButton(Button followButton,boolean isCurrentUserAFollower)
	{
		if(isCurrentUserAFollower)
		{
			followButton.setText("Unfollow");
			followButton.setSelected(false);
		}
		else
		{
			followButton.setText("Follow");
			followButton.setSelected(true);
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

			final TweetUser user = getItem(position);

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


				Button followButton = (Button)convertView.findViewById(R.id.follow_button);
				followButton.setTag(user);

				final boolean isCurrentUserAFollower  = TweetUtils.isStringPresent(user.followers, mUserName);
				setFollowButton(followButton,isCurrentUserAFollower);

				followButton.setOnClickListener(new View.OnClickListener() 
				{	
					@Override
					public void onClick(View v) 
					{
						setFollowButton((Button)v,!isCurrentUserAFollower);

						followOrUnfollowUser((Button)v, user.username,!isCurrentUserAFollower);				
					}
				});

			}

			return convertView;
		}
	}
}
