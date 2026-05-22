package com.classpulse.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.classpulse.R;
import com.classpulse.database.AppDatabase;
import com.classpulse.fragments.ColorPickerBottomSheet;
import com.classpulse.models.Subject;
import com.classpulse.models.Subject.ClassTimeSlot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditSubjectActivity extends AppCompatActivity {

    public static final int[] SUBJECT_COLORS = {
            Color.parseColor("#A8D8A8"),  // pastel green
            Color.parseColor("#A8C8E8"),  // pastel blue
            Color.parseColor("#F0D8A8"),  // pastel peach/tan
            Color.parseColor("#C8A8D8"),  // pastel purple
            Color.parseColor("#F0A8A8"),  // pastel rose
            Color.parseColor("#F0E0A0"),  // pastel yellow
            Color.parseColor("#A8D8C8"),  // pastel teal
            Color.parseColor("#B8C8F0"),  // pastel periwinkle
    };

    // ─── Views ───────────────────────────────────────────────────────────────
    private android.widget.EditText etSubjectName, etClassCode, etInstructor;
    private View layoutSubjectPreview, viewColorDot;
    private android.widget.ImageView ivSubjectIcon;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private CheckBox cbApplySameTime;
    private LinearLayout layoutTimeEntries;

    // ─── State ───────────────────────────────────────────────────────────────
    private int selectedColor = Color.parseColor("#A8C8E8");
    private android.net.Uri selectedIconUri = null;
    private static final int REQUEST_PICK_ICON = 201;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();



    private final String[] DAY_ORDER = {
            "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"
    };
    private final Map<String, View>  dayTimeViews = new LinkedHashMap<>();
    private final Map<String, int[]> startTimes   = new LinkedHashMap<>();
    private final Map<String, int[]> endTimes      = new LinkedHashMap<>();

    private int editSubjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_subject);

        bindViews();

        TextView tvHeaderPrefix = findViewById(R.id.tvHeaderPrefix);
        TextView tvHeaderSuffix = findViewById(R.id.tvHeaderSuffix);
        if (getIntent().hasExtra("subject_id")) {
            tvHeaderPrefix.setText("Edit ");
            tvHeaderSuffix.setText("Subject");
        } else {
            tvHeaderPrefix.setText("Add ");
            tvHeaderSuffix.setText("Subject");
        }

        setDefaultTimes();
        setupCheckboxListeners();
        setupSameTimeCheckbox();
        setupColorDot();
        setupButtons();

        // Apply initial color to text and save button
        updatePreviewTextColors(selectedColor);

        if (getIntent().hasExtra("subject_id")) {
            editSubjectId = getIntent().getIntExtra("subject_id", -1);
            loadSubjectForEdit(editSubjectId);
        }
    }

    // ─── Bind ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        etSubjectName        = findViewById(R.id.etSubjectName);
        etClassCode          = findViewById(R.id.etClassCode);
        etInstructor         = findViewById(R.id.etInstructor);
        layoutSubjectPreview = findViewById(R.id.layoutSubjectPreview);
        viewColorDot         = findViewById(R.id.viewColorDot);
        cbMonday             = findViewById(R.id.cbMonday);
        cbTuesday            = findViewById(R.id.cbTuesday);
        cbWednesday          = findViewById(R.id.cbWednesday);
        cbThursday           = findViewById(R.id.cbThursday);
        cbFriday             = findViewById(R.id.cbFriday);
        cbSaturday           = findViewById(R.id.cbSaturday);
        cbSunday             = findViewById(R.id.cbSunday);
        cbApplySameTime      = findViewById(R.id.cbApplySameTime);
        layoutTimeEntries    = findViewById(R.id.layoutTimeEntries);
        ivSubjectIcon        = findViewById(R.id.ivSubjectIcon);
    }

    // ─── Default times ────────────────────────────────────────────────────────

    private void setDefaultTimes() {
        for (String day : DAY_ORDER) {
            startTimes.put(day, new int[]{8, 0});
            endTimes.put(day, new int[]{16, 0});
        }
    }

    // ─── Day checkboxes ───────────────────────────────────────────────────────

    private void setupCheckboxListeners() {
        for (Map.Entry<String, CheckBox> entry : getDayCheckboxMap().entrySet()) {
            entry.getValue().setOnCheckedChangeListener((btn, checked) -> refreshTimeEntries());
        }
    }

    private Map<String, CheckBox> getDayCheckboxMap() {
        Map<String, CheckBox> map = new LinkedHashMap<>();
        map.put("Monday",    cbMonday);
        map.put("Tuesday",   cbTuesday);
        map.put("Wednesday", cbWednesday);
        map.put("Thursday",  cbThursday);
        map.put("Friday",    cbFriday);
        map.put("Saturday",  cbSaturday);
        map.put("Sunday",    cbSunday);
        return map;
    }

    private List<String> getSelectedDays() {
        List<String> selected = new ArrayList<>();
        Map<String, CheckBox> cbMap = getDayCheckboxMap();
        for (String day : DAY_ORDER) {
            CheckBox cb = cbMap.get(day);
            if (cb != null && cb.isChecked()) selected.add(day);
        }
        return selected;
    }

    // ─── Time entries ─────────────────────────────────────────────────────────

    private void refreshTimeEntries() {
        layoutTimeEntries.removeAllViews();
        dayTimeViews.clear();

        List<String> selectedDays = getSelectedDays();
        if (selectedDays.isEmpty()) return;

        if (cbApplySameTime.isChecked()) {
            layoutTimeEntries.addView(inflateSingleTimeRow());
        } else {
            for (String day : selectedDays) {
                View row = inflateDayTimeRow(day);
                dayTimeViews.put(day, row);
                layoutTimeEntries.addView(row);
            }
        }
    }

    private View inflateDayTimeRow(String day) {
        View row = getLayoutInflater().inflate(R.layout.item_time_entry, layoutTimeEntries, false);
        TextView tvDayLabel = row.findViewById(R.id.tvDayLabel);
        TextView tvStart    = row.findViewById(R.id.tvStartTime);
        TextView tvEnd      = row.findViewById(R.id.tvEndTime);

        tvDayLabel.setText(day);
        tvStart.setText(formatTime(startTimes.get(day)));
        tvEnd.setText(formatTime(endTimes.get(day)));

        tvStart.setOnClickListener(v -> openTimePicker(day, true,  tvStart));
        tvEnd.setOnClickListener(v   -> openTimePicker(day, false, tvEnd));
        return row;
    }

    private View inflateSingleTimeRow() {
        View row = getLayoutInflater().inflate(R.layout.item_time_entry, layoutTimeEntries, false);
        TextView tvDayLabel = row.findViewById(R.id.tvDayLabel);
        TextView tvStart    = row.findViewById(R.id.tvStartTime);
        TextView tvEnd      = row.findViewById(R.id.tvEndTime);

        tvDayLabel.setVisibility(View.GONE);

        List<String> days = getSelectedDays();
        String seed = days.isEmpty() ? "Monday" : days.get(0);
        startTimes.put("__all__", startTimes.get(seed).clone());
        endTimes.put("__all__",   endTimes.get(seed).clone());

        tvStart.setText(formatTime(startTimes.get("__all__")));
        tvEnd.setText(formatTime(endTimes.get("__all__")));

        tvStart.setOnClickListener(v -> openTimePicker("__all__", true,  tvStart));
        tvEnd.setOnClickListener(v   -> openTimePicker("__all__", false, tvEnd));
        return row;
    }

    private void openTimePicker(String dayKey, boolean isStart, TextView display) {
        int[] time = isStart ? startTimes.get(dayKey) : endTimes.get(dayKey);
        if (time == null) time = new int[]{8, 0};

        new TimePickerDialog(this, (tp, h, m) -> {
            if (isStart) startTimes.put(dayKey, new int[]{h, m});
            else         endTimes.put(dayKey,   new int[]{h, m});
            display.setText(formatTime(new int[]{h, m}));
        }, time[0], time[1], false).show();
    }

    // ─── Same-time checkbox ───────────────────────────────────────────────────

    private void setupSameTimeCheckbox() {
        cbApplySameTime.setOnCheckedChangeListener((btn, checked) -> refreshTimeEntries());
    }

    // ─── Color picker ─────────────────────────────────────────────────────────

    private void setupColorDot() {
        // Set initial tint safely
        if (viewColorDot != null && viewColorDot.getBackground() != null) {
            viewColorDot.getBackground().mutate().setTint(selectedColor);
        }
        if (viewColorDot != null) {
            viewColorDot.setOnClickListener(v -> showColorPicker());
        }
    }

    private void showColorPicker() {
        ColorPickerBottomSheet sheet =
                ColorPickerBottomSheet.newInstance(SUBJECT_COLORS, selectedColor);
        sheet.setOnColorSelectedListener(new ColorPickerBottomSheet.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                selectedColor = color;
                if (viewColorDot != null && viewColorDot.getBackground() != null)
                    viewColorDot.getBackground().mutate().setTint(color);
                layoutSubjectPreview.setBackgroundColor(color);
                updatePreviewTextColors(color);
            }
        });
        sheet.show(getSupportFragmentManager(), "color_picker");
    }

    // ─── Buttons ──────────────────────────────────────────────────────────────

    private void setupButtons() {
        findViewById(R.id.btnSaveSubject).setOnClickListener(v -> saveSubject());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        if (ivSubjectIcon != null)
            ivSubjectIcon.setOnClickListener(v -> showIconPicker());
    }

    private void showIconPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Persist permission so the URI stays accessible after restart
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_ICON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_ICON
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedIconUri = data.getData();

            // Persist read permission across reboots
            try {
                getContentResolver().takePersistableUriPermission(
                        selectedIconUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            if (ivSubjectIcon != null) {
                ivSubjectIcon.setImageURI(selectedIconUri);
            }
        }
    }

    private void saveSubject() {
        String name       = etSubjectName.getText().toString().trim();
        String classCode  = etClassCode.getText().toString().trim();
        String instructor = etInstructor.getText().toString().trim();

        if (name.isEmpty()) {
            etSubjectName.setError("Subject name is required");
            etSubjectName.requestFocus();
            return;
        }

        List<String> selectedDays = getSelectedDays();
        if (selectedDays.isEmpty()) {
            Toast.makeText(this, "Please select at least one class day", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject subject    = new Subject();
        subject.name       = name;
        subject.classCode  = classCode;
        subject.instructor = instructor;
        subject.colorInt   = selectedColor;
        subject.classDays  = selectedDays;
        subject.timeSlots  = buildTimeSlots(selectedDays);
        subject.iconUri    = selectedIconUri != null ? selectedIconUri.toString() : null;

        final boolean isEdit = editSubjectId != -1;
        if (isEdit) subject.id = editSubjectId;

        // ✅ DB operation on background thread, finish() on main thread after
        executor.execute(() -> {
            if (isEdit) {
                AppDatabase.getInstance(this).subjectDao().update(subject);
            } else {
                AppDatabase.getInstance(this).subjectDao().insert(subject);
            }
            runOnUiThread(this::finish);
        });
    }

    private List<ClassTimeSlot> buildTimeSlots(List<String> days) {
        List<ClassTimeSlot> slots = new ArrayList<>();
        boolean sameTime = cbApplySameTime.isChecked();

        int[] sharedStart = sameTime ? startTimes.get("__all__") : null;
        int[] sharedEnd   = sameTime ? endTimes.get("__all__")   : null;

        for (String day : days) {
            int[] s = sameTime ? sharedStart : startTimes.get(day);
            int[] e = sameTime ? sharedEnd   : endTimes.get(day);
            if (s == null) s = new int[]{8, 0};
            if (e == null) e = new int[]{16, 0};
            slots.add(new ClassTimeSlot(day, s[0], s[1], e[0], e[1]));
        }
        return slots;
    }

    // ─── Edit mode ────────────────────────────────────────────────────────────

    private void loadSubjectForEdit(int id) {
        // ✅ Load from DB on background thread, populate UI on main thread
        executor.execute(() -> {
            Subject subject = AppDatabase.getInstance(this).subjectDao().getSubjectByIdSync(id);
            if (subject == null) return;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                etSubjectName.setText(subject.name);
                etClassCode.setText(subject.classCode);
                etInstructor.setText(subject.instructor);

                selectedColor = subject.colorInt;
                layoutSubjectPreview.setBackgroundColor(selectedColor);
                viewColorDot.getBackground().setTint(selectedColor);
                updatePreviewTextColors(selectedColor);

                Map<String, CheckBox> cbMap = getDayCheckboxMap();
                if (subject.classDays != null) {
                    for (String day : subject.classDays) {
                        CheckBox cb = cbMap.get(day);
                        if (cb != null) cb.setChecked(true);
                    }
                }

                if (subject.timeSlots != null) {
                    for (ClassTimeSlot slot : subject.timeSlots) {
                        startTimes.put(slot.day, new int[]{slot.startHour, slot.startMinute});
                        endTimes.put(slot.day,   new int[]{slot.endHour,   slot.endMinute});
                    }
                }

                // Restore saved icon
                if (subject.iconUri != null && ivSubjectIcon != null) {
                    try {
                        selectedIconUri = android.net.Uri.parse(subject.iconUri);
                        ivSubjectIcon.setImageURI(selectedIconUri);
                    } catch (Exception e) {
                        ivSubjectIcon.setImageResource(R.drawable.ic_subject_default);
                    }
                }

                refreshTimeEntries();
            });
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String formatTime(int[] hm) {
        if (hm == null) return "8:00 AM";
        int h = hm[0], m = hm[1];
        String ampm = h < 12 ? "AM" : "PM";
        int h12 = h % 12;
        if (h12 == 0) h12 = 12;
        return String.format(java.util.Locale.getDefault(), "%d:%02d %s", h12, m, ampm);
    }

    /** Dark text on light pastels, white text on dark backgrounds */
    private void updatePreviewTextColors(int bgColor) {
        int textColor = isColorLight(bgColor)
                ? Color.parseColor("#1C1C2E")
                : Color.WHITE;
        int subTextColor = isColorLight(bgColor)
                ? Color.parseColor("#4A4A6A")
                : Color.parseColor("#E8F0FA");
        int hintColor = isColorLight(bgColor)
                ? Color.parseColor("#7A7A9A")
                : Color.parseColor("#AABBDD");

        android.widget.EditText etName  = findViewById(R.id.etSubjectName);
        android.widget.EditText etCode  = findViewById(R.id.etClassCode);
        android.widget.EditText etInstr = findViewById(R.id.etInstructor);
        if (etName  != null) { etName.setTextColor(textColor);    etName.setHintTextColor(hintColor); }
        if (etCode  != null) { etCode.setTextColor(subTextColor); etCode.setHintTextColor(hintColor); }
        if (etInstr != null) { etInstr.setTextColor(subTextColor);etInstr.setHintTextColor(hintColor);}
    }

    private boolean isColorLight(int color) {
        double r = Color.red(color)   / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color)  / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return luminance > 0.5;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}