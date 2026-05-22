package com.classpulse.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.classpulse.R;
import com.classpulse.activities.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, mainIntent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, "classpulse_reminder")
                .setSmallIcon(R.drawable.ic_nav_logs)
                .setContentTitle("Don't forget to log! 📚")
                .setContentText("Tap to record today's class in ClassPulse.")
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(1001, builder.build());
    }
}