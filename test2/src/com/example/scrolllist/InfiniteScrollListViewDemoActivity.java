/**
 *
 * Copyright 2013 Wei Xiao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.example.scrolllist;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scrolllist.DemoListAdapter.NewPageListener;
import com.example.test.R;

/**
 * A demo for the listView with infinite scrolling capability
 * It shows how to make the loading happen when the list view reaches its top or bottom
 * It also shows how to display a customizable view as the loading indicator view either
 * at the top or bottom of the list view
 */
public class InfiniteScrollListViewDemoActivity extends Activity {

	// A setting for how many items should be loaded at once from the server
	private static final int SEVER_SIDE_BATCH_SIZE = 10;
	private static final String GITHUB_LINK = "https://github.com/weixiao1984/Android-Infinite-Scroll-Listview";

	private InfiniteScrollListView demoListView;


	private DemoListAdapter demoListAdapter;
	private BogusRemoteService bogusRemoteService;
	private Handler handler;
	private AsyncTask<Void, Void, List<String>> fetchAsyncTask;

	private Map<String, Integer> sushiMappings;


	public InfiniteScrollListViewDemoActivity () {
		super();
		bogusRemoteService = new BogusRemoteService();
		// Set up the image mapping for data points
		sushiMappings = new LinkedHashMap<String, Integer>();
		sushiMappings.put("Akaki", R.drawable.ic_launcher);
		sushiMappings.put("Ama Ebi", R.drawable.ic_launcher);
		sushiMappings.put("Anago", R.drawable.ic_launcher);
		sushiMappings.put("Kuruma Ebi", R.drawable.ic_launcher);
		sushiMappings.put("Hamachi", R.drawable.ic_launcher);
		sushiMappings.put("Hirame", R.drawable.ic_launcher);
		sushiMappings.put("Hokki", R.drawable.ic_launcher);
		sushiMappings.put("Hotate", R.drawable.ic_launcher);
		sushiMappings.put("Ika", R.drawable.ic_launcher);
		sushiMappings.put("Ikura", R.drawable.ic_launcher);
		sushiMappings.put("Inari", R.drawable.ic_launcher);
		sushiMappings.put("Kaibashira", R.drawable.ic_launcher);
		sushiMappings.put("Kaki", R.drawable.ic_launcher);
		sushiMappings.put("Maguro", R.drawable.ic_launcher);
		sushiMappings.put("Hon Maguro", R.drawable.ic_launcher);
		sushiMappings.put("Maguro Toro", R.drawable.ic_launcher);
		sushiMappings.put("Masago", R.drawable.ic_launcher);
		sushiMappings.put("Mirugai", R.drawable.ic_launcher);
		sushiMappings.put("Saba", R.drawable.ic_launcher);
		sushiMappings.put("Sake", R.drawable.ic_launcher);
		sushiMappings.put("Tai", R.drawable.ic_launcher);
		sushiMappings.put("Tako", R.drawable.ic_launcher);
		sushiMappings.put("Tamago", R.drawable.ic_launcher);
		sushiMappings.put("Tobiko", R.drawable.ic_launcher);
		sushiMappings.put("Torigai", R.drawable.ic_launcher);
		sushiMappings.put("Unagi", R.drawable.ic_launcher);
		sushiMappings.put("Uni", R.drawable.ic_launcher);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
		handler = new Handler();

		demoListView = (InfiniteScrollListView) this.findViewById(R.id.infinite_listview_infinitescrolllistview);

		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		demoListView.setLoadingView(layoutInflater.inflate(R.layout.loading_view_demo, null));
		demoListAdapter = new DemoListAdapter(new NewPageListener() {
			@Override
			public void onScrollNext() {
				fetchAsyncTask = new AsyncTask<Void, Void, List<String>>() {
					@Override
					protected void onPreExecute() {
						// Loading lock to allow only one instance of loading
						demoListAdapter.lock();
					}
					@Override
					protected List<String> doInBackground(Void ... params) {
						List<String> result;
						// Mimic loading data from a remote service

						result = bogusRemoteService.getNextSushiBatch(SEVER_SIDE_BATCH_SIZE);

						return result;
					}
					@Override
					protected void onPostExecute(List<String> result) {
						if (isCancelled() || result == null || result.isEmpty()) {
							demoListAdapter.notifyEndOfList();
						} else {
							// Add data to the placeholder

								demoListAdapter.addEntriesToBottom(result);
							
							// Add or remove the loading view depend on if there might be more to load
							if (result.size() < SEVER_SIDE_BATCH_SIZE) {
								demoListAdapter.notifyEndOfList();
							} else {
								demoListAdapter.notifyHasMore();
							}
						}
					};
					@Override
					protected void onCancelled() {
						// Tell the adapter it is end of the list when task is cancelled
						demoListAdapter.notifyEndOfList();
					}
				}.execute();
			}
			@Override
			public View getInfiniteScrollListView(int position, View convertView, ViewGroup parent) {
				// Customize the row for list view
				if(convertView == null) {
					LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = layoutInflater.inflate(R.layout.row_demo, null);
				}
				String name = (String) demoListAdapter.getItem(position);
				if (name != null) {
					TextView rowName = (TextView) convertView.findViewById(R.id.row_name);
					ImageView rowPhoto = (ImageView) convertView.findViewById(R.id.row_photo);
					rowName.setText(name);

						rowPhoto.setImageResource(sushiMappings.get(name));
					
				}
				return convertView;
			}
		});
		demoListView.setAdapter(demoListAdapter);
		// Display a toast when a list item is clicked
		demoListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(InfiniteScrollListViewDemoActivity.this, demoListAdapter.getItem(position) + " " + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
					}
				});
			}
		});



	}

	@Override
	protected void onResume() {
		super.onResume();
		// Load the first page to start demo
		demoListAdapter.onScrollNext();
	}
}