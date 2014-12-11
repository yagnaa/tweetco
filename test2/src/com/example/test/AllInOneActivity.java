package com.example.test;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.scrolllist.InfiniteScrollListView;
import com.example.scrolllist.InfiniteScrollListViewDemoActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;



public class AllInOneActivity extends ListActivity
{
	
	private MobileServiceClient mClient;
	
	private InfiniteScrollListView demoListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_demo);
		
		demoListView = (InfiniteScrollListView) this.findViewById(R.id.infinite_listview_infinitescrolllistview);
		
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
		
		
		Item item = new Item();
		item.Text = "Awesome item";
		mClient.getTable(Item.class).insert(item, new TableOperationCallback<Item>() {
		      public void onCompleted(Item entity, Exception exception, ServiceFilterResponse response) {
		            if (exception == null) {
		                  // Insert succeeded
		            } else {
		                  // Insert failed
		            }
		      }
		});
		
		String[] list = new String[] { "first","second"};
        // Creates the backing adapter for the ListView.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getApplicationContext(), R.layout.noteslist_item, list);
        
        // Sets the ListView's adapter to be the cursor adapter that was just created.
        setListAdapter(adapter);
        
        this.getListView().setOnItemClickListener( new AdapterView.OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> adapterView , View view , int position ,long arg3) 
            {
                Log.i("Item clicked","tushar:itemclicked") ;
                tweetusers item = new tweetusers();
                item.username = "IronMan";
//        		mClient.getTable(tweetusers.class).insert(item, new TableOperationCallback<tweetusers>() {
//        		      public void onCompleted(tweetusers entity, Exception exception, ServiceFilterResponse response) {
//        		            if (exception == null) {
//        		                Log.i("Item clicked","first inserted") ;
//        		            } else {
//        		                Log.i("Item clicked","first insertion faileder") ;
//        		            }
//        		      }
//        		});
        		
//        		mClient.getTable(tweetusers.class).where().execute(new TableQueryCallback<tweetusers>() {
//					
//					@Override
//					public void onCompleted(List<tweetusers> UsersList, int arg1, Exception arg2,
//							ServiceFilterResponse arg3) {
//						for(tweetusers users:UsersList)
//						{
//							Log.i("Item clicked",users.displayname+"") ;
//							Log.i("Item clicked",users.userid+"") ;
//							Log.i("Item clicked",users.username+"") ;
//							Log.i("Item clicked","") ;
//							Log.i("Item clicked","") ;
//							Log.i("Item clicked","") ;
//							
//						}
//						
//					}
//				});
        		
                mClient.invokeApi("getusers", "GET", new ArrayList<Pair<String,String>>(), new ApiJsonOperationCallback() {
					
					@Override
					public void onCompleted(JsonElement arg0, Exception arg1,
							ServiceFilterResponse arg2) {
						
		                Log.i("Item clicked","tushar:itemclicked") ;
						
					}
				});

//        		List<Pair<String,String>> params = new ArrayList<Pair<String,String>>();
//        		params.add(new Pair<String, String>("kRequestingUserKey", "tweetbot"));
//        		params.add(new Pair<String, String>("kFeedTypeKey", "homefeed"));
        		JsonObject obj = new JsonObject();
        		obj.addProperty("requestingUser", "tweetbot");
        		obj.addProperty("feedtype", "homefeed");
        		mClient.invokeApi("gettweetsforuser", obj, new ApiJsonOperationCallback() {
					
					@Override
					public void onCompleted(JsonElement arg0, Exception arg1,
							ServiceFilterResponse arg2) {
					      Log.i("Item clicked","Json received") ;
						
					}
				});
        		
        		
//				JsonObject element = new JsonObject();
//				element.addProperty("tweetowner", TweetCo.getAccount().getUsername());
//				element.addProperty("tweetcontent", mTweetContent.getEditableText().toString());
//				mClient.invokeApi("PostTweet", element, new ApiJsonOperationCallback() {
//					
//					@Override
//					public void onCompleted(JsonElement element, Exception exception,
//							ServiceFilterResponse arg2) {
//						if(exception != null)
//						{
//							Log.d("postTweet", "TweetPosted");
//						}
//						else
//						{
//							Log.e("postTweet", "TweetPost failed");
//						}
//						
//					}
//				});
//			} 
//			catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

        		
        		
        		
        		Intent intent = new Intent();
        		intent.setClass(AllInOneActivity.this, InfiniteScrollListViewDemoActivity.class);
        		AllInOneActivity.this.startActivity(intent);

            }
        });
        

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