package com.classpulse.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.classpulse.R;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;
import com.classpulse.utils.AnalyticsEngine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogReflectionActivity extends AppCompatActivity {

    private String selectedAttendance    = "Present";
    private String selectedParticipation = "Medium";
    private String selectedMood          = "Focused";
    private int    selectedSubjectId     = -1;
    private String selectedSubjectName   = "";
    private List<Subject> subjectList;

    private TextView    tvSubjectSubtitle;
    private View        cardSubjectSelector;
    private Spinner     spinnerSubject;
    private EditText    etNotes;

    private LinearLayout btnPresent, btnLate, btnAbsent;
    private LinearLayout btnLow, btnMedium, btnHigh;
    private LinearLayout moodFocused, moodNeutral, moodTired, moodStressed;

    private boolean launchedFromSchedule = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reflection);

        tvSubjectSubtitle   = findViewById(R.id.tv_subject_subtitle);
        cardSubjectSelector = findViewById(R.id.card_subject_selector);
        spinnerSubject      = findViewById(R.id.spinner_subject);
        etNotes             = findViewById(R.id.et_notes);

        btnPresent = findViewById(R.id.btn_present);
        btnLate    = findViewById(R.id.btn_late);
        btnAbsent  = findViewById(R.id.btn_absent);

        btnLow    = findViewById(R.id.btn_low);
        btnMedium = findViewById(R.id.btn_medium);
        btnHigh   = findViewById(R.id.btn_high);

        moodFocused  = findViewById(R.id.mood_focused);
        moodNeutral  = findViewById(R.id.mood_neutral);
        moodTired    = findViewById(R.id.mood_tired);
        moodStressed = findViewById(R.id.mood_stressed);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("subject_id")) {
            launchedFromSchedule = true;
            selectedSubjectId   = intent.getIntExtra("subject_id", -1);
            selectedSubjectName = intent.getStringExtra("subject_name") != null
                    ? intent.getStringExtra("subject_name") : "";
            SimpleDateFormat fmt = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            if (tvSubjectSubtitle != null)
                tvSubjectSubtitle.setText(selectedSubjectName + " · " + fmt.format(new Date()));
            if (cardSubjectSelector != null)
                cardSubjectSelector.setVisibility(View.GONE);
        } else {
            launchedFromSchedule = false;
            if (cardSubjectSelector != null)
                cardSubjectSelector.setVisibility(View.VISIBLE);
        }

        selectAttendance("Present");
        selectParticipation("Medium");
        selectMood("Focused");

        btnPresent.setOnClickListener(v -> selectAttendance("Present"));
        btnLate.setOnClickListener(v    -> selectAttendance("Late"));
        btnAbsent.setOnClickListener(v  -> selectAttendance("Absent"));

        btnLow.setOnClickListener(v    -> selectParticipation("Low"));
        btnMedium.setOnClickListener(v -> selectParticipation("Medium"));
        btnHigh.setOnClickListener(v   -> selectParticipation("High"));

        moodFocused.setOnClickListener(v  -> selectMood("Focused"));
        moodNeutral.setOnClickListener(v  -> selectMood("Neutral"));
        moodTired.setOnClickListener(v    -> selectMood("Tired"));
        moodStressed.setOnClickListener(v -> selectMood("Stressed"));

        findViewById(R.id.btn_save).setOnClickListener(v -> saveEntry());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        executor.execute(() -> {
            subjectList = AppDatabase.getInstance(this).subjectDao().getAllSubjectsSync();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (subjectList == null || subjectList.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("No Subjects Found")
                            .setMessage("Please add a subject first before logging a class.")
                            .setPositiveButton("Add Subject", (d, w) -> {
                                startActivity(new Intent(this, AddEditSubjectActivity.class));
                                finish();
                            })
                            .setNegativeButton("Go Back", (d, w) -> finish())
                            .setCancelable(false)
                            .show();
                } else if (!launchedFromSchedule) {
                    String[] names = new String[subjectList.size()];
                    for (int i = 0; i < subjectList.size(); i++) names[i] = subjectList.get(i).name;

                    // ── Fixed: use custom layouts so text is dark/visible ──
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                            this, R.layout.item_spinner_subject, names);
                    adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                    spinnerSubject.setAdapter(adapter);

                    selectedSubjectId   = subjectList.get(0).id;
                    selectedSubjectName = subjectList.get(0).name;

                    spinnerSubject.setOnItemSelectedListener(
                            new android.widget.AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(android.widget.AdapterView<?> p,
                                                           View v, int pos, long id) {
                                    selectedSubjectId   = subjectList.get(pos).id;
                                    selectedSubjectName = subjectList.get(pos).name;
                                }
                                @Override
                                public void onNothingSelected(android.widget.AdapterView<?> p) {}
                            });
                }
            });
        });
    }

    private void selectAttendance(String value) {
        selectedAttendance = value;
        resetAttendance(btnPresent);
        resetAttendance(btnLate);
        resetAttendance(btnAbsent);
        switch (value) {
            case "Present":
                activateCard(btnPresent, R.drawable.bg_attendance_present,
                        Color.parseColor("#2E7D52"), true); break;
            case "Late":
                activateCard(btnLate, R.drawable.bg_attendance_late,
                        Color.parseColor("#B45309"), true); break;
            case "Absent":
                activateCard(btnAbsent, R.drawable.bg_attendance_absent,
                        Color.parseColor("#B03030"), true); break;
        }
    }

    private void resetAttendance(LinearLayout card) {
        card.setBackgroundResource(R.drawable.bg_attendance_unselected);
        setCardLabelColor(card, Color.parseColor("#8E8E93"), false);
    }

    private void selectParticipation(String value) {
        selectedParticipation = value;
        resetParticipation(btnLow);
        resetParticipation(btnMedium);
        resetParticipation(btnHigh);
        switch (value) {
            case "Low":
                activateCard(btnLow, R.drawable.bg_participation_low,
                        Color.parseColor("#555555"), false); break;
            case "Medium":
                activateCard(btnMedium, R.drawable.bg_participation_medium,
                        Color.parseColor("#1A5FA8"), true); break;
            case "High":
                activateCard(btnHigh, R.drawable.bg_participation_high,
                        Color.parseColor("#2E7D52"), true); break;
        }
    }

    private void resetParticipation(LinearLayout card) {
        card.setBackgroundResource(R.drawable.bg_attendance_unselected);
        setCardLabelColor(card, Color.parseColor("#8E8E93"), false);
    }

    private void selectMood(String mood) {
        selectedMood = mood;
        resetMood(moodFocused);
        resetMood(moodNeutral);
        resetMood(moodTired);
        resetMood(moodStressed);
        LinearLayout target;
        switch (mood) {
            case "Neutral":  target = moodNeutral;  break;
            case "Tired":    target = moodTired;    break;
            case "Stressed": target = moodStressed; break;
            default:         target = moodFocused;  break;
        }
        target.setBackgroundResource(R.drawable.bg_mood_selected);
        setCardLabelColor(target, Color.parseColor("#2E7D52"), true);
    }

    private void resetMood(LinearLayout layout) {
        layout.setBackgroundResource(R.drawable.bg_mood_unselected);
        setCardLabelColor(layout, Color.parseColor("#8E8E93"), false);
    }

    private void activateCard(LinearLayout card, int bgRes, int textColor, boolean bold) {
        card.setBackgroundResource(bgRes);
        setCardLabelColor(card, textColor, bold);
    }

    private void setCardLabelColor(LinearLayout card, int color, boolean bold) {
        if (card == null || card.getChildCount() < 2) return;
        View lastChild = card.getChildAt(card.getChildCount() - 1);
        if (lastChild instanceof TextView) {
            ((TextView) lastChild).setTextColor(color);
            ((TextView) lastChild).setTypeface(bold
                    ? android.graphics.Typeface.DEFAULT_BOLD
                    : android.graphics.Typeface.DEFAULT);
        }
    }

    private void saveEntry() {
        if (selectedSubjectId == -1) {
            Toast.makeText(this, "Please select a subject first", Toast.LENGTH_SHORT).show();
            return;
        }
        String notes = etNotes.getText().toString().trim();
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        String dayAbbr  = AnalyticsEngine.getDayAbbr(cal.get(Calendar.DAY_OF_WEEK));
        String timeSlot = AnalyticsEngine.getTimeSlotForHour(cal.get(Calendar.HOUR_OF_DAY));

        ClassLog log = new ClassLog(
                selectedSubjectId, selectedSubjectName,
                selectedAttendance, selectedParticipation,
                selectedMood, notes, now, dayAbbr, timeSlot);

        executor.execute(() -> {
            AppDatabase.getInstance(this).classLogDao().insert(log);
            runOnUiThread(() -> {
                Toast.makeText(this, "Entry saved ✓", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}