/*
 * Copyright 2013 Lars Werkman
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
 */

package com.tweetco.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class QuickReturnListView extends ListView {


	private int mItemCount;
	private int mItemOffsetY[];
	private boolean scrollIsComputed = false;
	private int mHeight;

	public QuickReturnListView(Context context) {
		super(context);
	}

	public QuickReturnListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public int getListHeight() {
		return mHeight;
	}

//	public void computeScrollY() {
//		mHeight = 0;
//		mItemCount = getAdapter().getCount();
//		if (mItemCount!=0) {
//			mItemOffsetY = new int[mItemCount];
//		}
//		else
//		{
//			mItemOffsetY = null;
//		}
//		for (int i = 0; i < mItemCount; ++i) {
//			View view = getAdapter().getView(i, null, this);
//			view.measure(
//					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
//					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//			mItemOffsetY[i] = mHeight;
//			mHeight += view.getMeasuredHeight();
//			System.out.println(mHeight);
//		}
//		scrollIsComputed = true;
//	}
//
//	public boolean scrollYIsComputed() {
//		return scrollIsComputed;
//	}

	public int getComputedScrollY() {
		mHeight = 0;
		int pos, nScrollY = 0, nItemY;
		View view = null;
		pos = getFirstVisiblePosition();
		view = getChildAt(0);
		int childCount = this.getChildCount();

		if(childCount>0)
		{
			mItemOffsetY = new int[childCount];
			for (int i = 0; i < childCount; ++i) {
				View child = getAdapter().getView(i, null, this);
				child.measure(
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				mItemOffsetY[i] = mHeight;
				mHeight += child.getMeasuredHeight();
			}
		}
		else
		{
			mItemOffsetY = null;
		}
		
		if(view != null && mItemOffsetY!=null)
		{
			nItemY = view.getTop();
			nScrollY = mItemOffsetY[0] - nItemY;
		}
		return nScrollY;
	}
}
