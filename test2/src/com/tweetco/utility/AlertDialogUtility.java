package com.tweetco.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class AlertDialogUtility 
{
	public static AlertDialog getAlertDialogOK(Context context, String message, OnClickListener onOKClickListner)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setMessage(message);
		if(onOKClickListner != null)
		{
			builder.setPositiveButton("OK", onOKClickListner);
		}
		else
		{
			builder.setPositiveButton("OK", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			});
		}
		
		return builder.create();
	}
}
