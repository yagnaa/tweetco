

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tweetco.activities;

import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;


public class CustomClickableSpan extends ClickableSpan implements ParcelableSpan {

    private final String mString;
    
    public static final String TAG_REGEX = "\\S*#(?:\\[[^\\]]+\\]|\\S+)";
    public static final String HANDLE_REGEX = "\\S*@(?:\\[[^\\]]+\\]|\\S+)";
    
	public static final Pattern TAG_PATTERN = 
			   Pattern.compile("\\S*#(?:\\[[^\\]]+\\]|\\S+)");
	
	public static final Pattern HANDLE_PATTERN = 
			   Pattern.compile("\\S*@(?:\\[[^\\]]+\\]|\\S+)");
	
	

    public CustomClickableSpan(String url) {
    	mString = url;
    }

    public CustomClickableSpan(Parcel src) {
    	mString = src.readString();
    }
    
    @Override
    public int getSpanTypeId() 
    {
        return 100;
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mString);
    }

    public String getString() {
        return mString;
    }

    @Override
    public void onClick(View widget) 
    {
    	Context context = widget.getContext();
    	if(mString.matches(HANDLE_REGEX))
    	{
        	
        	String temp = mString.substring(1, mString.length()); 
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra(Constants.USERNAME_STR, temp);
            context.startActivity(intent);
    	}
    	else if(mString.matches(TAG_REGEX))
    	{
    		String temp = mString.substring(1, mString.length());
    		Intent intent = new Intent(context, TrendingFragmentActivity.class);
            intent.putExtra(Constants.TREND_TAG_STR, temp);
            context.startActivity(intent);
    	}
    	else
    	{
    		Log.e("CustomClickableSpan","Unable to handle regex for "+mString);
    	}
    }
}
