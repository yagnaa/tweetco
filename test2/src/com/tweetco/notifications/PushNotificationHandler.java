package com.tweetco.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.Registration;
import com.microsoft.windowsazure.mobileservices.RegistrationCallback;
import com.tweetco.activities.AllInOneActivity;
import com.tweetco.activities.TweetCoBaseActivity;
import com.tweetco.tweets.TweetCommonData;

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
		
		String tweeterName = bundle.getString("message");
		String tweetContent = bundle.getString("message2");

		sendNotification(tweeterName,tweetContent);
	}

	private void sendNotification(String tweeterName, String tweetContent) 
	{	
		Log.d(TAG, "Received a push Notification with tweetContent="+tweetContent);
		if(TweetCoBaseActivity.isAppInForeground)
		{
			if(TweetCoBaseActivity.topActivity instanceof AllInOneActivity)
			{
				((AllInOneActivity)TweetCoBaseActivity.topActivity).getController().tweetsListRefresh(null);
			}
		}
		else
		{			
//			mNotificationManager = (NotificationManager)
//					ctx.getSystemService(Context.NOTIFICATION_SERVICE);
//			Intent intent = new Intent(ctx, AllInOneActivity.class);
//			intent.putExtra(Constants.LAUNCHED_FROM_NOTIFICATIONS, true);
//			PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
//					intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
//
//			NotificationCompat.Builder mBuilder =
//					new NotificationCompat.Builder(ctx)
//			.setSmallIcon(R.drawable.icon_main)
//			.setContentTitle(tweeterName)
//			.setStyle(new NotificationCompat.BigTextStyle()
//			.bigText(tweetContent))
//			.setContentText(tweetContent);
//
//			mBuilder.setContentIntent(contentIntent);
//			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		}

	}

}