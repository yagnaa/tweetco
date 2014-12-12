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

package com.yagnasri.displayingbitmaps.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.scrolllist.InfiniteScrollListView;

/**
 * A ListView with upward/downward infinite scrolling capability, with a customizable view
 * to be displayed at the top/bottom of the list as the loading indicator
 */
public class TweetListView extends ListView implements InfiniteScrollListPageListener {
	
	
    public TweetListView(Context context) {
        super(context, null);
    }

    public TweetListView(Context context, AttributeSet attrs) {
    	super(context, attrs);

    }

    public TweetListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
	@Override
	public void endOfList() {
		// Remove loading view when there is no more to load
	//	removeLoadingView(this, loadingView);
	}

	@Override
	public void hasMore() {
		// Display loading view when there might be more to load
	//	addLoadingView(InfiniteScrollListView.this, loadingView);
	}
}