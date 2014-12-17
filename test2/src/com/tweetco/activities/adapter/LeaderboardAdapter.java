package com.tweetco.activities.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tweetco.R;
import com.tweetco.utility.UiUtility;
import com.yagnasri.dao.LeaderboardUser;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.util.ImageFetcher;

public class LeaderboardAdapter extends BaseAdapter 
{
	public interface OnProfilePicClick
	{
		void onItemClick(int position);
	}

    private Context mContext = null;
	private TweetUser[] mTweetUsers = null;
	private OnProfilePicClick mOnProfilePicClickCallback = null;
	private ImageFetcher mImageFetcher; //Fetches the images
    
	
	public LeaderboardAdapter(Activity context, ImageFetcher imageFetcher, TweetUser[] tweetUsers, OnProfilePicClick onProfilePicClickCallback)
	{
		mContext = context;
		mTweetUsers = tweetUsers;
		mOnProfilePicClickCallback = onProfilePicClickCallback;
		mImageFetcher = imageFetcher;
	}
	
	@Override
    public int getCount() 
	{
       	return mTweetUsers.length;
    }

    @Override
    public Object getItem(int position) {
        return mTweetUsers[position];
    }

    @Override
    public long getItemId(int position) {
        return position < 0 ? 0 : position;
    }

    @Override
    public int getViewTypeCount() 
    {
        // Two types of views, the normal ImageView and the top row of empty views
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ImageView imageView;
        if (convertView == null) { // if it's not recycled, instantiate and initialize
        	
			LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.leaderview, null);
        	
        	
            imageView = (ImageView)convertView.findViewById(R.id.leaderProfilePic);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else { // Otherwise re-use the converted view
        	imageView = (ImageView)convertView.findViewById(R.id.leaderProfilePic);
        }
        
        imageView.setTag(position);
        imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int position = Integer.parseInt(v.getTag().toString());
        		mOnProfilePicClickCallback.onItemClick(position);
			}
		});
        
        LeaderboardUser user = (LeaderboardUser)getItem(position);
        if(user != null)
        {
        	TextView leaderDisplayName = UiUtility.getView(convertView, R.id.leaderDisplayName);
        	TextView likesCount = UiUtility.getView(convertView, R.id.leaderLikesCount);
        	TextView bookmarkedCount = UiUtility.getView(convertView, R.id.leaderBookmarkedCount);
        	
        	leaderDisplayName.setText((!TextUtils.isEmpty(user.displayname)?user.displayname: user.username));
        	likesCount.setText((!TextUtils.isEmpty(user.upvotes)?user.upvotes: "0"));
        	bookmarkedCount.setText((!TextUtils.isEmpty(user.bookmarks)?user.bookmarks: "0"));
        	mImageFetcher.loadImage(user.profileimageurl, imageView);
        	
        }
        
        return convertView;
	}

}
