package com.example.twovideo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2017/10/28.
 */

public class NotificationBack {
	
	public static final String TAG = "NotificationBack";
    private Context mContext;
    private NotificationManager manage;
	
    public NotificationBack(Context mContext) {
        this.mContext = mContext;
        manage = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createNotification() {
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(mContext.getString(R.string.app_name) + mContext.getString(R.string.application_notification_title));
        builder.setContentText(mContext.getString(R.string.application_notification_content));
        builder.setOngoing(true);
        builder.setAutoCancel(true);
        builder.setContentIntent(getPendingIntent(PendingIntent.FLAG_ONE_SHOT));
        manage.notify(1, builder.build());
    }
	
	public void deleteNotification() {
		manage.cancel(1);
	}

    private PendingIntent getPendingIntent(int flags) {
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent, flags);
        return pendingIntent;
    }

	
}
