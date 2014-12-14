package com.tweetco.utility;

import android.app.Activity;
import android.view.View;

public class UiUtility 
{
	/**
     * Same as {@link Activity#findViewById}, but crashes if there's no view.
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getView(Activity parent, int viewId) {
        return (T) parent.findViewById(viewId);
    }
    
	/**
     * Same as {@link View#findViewById}, but crashes if there's no view.
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getView(View parent, int viewId) {
        return (T) parent.findViewById(viewId);
    }

    private static View checkView(View v) {
        if (v == null) {
            throw new IllegalArgumentException("View doesn't exist");
        }
        return v;
    }
}
