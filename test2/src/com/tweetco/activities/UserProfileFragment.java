package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tweetco.R;
import com.tweetco.tweets.TweetCommonData;
import com.tweetco.utility.UiUtility;
import com.yagnasri.dao.TweetUser;
import com.yagnasri.displayingbitmaps.ui.TweetAdapter;
import com.yagnasri.displayingbitmaps.ui.TweetListFragment;

public class UserProfileFragment extends FragmentActivity 
{
	private String mUserName = null;
	
	private ImageView mUserProfileBg = null;
	private ImageView mUserProfilePic = null;
	private TextView mUserProfileDisplayName = null;
	private TextView mUserProfileHandleName = null;
	private TextView mFollowerCount = null;
	private TextView mFolloweeCount = null;
	private Button mEditProfileButton = null;
	/**
     * Empty constructor as per the Fragment documentation
     */
    public UserProfileFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.userprofilefragment);
       
        mUserName = getIntent().getExtras().getString(Constants.USERNAME_STR);
        TweetUser user = TweetCommonData.tweetUsers.get(mUserName);
    	if(user != null)
    	{
    		mUserProfileBg = UiUtility.getView(this, R.id.userProfileBg);
    		mUserProfilePic = UiUtility.getView(this, R.id.userProfilePic);
    		mUserProfileDisplayName = UiUtility.getView(this, R.id.userProfileDisplayName);
    		mUserProfileHandleName = UiUtility.getView(this, R.id.userProfileHandle);
    		mFolloweeCount = UiUtility.getView(this, R.id.followeesCount);
    		mFollowerCount = UiUtility.getView(this, R.id.followersCount);
    		
    		mUserProfileDisplayName.setText((TextUtils.isEmpty(user.displayname))?mUserName:user.displayname);
    		mUserProfileHandleName.setText("@"+mUserName);
    		int followeesCount = 0;
    		if(!TextUtils.isEmpty(user.followees))
    		{
    			followeesCount = user.followees.split(";").length - 1;
    		}
    		mFolloweeCount.setText(String.valueOf(followeesCount));
    		
    		int followersCount = 0;
    		if(!TextUtils.isEmpty(user.followers))
    		{
    			followersCount = user.followers.split(";").length - 1;
    		}
    		mFollowerCount.setText(String.valueOf(followersCount));
    		
    		if(mUserName.equals((TweetCommonData.getUserName())))
    		{
    			LinearLayout layout = UiUtility.getView(this, R.id.editProfileButtonLayout);
    			layout.setVisibility(View.VISIBLE);
    			mEditProfileButton = UiUtility.getView(this, R.id.editProfileButton);
    			mEditProfileButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) 
					{
						startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
					}
				});
    		}
    	}
    	
    	if(UiUtility.getView(this, R.id.tweetsListFragmentContainer) != null)
		{
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            TweetListFragment tweetListFragment = new TweetListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.USERNAME_STR, mUserName);
            bundle.putString(Constants.FEEDTYPE_STR, TweetAdapter.TweetListMode.USER_FEED.name());
            bundle.putBoolean("hideFooter", true);
            tweetListFragment.setArguments(bundle);
            ft.replace(R.id.tweetsListFragmentContainer, tweetListFragment);
            ft.commit();
		}
    }
}
