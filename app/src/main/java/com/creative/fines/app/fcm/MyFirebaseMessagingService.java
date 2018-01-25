package com.creative.fines.app.fcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.creative.fines.app.R;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.menu.LoginActivity;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.UtilClass;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by GS on 2016-11-10.
 */
public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    private Intent intent;

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        UtilClass.logD("FCM", "From: " + remoteMessage.getData());

        ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> rList = am.getRunningAppProcesses();
        String appPackage = rList.get(0).processName;

        UtilClass.logD(TAG, "appPackage="+appPackage+", onAppCheck="+ MainFragment.onAppCheck);

        if(MainFragment.onAppCheck){    //이미 로그인 후 사용중
            intent = new Intent(this, FragMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if(MainFragment.pendingPath.equals("패널티카드")){
                intent.putExtra("title", MainFragment.pendingPath+"상세");

            }else if(MainFragment.pendingPath.equals("동료사랑카드")){
                intent.putExtra("title", MainFragment.pendingPath+"상세");

            }else{
                intent.putExtra("title","공지사항");
            }

        }else{
            UtilClass.logD(TAG,"왜에러가나냐");
            intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        //푸시 종류
        sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"));
    }

    private void sendNotification(String title, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setTicker(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                //.setNumber(1)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public void sendNotificationLine(String title, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setTicker(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                .addLine("M.Twain (Google+) Haiku is more than a cert...")
                .addLine("M.Twain Reminder")
                .addLine("M.Twain Lunch?")
                .addLine("M.Twain Revised Specs")
                .addLine("M.Twain ")
                .addLine("Google Play Celebrate 25 billion apps with Goo..")
                .addLine("Stack Exchange StackOverflow weekly Newsl...")
                .setBigContentTitle("6 new message")
                .setSummaryText("mtwain@android.com");

        mBuilder.setStyle(style);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;

        // Send the notification.
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);

    }

    public void sendNotificationImage(String title, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setTicker(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        try{
            URL url = new URL("http://이미지주소");
            URLConnection conn = url.openConnection();
            conn.connect();

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            Bitmap img = BitmapFactory.decodeStream(bis);

            NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle(mBuilder);
            style.bigPicture(img).setBigContentTitle(title);
            mBuilder.setStyle(style);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"이미지 URL 에러.", Toast.LENGTH_SHORT).show();
        }

        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;

        // Send the notification.
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);

    }
}

