package com.classpulse.fragments;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.classpulse.R;
import com.classpulse.activities.AddEditSubjectActivity;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.ClassLog;
import com.classpulse.utils.PrefsManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private TextView tvCurrentName, tvCurrentBirthday, tvCurrentSchool, tvCurrentYear;
    private TextView tvReminderTime;
    private Switch switchReminder;
    private View rowReminderTime;
    private PrefsManager prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new PrefsManager(requireContext());

        // Bind views
        tvCurrentName     = view.findViewById(R.id.tv_current_name);
        tvCurrentBirthday = view.findViewById(R.id.tv_current_birthday);
        tvCurrentSchool   = view.findViewById(R.id.tv_current_school);
        tvCurrentYear     = view.findViewById(R.id.tv_current_year);
        tvReminderTime    = view.findViewById(R.id.tv_reminder_time);
        switchReminder    = view.findViewById(R.id.switch_reminder);
        rowReminderTime   = view.findViewById(R.id.row_reminder_time);

        // Back button
        view.findViewById(R.id.btn_back_settings).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Load current values
        loadCurrentValues();

        // Account rows
        view.findViewById(R.id.row_edit_name).setOnClickListener(v ->
                showEditDialog("Name", prefs.getUserName(), value -> {
                    prefs.setUserName(value);
                    tvCurrentName.setText(value);
                }));

        view.findViewById(R.id.row_edit_birthday).setOnClickListener(v ->
                showEditDialog("Birthday", prefs.getBirthday(), value -> {
                    prefs.setBirthday(value);
                    tvCurrentBirthday.setText(value);
                }));

        view.findViewById(R.id.row_edit_school).setOnClickListener(v ->
                showEditDialog("School", prefs.getSchool(), value -> {
                    prefs.setSchool(value);
                    tvCurrentSchool.setText(value);
                }));

        view.findViewById(R.id.row_edit_year).setOnClickListener(v ->
                showEditDialog("Year Level", prefs.getYearLevel(), value -> {
                    prefs.setYearLevel(value);
                    tvCurrentYear.setText(value);
                }));

        // Manage subjects
        view.findViewById(R.id.btn_manage_subjects).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditSubjectActivity.class)));

        // Notifications
        boolean reminderOn = prefs.isReminderOn();
        switchReminder.setChecked(reminderOn);
        rowReminderTime.setVisibility(reminderOn ? View.VISIBLE : View.GONE);
        tvReminderTime.setText(prefs.getReminderTime());

        switchReminder.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setReminderOn(checked);
            rowReminderTime.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (checked) scheduleReminder();
            else cancelReminder();
        });

        rowReminderTime.setOnClickListener(v -> showTimePicker());

        // Export CSV
        view.findViewById(R.id.btn_export_csv).setOnClickListener(v -> exportLogsAsCsv());

        // Delete logs
        view.findViewById(R.id.btn_delete_logs).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete All Logs")
                        .setMessage("Delete all class logs? Your subjects will be kept.")
                        .setPositiveButton("Delete", (d, w) -> executor.execute(() -> {
                            AppDatabase.getInstance(requireContext()).classLogDao().deleteAll();
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "All logs deleted.", Toast.LENGTH_SHORT).show());
                        }))
                        .setNegativeButton("Cancel", null)
                        .show());

        // Reset all data
        view.findViewById(R.id.btn_reset_data).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Reset All Data")
                        .setMessage("Delete ALL logs and subjects? This cannot be undone.")
                        .setPositiveButton("Reset", (d, w) -> executor.execute(() -> {
                            AppDatabase db = AppDatabase.getInstance(requireContext());
                            db.classLogDao().deleteAll();
                            db.subjectDao().deleteAll();
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "All data cleared.", Toast.LENGTH_SHORT).show());
                        }))
                        .setNegativeButton("Cancel", null)
                        .show());
    }

    private void loadCurrentValues() {
        tvCurrentName.setText(prefs.getUserName());
        tvCurrentBirthday.setText(prefs.getBirthday());
        tvCurrentSchool.setText(prefs.getSchool());
        tvCurrentYear.setText(prefs.getYearLevel());
        tvReminderTime.setText(prefs.getReminderTime());
    }

    private void showEditDialog(String field, String current, OnValueSaved callback) {
        EditText input = new EditText(requireContext());
        input.setText(current);
        input.setHint("Enter " + field);
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit " + field)
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (!value.isEmpty()) {
                        callback.onSaved(value);
                        Toast.makeText(requireContext(), field + " updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTimePicker() {
        String current = prefs.getReminderTime();
        int hour = 8, minute = 0;
        try {
            String[] parts = current.split(":");
            hour = Integer.parseInt(parts[0].trim());
            String[] minAmpm = parts[1].trim().split(" ");
            minute = Integer.parseInt(minAmpm[0]);
            if (minAmpm[1].equals("PM") && hour != 12) hour += 12;
            if (minAmpm[1].equals("AM") && hour == 12) hour = 0;
        } catch (Exception ignored) {}

        new TimePickerDialog(requireContext(), (tp, h, m) -> {
            String ampm = h < 12 ? "AM" : "PM";
            int h12 = h % 12;
            if (h12 == 0) h12 = 12;
            String timeStr = String.format(Locale.getDefault(), "%d:%02d %s", h12, m, ampm);
            prefs.setReminderTime(timeStr);
            tvReminderTime.setText(timeStr);
            scheduleReminder();
        }, hour, minute, false).show();
    }

    private void scheduleReminder() {
        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "classpulse_reminder", "Class Log Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager nm = requireContext().getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        // Parse time
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

        Intent intent = new Intent(requireContext(),
                com.classpulse.receivers.ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(requireContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (am != null)
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);

        Toast.makeText(requireContext(), "Reminder set!", Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder() {
        Intent intent = new Intent(requireContext(),
                com.classpulse.receivers.ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(requireContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pi);
        Toast.makeText(requireContext(), "Reminder cancelled.", Toast.LENGTH_SHORT).show();
    }

    private void exportLogsAsCsv() {
        executor.execute(() -> {
            List<ClassLog> logs = AppDatabase.getInstance(requireContext())
                    .classLogDao().getAllLogsSync();

            if (logs.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "No logs to export.", Toast.LENGTH_SHORT).show());
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            StringBuilder csv = new StringBuilder();
            csv.append("Date,Subject,Attendance,Participation,Mood,Notes\n");
            for (ClassLog l : logs) {
                csv.append(sdf.format(new Date(l.logDate))).append(",")
                        .append(clean(l.subjectName)).append(",")
                        .append(clean(l.attendance)).append(",")
                        .append(clean(l.participation)).append(",")
                        .append(clean(l.mood)).append(",")
                        .append(clean(l.notes)).append("\n");
            }

            try {
                File dir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                File file = new File(dir, "classpulse_logs_"
                        + new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                        .format(new Date()) + ".csv");
                FileWriter fw = new FileWriter(file);
                fw.write(csv.toString());
                fw.close();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Exported to Downloads: " + file.getName(),
                                Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Export failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String clean(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    interface OnValueSaved {
        void onSaved(String value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}