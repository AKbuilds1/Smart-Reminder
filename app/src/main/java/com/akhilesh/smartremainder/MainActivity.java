package com.akhilesh.smartremainder;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout reminderContainer;
    private TextView tvSubtitle;
    
    private final String[] dynamicWords = {
            "organized",
            "healthy",
            "systematic"
    };
    private int currentWordIndex = 0;
    private final Handler animationHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvSubtitle = findViewById(R.id.tvSubtitle);
        startTypingAnimation();

        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        100
                );
            }
        }

        databaseHelper =
                new DatabaseHelper(this);

        reminderContainer =
                findViewById(
                        R.id.reminderContainer
                );

        ExtendedFloatingActionButton addReminderButton =
                findViewById(
                        R.id.btnAddReminder
                );

        addReminderButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AddReminderActivity.class
                    );

            startActivity(intent);
        });

        loadReminders();
    }

    private void startTypingAnimation() {
        String prefix = "Keep your life ";
        String wordToType = dynamicWords[currentWordIndex];
        
        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i <= wordToType.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                String currentText = prefix + wordToType.substring(0, index);
                SpannableString spannable = new SpannableString(currentText);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), prefix.length(), currentText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvSubtitle.setText(spannable);
                
                if (index == wordToType.length()) {
                    animationHandler.postDelayed(() -> {
                        currentWordIndex = (currentWordIndex + 1) % dynamicWords.length;
                        startTypingAnimation();
                    }, 3000);
                }
            }, (long) i * 150);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            loadReminders();
        }
    }

    private void loadReminders() {

        reminderContainer.removeAllViews();

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

        Cursor cursor =
                db.query(
                        DatabaseHelper.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        DatabaseHelper.COLUMN_ID + " DESC"
                );

        if (cursor.getCount() == 0) {

            TextView emptyMessage =
                    new TextView(this);

            emptyMessage.setText(
                    "No reminders yet"
            );

            emptyMessage.setTextSize(17);
            emptyMessage.setTextColor(
                    Color.BLACK
            );

            emptyMessage.setGravity(
                    Gravity.CENTER
            );

            emptyMessage.setPadding(
                    0,
                    100,
                    0,
                    0
            );

            reminderContainer.addView(
                    emptyMessage
            );

        } else {

            while (cursor.moveToNext()) {

                long id =
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_ID
                                )
                        );

                String purpose =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_PURPOSE
                                )
                        );

                String time =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_TIME
                                )
                        );

                String date =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_DATE
                                )
                        );

                String repeat =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_REPEAT
                                )
                        );

                String status =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_STATUS
                                )
                        );

                addReminderCard(
                        id,
                        purpose,
                        time,
                        date,
                        repeat,
                        status
                );
            }
        }

        cursor.close();
        db.close();
    }

    private void addReminderCard(
            long id,
            String purpose,
            String time,
            String date,
            String repeat,
            String status) {

        MaterialCardView card = new MaterialCardView(this);
        card.setCardElevation(0f);
        card.setRadius(80f); // More oval shape
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeColor(Color.BLACK);
        card.setStrokeWidth(4);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(64, 48, 64, 48); // Increased horizontal padding for oval

        // Purpose
        TextView purposeText = new TextView(this);
        purposeText.setText(purpose);
        purposeText.setTextSize(22);
        purposeText.setTextColor(Color.BLACK);
        purposeText.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));

        // Date & Time
        TextView dateTimeText = new TextView(this);
        dateTimeText.setText((date != null ? date : "Today") + " • " + time);
        dateTimeText.setTextSize(14);
        dateTimeText.setTextColor(Color.BLACK);
        dateTimeText.setAlpha(0.6f);
        dateTimeText.setPadding(0, 8, 0, 16);
        dateTimeText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        // Repeat & Status Layout
        LinearLayout statusLayout = new LinearLayout(this);
        statusLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView repeatText = new TextView(this);
        repeatText.setText(repeat.toUpperCase());
        repeatText.setTextSize(12);
        repeatText.setTextColor(Color.WHITE);
        repeatText.setBackgroundColor(Color.BLACK);
        repeatText.setPadding(24, 8, 24, 8); // Padded for rounded look
        repeatText.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));

        TextView statusText = new TextView(this);
        statusText.setText(" " + status.toUpperCase());
        statusText.setTextSize(12);
        statusText.setTextColor(Color.BLACK);
        statusText.setPadding(16, 4, 16, 4);
        statusText.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));

        statusLayout.addView(repeatText);
        statusLayout.addView(statusText);

        // Spacer
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                48
        );
        spacer.setLayoutParams(spacerParams);

        // Delete button
        MaterialButton deleteButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        deleteButton.setText("DELETE");
        deleteButton.setTextColor(Color.BLACK);
        deleteButton.setStrokeColor(ColorStateList.valueOf(Color.BLACK));
        deleteButton.setStrokeWidth(2);
        deleteButton.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_delete));
        deleteButton.setIconTint(ColorStateList.valueOf(Color.BLACK));
        deleteButton.setCornerRadius(100); // Oval delete button
        deleteButton.setOnClickListener(v -> showDeleteConfirmation(id, purpose));

        innerLayout.addView(purposeText);
        innerLayout.addView(dateTimeText);
        innerLayout.addView(statusLayout);
        innerLayout.addView(spacer);
        innerLayout.addView(deleteButton);

        card.addView(innerLayout);
        reminderContainer.addView(card);
    }

    private void showDeleteConfirmation(
            long id,
            String purpose) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder?")
                .setMessage("Are you sure you want to delete \"" + purpose + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteReminder(id, purpose))
                .show();
    }

    private void deleteReminder(
            long id,
            String purpose) {

        databaseHelper.deleteReminder(id);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, ReminderReceiver.class);
        alarmIntent.putExtra("purpose", purpose);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                this,
                (int) id,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(alarmPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel((int) id);

        loadReminders();
    }
}