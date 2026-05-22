package com.classpulse.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import java.util.Locale;
import com.classpulse.utils.PrefsManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        PrefsManager prefs = new PrefsManager(context);
        if (!prefs.isReminderOn()) return;

        // Re-schedule the alarm after reboot
        String timeStr = prefs.getReminderTime();
        int hour = 8, minute = 0;
        try {
            String[] parts = timeStr.split(":");
            hour = Integer.parseInt(parts[0].trim());
            String[] minAmpm = parts[1].trim().split(" ");
            minute = Integer.parseInt(minAmpm[0]);
            if (minAmpm[1].equals("PM") && hour != 12) hour += 12;
            if (minAmpm[1].equals("AM") && hour == 12) hour = 0;
        } catch (Exception ignored) {}

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_MONTH, 1);

        Intent ri = new Intent(context, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, ri,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null)
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
}