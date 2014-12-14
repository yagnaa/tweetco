package com.tweetco.activities.progress;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;

public class ProgressDialogUtil 
{
	public static ProgressDialog createProgressDialog(Context c, String message,OnCancelListener onCancelListener,boolean isCancellable)
    {
       ProgressDialog pd = new ProgressDialog(c);
       pd.setIndeterminate(true);
       pd.setMessage(message);
       pd.setCancelable(isCancellable);
       pd.setOnCancelListener(onCancelListener);
       return pd;
    }
	
	public static ProgressDialog createProgressDialog(Context c, String message,OnCancelListener onCancelListener,boolean isCancellable, boolean isCancellableOnTouchOutside)
    {
		ProgressDialog pd = createProgressDialog(c, message, onCancelListener, isCancellable);
		pd.setCanceledOnTouchOutside(isCancellableOnTouchOutside);
		return pd;
    }
}
