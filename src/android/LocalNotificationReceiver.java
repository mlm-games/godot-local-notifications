package vn.kyoz.godot.localnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class LocalNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "GodotLNReceiver";
    public static final String NOTIFICATION_CHANNEL_ID = "godot_local_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra("notification_id", 0);

        Log.i(TAG, "Received notification: " + message);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, 
                    "Godot Local Notifications", 
                    importance);
            notificationChannel.setShowBadge(true);
            notificationChannel.enableVibration(true);
            notificationChannel.enableLights(true);
            manager.createNotificationChannel(notificationChannel);
        }

        // Find the main activity class
        Class<?> appClass = null;
        try {
            appClass = Class.forName("com.godot.game.GodotApp");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed to find GodotApp class: " + e.getMessage());
            return;
        }

        // Create intent to open the app when notification is clicked
        Intent launchIntent = new Intent(context, appClass);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                Intent.FLAG_ACTIVITY_SINGLE_TOP | 
                Intent.FLAG_ACTIVITY_NEW_TASK);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                notificationId, 
                launchIntent, 
                flags);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        // Get app icon for notification
        int iconID = context.getResources().getIdentifier("icon", "mipmap", context.getPackageName());
        int notificationIconID = context.getResources().getIdentifier("notification_icon", "mipmap", context.getPackageName());
        
        // Use custom notification icon if available, otherwise use app icon
        if (notificationIconID <= 0) {
            builder.setSmallIcon(iconID);
        } else {
            builder.setSmallIcon(notificationIconID);
        }
        
        // Set large icon (app icon)
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconID);
        builder.setLargeIcon(largeIcon);
        
        // Set badge icon type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
        }

        // Get custom notification color if available
        int colorID = context.getResources()
                .getIdentifier("notification_color", "color", context.getPackageName());

        if (colorID <= 0) {
            builder.setColor(Color.BLACK);
        } else {
            builder.setColor(context.getResources().getColor(colorID));
        }

        // Configure notification
        builder.setShowWhen(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setTicker(message);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setColorized(true);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        
        // Add vibration pattern
        builder.setVibrate(new long[] { 0, 250, 250, 250 });
        
        // Show the notification
        Notification notification = builder.build();
        manager.notify(notificationId, notification);
    }
}