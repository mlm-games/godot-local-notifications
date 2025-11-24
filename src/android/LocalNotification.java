package vn.kyoz.godot.localnotification;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Calendar;
import java.util.Set;
import java.util.Collections;

public class LocalNotification extends GodotPlugin {
    private static final String TAG = "GodotLocalNotification";
    private static final int REQUEST_CODE = 6969;

    public LocalNotification(Godot godot) {
        super(godot);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "LocalNotification";
    }

    @NonNull
    @Override
    public Set<String> getPluginMethods() {
        return Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                "isPermissionGranted",
                "requestPermission",
                "openAppSetting",
                "show",
                "showRepeating",
                "showDaily",
                "cancel"
            ))
        );
    }

    @Override
    public void onMainRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
                    emitSignalFromJava("on_permission_request_completed");
                    break;
                }
            }
        }
    }

    @UsedByGodot
    public boolean isPermissionGranted() {
        Log.d(TAG, "isPermissionGranted()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @UsedByGodot
    public void requestPermission() {
        Log.d(TAG, "requestPermission()");

        if (isPermissionGranted()) {
            emitSignalFromJava("on_permission_request_completed");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(getActivity(), 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    REQUEST_CODE);
        } else {
            emitSignalFromJava("on_permission_request_completed");
        }
    }

    @UsedByGodot
    public void openAppSetting() {
        Log.d(TAG, "openAppSetting()");

        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings: " + e.getMessage());
        }
    }

    @UsedByGodot
    public void show(String title, String message, int interval, int tag) {
        Log.d(TAG, "show(" + title + "," + message + "," + Integer.toString(interval) + "," + Integer.toString(tag) + ")");

        if (interval <= 0) {
            return;
        }

        PendingIntent sender = getPendingIntent(title, message, tag);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, interval);

        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
    }

    @UsedByGodot
    public void showRepeating(String title, String message, int interval, int repeat_interval, int tag) {
        Log.d(TAG, "showRepeating(" + title + "," + message + "," + Integer.toString(interval) +
                "," + Integer.toString(repeat_interval) + "," + Integer.toString(tag) + ")");

        if (interval <= 0 || repeat_interval <= 0) {
            return;
        }

        PendingIntent sender = getPendingIntent(title, message, tag);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, interval);

        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeat_interval * 1000, sender);
    }

    @UsedByGodot
    public void showDaily(String title, String message, int at_hour, int at_minute, int tag) {
        Log.d(TAG, "showDaily(" + title + "," + message + "," + 
              Integer.toString(at_hour) + "," + Integer.toString(at_minute) + "," + 
              Integer.toString(tag) + ")");

        if (at_hour < 0 || at_hour > 23 || at_minute < 0 || at_minute > 59) {
            return;
        }

        PendingIntent sender = getPendingIntent(title, message, tag);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, at_hour);
        calendar.set(Calendar.MINUTE, at_minute);
        calendar.set(Calendar.SECOND, 0);
        
        // If time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 
                AlarmManager.INTERVAL_DAY, sender);
    }

    @UsedByGodot
    public void cancel(int tag) {
        Log.d(TAG, "cancel(" + Integer.toString(tag) + ")");

        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        PendingIntent sender = getPendingIntent("", "", tag);
        am.cancel(sender);
    }

    private PendingIntent getPendingIntent(String title, String message, int tag) {
        Intent i = new Intent(getActivity().getApplicationContext(), LocalNotificationReceiver.class);
        i.putExtra("title", title);
        i.putExtra("message", message);
        i.putExtra("notification_id", tag);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        return PendingIntent.getBroadcast(getActivity(), tag, i, flags);
    }
    
    private void emitSignalFromJava(String signalName) {
        try {
            emitSignal(signalName);
        } catch (Exception e) {
            Log.e(TAG, "Error emitting signal: " + e.getMessage());
        }
    }
}