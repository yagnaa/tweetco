package com.tweetco.activities;

import android.content.Intent;
import android.os.Bundle;
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
				LayoutParams.WRAP_CONTENT));
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
		
		
	}

	@Override
	public void onResumeCallback() {
		
	}
}
