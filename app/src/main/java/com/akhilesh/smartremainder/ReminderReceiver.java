package com.akhilesh.smartremainder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID =
            "reminder_alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        long reminderId =
                intent.getLongExtra("reminderId", -1);

        String purpose =
                intent.getStringExtra("purpose");

        if (purpose == null) {
            purpose = "Reminder";
        }

        // Vibrate phone
        vibratePhone(context);

        // Show notification
        showNotification(context, reminderId, purpose);

        // Schedule another reminder after 10 minutes
        scheduleAgain(context, reminderId, purpose);
    }

    // =====================================================
    // SHOW NOTIFICATION
    // =====================================================

    private void showNotification(
            Context context,
            long reminderId,
            String purpose) {

        createNotificationChannel(context);

        // -------------------------
        // COMPLETED BUTTON
        // -------------------------

        Intent completedIntent =
                new Intent(
                        context,
                        ReminderActionReceiver.class
                );

        completedIntent.setAction("COMPLETED");

        completedIntent.putExtra(
                "reminderId",
                reminderId
        );

        completedIntent.putExtra(
                "purpose",
                purpose
        );

        PendingIntent completedPendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        (int) reminderId + 1,
                        completedIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        // -------------------------
        // IN PROGRESS BUTTON
        // -------------------------

        Intent progressIntent =
                new Intent(
                        context,
                        ReminderActionReceiver.class
                );

        progressIntent.setAction("IN_PROGRESS");

        progressIntent.putExtra(
                "reminderId",
                reminderId
        );

        progressIntent.putExtra(
                "purpose",
                purpose
        );

        PendingIntent progressPendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        (int) reminderId + 2,
                        progressIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        // -------------------------
        // NOTIFICATION
        // -------------------------

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )

                        .setSmallIcon(
                                R.drawable.ic_launcher_foreground
                        )

                        .setContentTitle(
                                "Smart Reminder"
                        )

                        .setContentText(
                                purpose
                        )

                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )

                        .setDefaults(
                                Notification.DEFAULT_SOUND |
                                        Notification.DEFAULT_VIBRATE
                        )

                        .setCategory(
                                NotificationCompat.CATEGORY_ALARM
                        )

                        .setOngoing(true)

                        .setAutoCancel(false)

                        .addAction(
                                0,
                                "Completed",
                                completedPendingIntent
                        )

                        .addAction(
                                0,
                                "In Progress",
                                progressPendingIntent
                        );

        NotificationManager notificationManager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        notificationManager.notify(
                (int) reminderId,
                builder.build()
        );
    }

    // =====================================================
    // 10 MINUTE REPEAT
    // =====================================================

    private void scheduleAgain(
            Context context,
            long reminderId,
            String purpose) {

        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.putExtra(
                "reminderId",
                reminderId
        );

        intent.putExtra(
                "purpose",
                purpose
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        (int) reminderId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        long tenMinutes =
                10 * 60 * 1000L;

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis()
                        + tenMinutes,
                pendingIntent
        );
    }

    // =====================================================
    // DIRECT PHONE VIBRATION
    // =====================================================

    private void vibratePhone(Context context) {

        long[] pattern = {
                0,
                600,
                300,
                600
        };

        // Android 12 and newer
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S) {

            VibratorManager vibratorManager =
                    (VibratorManager)
                            context.getSystemService(
                                    Context.VIBRATOR_MANAGER_SERVICE
                            );

            Vibrator vibrator =
                    vibratorManager.getDefaultVibrator();

            vibrator.vibrate(
                    VibrationEffect.createWaveform(
                            pattern,
                            -1
                    )
            );

        }

        // Android 8 - Android 11
        else if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            Vibrator vibrator =
                    (Vibrator)
                            context.getSystemService(
                                    Context.VIBRATOR_SERVICE
                            );

            vibrator.vibrate(
                    VibrationEffect.createWaveform(
                            pattern,
                            -1
                    )
            );

        }

        // Older Android
        else {

            Vibrator vibrator =
                    (Vibrator)
                            context.getSystemService(
                                    Context.VIBRATOR_SERVICE
                            );

            vibrator.vibrate(
                    pattern,
                    -1
            );
        }
    }

    // =====================================================
    // NOTIFICATION CHANNEL
    // =====================================================

    private void createNotificationChannel(
            Context context) {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            Uri soundUri =
                    Settings.System
                            .DEFAULT_ALARM_ALERT_URI;

            AudioAttributes audioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(
                                    AudioAttributes.USAGE_ALARM
                            )
                            .setContentType(
                                    AudioAttributes
                                            .CONTENT_TYPE_SONIFICATION
                            )
                            .build();

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Reminder Alarms",
                            NotificationManager
                                    .IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Alarm notifications for Smart Reminder"
            );

            channel.setSound(
                    soundUri,
                    audioAttributes
            );

            channel.enableVibration(true);

            long[] vibrationPattern = {
                    0,
                    600,
                    300,
                    600
            };

            channel.setVibrationPattern(
                    vibrationPattern
            );

            NotificationManager manager =
                    context.getSystemService(
                            NotificationManager.class
                    );

            manager.createNotificationChannel(
                    channel
            );
        }
    }
}