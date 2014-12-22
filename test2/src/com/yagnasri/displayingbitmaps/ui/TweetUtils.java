package com.yagnasri.displayingbitmaps.ui;

import android.text.TextUtils;

public class TweetUtils
{
	public static boolean isStringPresent(String commaSeparatedString, String stringToSearch)
	{
		if(TextUtils.isEmpty(stringToSearch.trim()))
		{
			throw new IllegalArgumentException("String to search is empty");
		}
		if(commaSeparatedString!=null)
		{
			String[] stringArray = commaSeparatedString.split(";");
	
			for(String str:stringArray)
			{
				if(!TextUtils.isEmpty(str.trim()))
				{
					return stringToSearch.trim().equalsIgnoreCase(str.trim());
				}
			}
		}

		return false;
	}
}


