package com.tweetco.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.Registration;
import com.microsoft.windowsazure.mobileservices.RegistrationCallback;
import com.tweetco.R;
import com.tweetco.tweets.TweetCommonData;
import com.yagnasri.displayingbitmaps.ui.AllInOneActivity;

public class PushNotificationHandler extends com.microsoft.windowsazure.notifications.NotificationsHandler
{
	public static final String TAG = "PushNotificationHandler";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context ctx;

	/**
	 * This gets called in a background thread. No need to create another async task in this function.
	 */
	@Override
	public void onRegistered(Context context,  final String gcmRegistrationId) {
		super.onRegistered(context, gcmRegistrationId);

		TweetCommonData.mClient.getPush().register(gcmRegistrationId, null,new RegistrationCallback() {

			@Override
			public void onRegister(Registration registration, Exception exception) 
			{
				if(exception==null)
				{
					Log.d(TAG, "User device is successfully registered to receive push notifications");
					Log.d(TAG, "Registration Id="+registration.getRegistrationId());
					Log.d(TAG, "PNS Handle ="+registration.getPNSHandle());
				}
				else
				{
					exception.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onReceive(Context context, Bundle bundle) 
	{
		ctx = context;
		String nhMessage = bundle.getString("message");

		sendNotification(nhMessage);
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager)
				ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				new Intent(ctx, AllInOneActivity.class), 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(ctx)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("Notification Hub Demo")
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(msg))
		.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}