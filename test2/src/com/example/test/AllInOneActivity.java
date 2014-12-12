package com.example.test;
import java.net.MalformedURLException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scrolllist.BogusRemoteService;
import com.example.scrolllist.DemoListAdapter;
import com.example.scrolllist.DemoListAdapter.NewPageListener;
import com.example.scrolllist.InfiniteScrollListView;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.yagnasri.bitmaps.R;



public class AllInOneActivity extends Activity
{
	private static final int SEVER_SIDE_BATCH_SIZE = 10;
	
	private MobileServiceClient mClient;
	
	private InfiniteScrollListView demoListView;
	
	private DemoListAdapter demoListAdapter;
	private BogusRemoteService bogusRemoteService;
	
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//Set the Layout
		setContentView(R.layout.activity_demo);
		demoListView = (InfiniteScrollListView) this.findViewById(R.id.infinite_listview_infinitescrolllistview);
		
		//These variables are for doing optimized image loading.
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
		

		

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

        
        
        
        
        
 
        
        setContentView(R.layout.activity_demo);
		handler = new Handler();

		demoListView = (InfiniteScrollListView) this.findViewById(R.id.infinite_listview_infinitescrolllistview);

		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		demoListView.setLoadingView(layoutInflater.inflate(R.layout.loading_view_demo, null));
		demoListAdapter = new DemoListAdapter();
		PageLoader loader = new PageLoader(this, demoListAdapter, mClient);
		demoListAdapter.setPageListener(loader);
		
		
		demoListView.setAdapter(demoListAdapter);
		// Display a toast when a list item is clicked
		demoListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(AllInOneActivity.this, demoListAdapter.getItem(position) + " " + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		
		
		
		
        

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// Load the first page to start demo
		demoListAdapter.onScrollNext();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
}