package com.tweetco.activities.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;
import com.onefortybytes.R;
import com.tweetco.dao.LeaderboardUser;
import com.tweetco.utility.UiUtility;

public class LeaderboardAdapter extends ArrayAdapter<LeaderboardUser> 
{
	public interface OnProfilePicClick
	{
		void onItemClick(int position);
	}

	private static final String TAG = "LeaderboardAdapter";
	private Context mContext = null;
	private OnProfilePicClick mOnProfilePicClickCallback = null;
	private ImageFetcher mImageFetcher; //Fetches the images


	public LeaderboardAdapter(Activity context, int resoureId, ImageFetcher imageFectcher,OnProfilePicClick onProfilePicClickCallback)
	{
		super(context, resoureId);
		mContext = context;
		mOnProfilePicClickCallback = onProfilePicClickCallback;
		mImageFetcher = imageFectcher;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if (convertView == null) 
		{	
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.leaderview, parent,false);
		}


		LeaderboardUser user = (LeaderboardUser)getItem(position);
		if(user != null)
		{

			ImageView profilePic = (ImageView)convertView.findViewById(R.id.leaderProfilePic);
			profilePic.setScaleType(ImageView.ScaleType.CENTER_CROP);

			profilePic.setTag(position);
			profilePic.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) 
				{
					int position = Integer.parseInt(v.getTag().toString());
					mOnProfilePicClickCallback.onItemClick(position);
				}
			});

			TextView leaderDisplayName = UiUtility.getView(convertView, R.id.leaderDisplayName);
			TextView leaderHandle = UiUtility.getView(convertView, R.id.handle);
			TextView likesCount = UiUtility.getView(convertView, R.id.leaderLikesCount);
			TextView bookmarkedCount = UiUtility.getView(convertView, R.id.leaderBookmarkedCount);
			ImageView rankImage = UiUtility.getView(convertView, R.id.rankImage);
			TextView rank = UiUtility.getView(convertView,  R.id.rank);
			if(position >= 0 && position < 3)
			{
				rankImage.setVisibility(View.VISIBLE);
				rank.setVisibility(View.GONE);
				
				if(position == 0)
				{
					rankImage.setImageResource(R.drawable.gold);
				}
				else if(position == 1)
				{
					rankImage.setImageResource(R.drawable.silver);
				}
				else if(position == 2)
				{
					rankImage.setImageResource(R.drawable.bronze);
				}
			}
			else
			{
				rankImage.setVisibility(View.GONE);
				rank.setVisibility(View.VISIBLE);
				
				rank.setText(String.valueOf(position+1));
			}

			leaderDisplayName.setText((!TextUtils.isEmpty(user.displayname)?user.displayname: user.username));
			leaderHandle.setText(Utils.getTweetHandle(user.username));
			likesCount.setText((!TextUtils.isEmpty(user.upvotes)?user.upvotes: "0"));
			bookmarkedCount.setText((!TextUtils.isEmpty(user.bookmarks)?user.bookmarks: "0"));
			loadProfileImage(user,profilePic);
		}

		return convertView;
	}
	
	private void loadProfileImage(LeaderboardUser user,ImageView imageView)
	{
		if(TextUtils.isEmpty(user.profileimageurl))
		{
			String initials = Utils.getInitials(user.displayname);
			mImageFetcher.loadImage(initials, imageView);
		}
		else
		{
			mImageFetcher.loadImage(user.profileimageurl, imageView);
		}
	}

}
