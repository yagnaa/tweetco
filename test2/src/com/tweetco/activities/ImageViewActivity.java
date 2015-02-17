package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.imagedisplay.util.ImageFetcher;
import com.imagedisplay.util.Utils;


public class ImageViewActivity extends TweetCoBaseActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		final String url  = intent.getStringExtra(Constants.IMAGE_TO_VIEW);
		
		final ImageView image = new ImageView(this);
		image.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		this.setContentView(image);
		
		image.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				int height = image.getMeasuredHeight();
				int width = image.getMeasuredWidth();
				ImageFetcher fetcher = Utils.getImageFetcher(ImageViewActivity.this, width, height);
				fetcher.loadImage(url, image);
			}
		});
		
		ActionBar actionbar = getSupportActionBar();
		if(actionbar!=null)
		{
			actionbar.setHomeButtonEnabled(true);
			actionbar.setDisplayHomeAsUpEnabled(true);
		}
		
	}

	@Override
	public void onResumeCallback() {
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
