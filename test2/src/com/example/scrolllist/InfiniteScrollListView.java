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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.yagnasri.displayingbitmaps.ui.InfiniteScrollListPageListener;

/**
 * A ListView with upward/downward infinite scrolling capability, with a customizable view
 * to be displayed at the top/bottom of the list as the loading indicator
 */
public class InfiniteScrollListView extends ListView implements InfiniteScrollListPageListener {

	public static final String TAG = InfiniteScrollListView.class.getSimpleName();

	private View loadingView;

	// A flag to prevent loading header or footer more than once
	private boolean loadingViewVisible = false;

	public InfiniteScrollListView(Context context) {
		super(context);
	}

	public InfiniteScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InfiniteScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setLoadingView(View loadingView) {
		this.loadingView = loadingView;
	}

	private void addLoadingView(ListView listView, View loadingView) {
		if (listView == null || loadingView == null) {
			return;
		}
		// Avoid overlapping the header or footer
		if (!loadingViewVisible) {

			// Add loading view to list view footer when scroll down to load
			listView.addFooterView(loadingView);

			loadingViewVisible = true;
		}
	}

	private void removeLoadingView(ListView listView, View loadingView) {
		if (listView == null || loadingView == null) {
			return;
		}
		// Remove header or footer depending on the loading mode
		if (loadingViewVisible) {

			listView.removeFooterView(loadingView);

			loadingViewVisible = false;
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// Force the list view to accept its own type of adapter
		if (!(adapter instanceof InfiniteScrollListAdapter)) {
			throw new IllegalArgumentException(InfiniteScrollListAdapter.class.getSimpleName() + " expected");
		}
		// Pass information to adaptor
		InfiniteScrollListAdapter infiniteListAdapter = (InfiniteScrollListAdapter) adapter;

		infiniteListAdapter.setInfiniteListPageListener(this);
		this.setOnScrollListener(infiniteListAdapter);
		// Workaround to keep spaces for header and footer
		View dummy = new View(getContext());
		addLoadingView(InfiniteScrollListView.super, dummy);
		super.setAdapter(adapter);
		removeLoadingView(InfiniteScrollListView.super, dummy);
	}

	@Override
	public void endOfList() {
		// Remove loading view when there is no more to load
		removeLoadingView(this, loadingView);
	}

	@Override
	public void hasMore() {
		// Display loading view when there might be more to load
		addLoadingView(InfiniteScrollListView.this, loadingView);
	}

}