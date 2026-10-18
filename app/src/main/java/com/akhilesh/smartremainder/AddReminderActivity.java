package com.akhilesh.smartremainder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private TextInputEditText etPurpose;
    private MaterialButton btnSelectDate;
    private MaterialButton btnSelectTime;
    private MaterialButton btnSaveReminder;

    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseHelper = new DatabaseHelper(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_reminder);

        etPurpose = findViewById(R.id.etPurpose);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSaveReminder = findViewById(R.id.btnSaveReminder);

        // Repeat dropdown
        AutoCompleteTextView spinnerRepeat = findViewById(R.id.spinnerRepeat);

        String[] repeatOptions = {
                "Once",
                "Every Day",
                "Every Week"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                repeatOptions
        );

        spinnerRepeat.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);

        // Date picker
        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;

                        String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                        btnSelectDate.setText(date);
                    },
                    selectedYear,
                    selectedMonth,
                    selectedDay
            );
            datePickerDialog.show();
        });

        // Time picker
        btnSelectTime.setOnClickListener(v -> {

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AddReminderActivity.this,
                    (view, hourOfDay, minute) -> {

                        selectedHour = hourOfDay;
                        selectedMinute = minute;

                        String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                        int displayHour = hourOfDay % 12;
                        if (displayHour == 0) displayHour = 12;

                        String time = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
                        btnSelectTime.setText(time);

                    },
                    selectedHour,
                    selectedMinute,
                    false
            );

            timePickerDialog.show();
        });

        // Save button
        btnSaveReminder.setOnClickListener(v -> {

            String purpose = "";
            if (etPurpose.getText() != null) {
                purpose = etPurpose.getText().toString().trim();
            }

            if (purpose.isEmpty()) {
                etPurpose.setError("Enter a purpose");
                return;
            }

            if (btnSelectDate.getText().toString().equals("Select Date")) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (btnSelectTime.getText().toString().equals("Select Time")) {
                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            String date = btnSelectDate.getText().toString();
            String time = btnSelectTime.getText().toString();
            String repeat = spinnerRepeat.getText().toString();

            long reminderId = databaseHelper.addReminder(
                    purpose,
                    time,
                    date,
                    repeat
            );

            if (reminderId != -1) {

                scheduleReminder(
                        reminderId,
                        purpose,
                        selectedYear,
                        selectedMonth,
                        selectedDay,
                        selectedHour,
                        selectedMinute
                );

                Toast.makeText(this, "Reminder saved!", Toast.LENGTH_SHORT).show();
                finish();

            } else {
                Toast.makeText(this, "Failed to save reminder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleReminder(
            long reminderId,
            String purpose,
            int year,
            int month,
            int day,
            int hour,
            int minute) {

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent =
                new Intent(
                        this,
                        ReminderReceiver.class
                );

        intent.putExtra("reminderId", reminderId);
        intent.putExtra("purpose", purpose);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        (int) reminderId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // Adjust to future if needed, but user has control via DatePicker.
        }

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}