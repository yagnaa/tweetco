package com.yagnasri.displayingbitmaps.ui;

import android.text.TextUtils;

public class TweetUtils
{
	public static boolean isStringPresent(String commaSeparatedString, String stringToSearch)
	{
		boolean bStringPresent = false;
		if(TextUtils.isEmpty(stringToSearch) || TextUtils.isEmpty(stringToSearch.trim()))
		{
			throw new IllegalArgumentException("String to search is empty");
		}
		if(!TextUtils.isEmpty(commaSeparatedString))
		{
			stringToSearch = stringToSearch.trim();
			String[] stringArray = commaSeparatedString.split(";");
			String tempStr;
			for(String str:stringArray)
			{
				tempStr = str.trim();
				if(!TextUtils.isEmpty(tempStr) && stringToSearch.equalsIgnoreCase(tempStr))
				{
					bStringPresent = true;
					break;
				}
			}
		}

		return bStringPresent;
	}
}


