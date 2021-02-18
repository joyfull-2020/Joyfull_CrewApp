package jp.co.zynas.work_schedule.sp.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

/**
 * FCMから送信されたメッセージを受信するサービス
 */
public class MyMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyMessagingService";

    public static final String ACTION_TOKEN_REFRESHED = "MyInstanceIdService.ACTION_TOKEN_REFRESHED";
    public static final String KEY_TOKEN = "MyInstanceIdService.KEY_TOKEN";

    /**
     * FCMメッセージを受信した際に呼び出されるコールバック
     *
     * @param remoteMessage FCMメッセージを保持するオブジェクト
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // 通知メッセージの受信
        Map<String, String> data = remoteMessage.getData();
        //RemoteMessage.Notification notification = remoteMessage.getNotification();

        if (data != null /*&& notification != null*/) {

            wakeUp();

            //String title = notification.getTitle();
            //String body = notification.getBody();
            String title = data.get("title");
            String body = data.get("body");

            String contents = data.get("contents");
            String type = data.get("apptype");

            sendNotification(type, title, body, contents);
        }
    }

    @Override
    public void onNewToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            android.util.Log.d("FCM-TEST", "token = " + token);

            // ブロードキャストレシーバーでActivityに制御を戻す
            Intent intent = new Intent();
            intent.setAction(ACTION_TOKEN_REFRESHED);
            intent.putExtra(KEY_TOKEN, token);
            getBaseContext().sendBroadcast(intent);

            sendRegistrationToServer(token);
        }
    }

    private void sendRegistrationToServer(String token) {
        if (DataHelper.isLoggedIn(getApplicationContext())) {
            ConnectionHelper.registDevice("", DataHelper.getUUID(getApplicationContext()), DataHelper.getUserLogin(getApplicationContext()), DataHelper.getModelName(), token);
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(String type, String title, String body, String contents) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.PUSH_NOTIFICATION_ACTION);
        intent.putExtra(MainActivity.ARG_CONTENTS, contents);
        // create uniqe id
        String appTime = String.valueOf(new Date().getTime());
        int appWidgetId = Integer.valueOf(appTime.substring(appTime.length() - 5));
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_stat_ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        //.setVibrate(new long[] {0, 1000})
                        //.setSound(defaultSoundUri)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setColor(getColor(R.color.colorAccent))
                        .setContentIntent(contentIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // create uniqe id
        String time = String.valueOf(new Date().getTime());
        int notificationId = Integer.valueOf(time.substring(time.length() - 5));

        notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());
    }

    private void wakeUp() {
        PowerManager.WakeLock wakelock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "disableLock");
        wakelock.acquire(5000);
    }

}
