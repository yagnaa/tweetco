package com.tweetco.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imagedisplay.util.AsyncTask;
import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.onefortybytes.R;
import com.tweetco.datastore.AccountSingleton;
import com.tweetco.dao.TweetUser;
import com.tweetco.datastore.UsersListSigleton;
import com.tweetco.interfaces.OnChangeListener;
import com.tweetco.models.UsersListModel;
import com.tweetco.tweets.TweetCommonData;

public class UsersListFragment extends ListFragmentWithSwipeRefreshLayout implements OnChangeListener<UsersListModel>
{
	private UsersListModel userListModel;

	private UserListAdapter userListAdapter = null;

	private String mUserName = null;

	ImageFetcher imageFetcher = null;

	private List<String> mUsersListFilter = new ArrayList<>();
	
	public UsersListFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		userListModel = new UsersListModel();

	}

	public void addUser(String usersListStr)
	{
		if(!TextUtils.isEmpty(usersListStr))
		{
			String[] list = usersListStr.split(";");
			for (String username : list)
			{
				if(!TextUtils.isEmpty(username))
				{
					mUsersListFilter.add(username);
				}
			}
		}
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		userListModel.addListener(this);
	}

	public void onPause() {
		super.onPause();
		userListModel.removeListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);

		imageFetcher = Utils.getImageFetcher(this.getActivity(), 60, 60);
		
		this.setListAdapter(null);

		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params) {
				try {
					mUserName = AccountSingleton.INSTANCE.getAccountModel().getAccountCopy().getUsername();
					userListModel.loadUsersList();
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
							userListModel.refreshUsersFromServer();
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

	public void followOrUnfollowUser(final String username, final boolean requestForFollow)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(requestForFollow)
				{
					try {
						userListModel.followUser(username);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else
				{
					try {
						userListModel.unfollowUser(username);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();


	}

	private void setFollowButton(Button followButton,boolean isCurrentUserAFollower)
	{
		if(isCurrentUserAFollower)
		{
			followButton.setText("Following");
			followButton.setSelected(false);
			followButton.setTextColor(getResources().getColor(R.color.white));
		}
		else
		{
			followButton.setText("Follow");
			followButton.setSelected(true);
			followButton.setTextColor(getResources().getColor(R.color.selector_green));
		}
	}
	
	
	private void loadProfileImage(TweetUser user,ImageView imageView)
	{
		if(TextUtils.isEmpty(user.profileimageurl))
		{
			String initials = Utils.getInitials(user.displayname);
			imageFetcher.loadImage(initials, imageView);
		}
		else
		{
			imageFetcher.loadImage(user.profileimageurl, imageView);
		}
	}

	@Override
	public void onChange(UsersListModel model) {
		this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (userListAdapter == null) {
					userListAdapter = new UserListAdapter(UsersListFragment.this.getActivity(), R.layout.users_list_row, new ArrayList<TweetUser>());

					UsersListFragment.this.setListAdapter(userListAdapter);
				}

				userListAdapter.getFilter().filter(null);
			}
		});


	}


	private class UserListAdapter extends ArrayAdapter<TweetUser>
	{
		Context mContext = null;
		private List<TweetUser> data;
		private TweetUserFilter mFilter;

		public UserListAdapter(Context context, int resource,List<TweetUser> objects) {
			super(context, resource, objects);
			mContext = context;
			data = objects;
			mFilter = new TweetUserFilter();
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
							Intent intent = new Intent(UserListAdapter.this.getContext(), UserProfileActivity.class);
							intent.putExtra(Constants.USERNAME_STR, user.username);
							UsersListFragment.this.getActivity().startActivity(intent);
						}
					}
				});
				
				
				loadProfileImage(user,imageView);


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
						followOrUnfollowUser(user.username,!isCurrentUserAFollower);
					}
				});

			}

			return convertView;
		}

		@Override
		public Filter getFilter() {
			return mFilter;
		}

		private class TweetUserFilter extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				FilterResults results = new FilterResults();

				if (mUsersListFilter.size() == 0) {
					List<TweetUser> list = new ArrayList<TweetUser>(UsersListSigleton.INSTANCE.getUsersList());
					results.values = list;
					results.count = list.size();
				} else {
					List<TweetUser> list = new ArrayList<>(mUsersListFilter.size());
					for (String username : mUsersListFilter) {
						TweetUser user = UsersListSigleton.INSTANCE.getUser(username);
						if (user != null) {
							list.add(user);
						}
					}

					results.values = list;
					results.count = list.size();
				}
				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				List<TweetUser> list = (List<TweetUser>) results.values;
				data.removeAll(list);
				data.addAll(list);
				data.retainAll(list);
				notifyDataSetChanged();
			}
		}
	}
}
