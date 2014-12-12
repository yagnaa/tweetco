package com.yagnasri.displayingbitmaps.ui;
import java.net.MalformedURLException;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.scrolllist.BogusRemoteService;
import com.example.scrolllist.DemoListAdapter;
import com.example.scrolllist.InfiniteScrollListView;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.yagnasri.displayingbitmaps.ui.TweetListFragment;



public class AllInOneActivity extends FragmentActivity
{
	private static final int SEVER_SIDE_BATCH_SIZE = 10; //Number of tweets fetched from server at one time
    private static final String IMAGE_CACHE_DIR = "thumbs"; //Name of directory where images are saved
	
	public static MobileServiceClient mClient;
	
	private InfiniteScrollListView demoListView;
	
	private DemoListAdapter demoListAdapter;
	private BogusRemoteService bogusRemoteService;
	
	private Handler handler;
	
	private static final String TAG = "ImageGridActivity";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		

        // Initialize the demo client here and use it all over.
        
		try {
			mClient = new MobileServiceClient(
				      "https://tweetcotest.azure-mobile.net/",
				      "PImqNtOVaoZFzGrQDAcrXwQnpLuZCf69",
				      this
				);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
        
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new TweetListFragment(), TAG);
            ft.commit();
        }
        

		
		
		
		
     

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
}