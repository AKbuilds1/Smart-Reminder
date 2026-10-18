package com.akhilesh.smartremainder;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long reminderId =
                intent.getLongExtra("reminderId", -1);

        String purpose =
                intent.getStringExtra("purpose");

        if (reminderId == -1) {
            return;
        }

        String action = intent.getAction();

        DatabaseHelper databaseHelper =
                new DatabaseHelper(context);

        if ("COMPLETED".equals(action)) {

            databaseHelper.updateStatus(
                    reminderId,
                    "Completed"
            );

        } else if ("IN_PROGRESS".equals(action)) {

            databaseHelper.updateStatus(
                    reminderId,
                    "In Progress"
            );
        }

        databaseHelper.close();

        // Stop the repeating alarm
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        Intent alarmIntent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        alarmIntent.putExtra("reminderId", reminderId);
        alarmIntent.putExtra("purpose", purpose);

        PendingIntent alarmPendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        (int) reminderId,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        alarmManager.cancel(alarmPendingIntent);

        // Remove notification
        NotificationManager notificationManager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        notificationManager.cancel(
                (int) reminderId
        );
    }
}