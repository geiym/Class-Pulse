package com.classpulse.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.classpulse.R;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportRoomActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "subject_id";

    private int subjectId;
    private Subject subject;
    private List<ClassLog> allLogs = new ArrayList<>();

    private LinearLayout layoutReportRoot;
    private TextView     tvReportSubjectName, tvReportSchedule, tvReportInstructor;
    private TextView     tvAnalysisSubject, tvReportTitle;
    private TextView     tvReportAttendance, tvReportAvgPart, tvReportSessions;
    private ImageView    ivReportIcon;
    private TextView     tabDaily, tabWeekly, tabMonthly;
    private FrameLayout  frameTabContent;
    private TextView     tvInsightTitle, tvInsightBody;

    private String currentTab = "daily";
    private Calendar displayCalendar = Calendar.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_room);

        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        if (subjectId == -1) { finish(); return; }

        bindViews();
        setupTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void bindViews() {
        layoutReportRoot       = findViewById(R.id.layoutReportRoot);
        tvReportSubjectName    = findViewById(R.id.tvReportSubjectName);
        tvReportSchedule       = findViewById(R.id.tvReportSchedule);
        tvReportInstructor     = findViewById(R.id.tvReportInstructor);
        tvAnalysisSubject      = findViewById(R.id.tvAnalysisSubject);
        tvReportTitle          = findViewById(R.id.tvReportTitle);
        tvReportAttendance     = findViewById(R.id.tvReportAttendance);
        tvReportAvgPart        = findViewById(R.id.tvReportAvgPart);
        tvReportSessions       = findViewById(R.id.tvReportSessions);
        ivReportIcon           = findViewById(R.id.ivReportIcon);
        tabDaily               = findViewById(R.id.tabDaily);
        tabWeekly              = findViewById(R.id.tabWeekly);
        tabMonthly             = findViewById(R.id.tabMonthly);
        frameTabContent        = findViewById(R.id.frameTabContent);
        tvInsightTitle         = findViewById(R.id.tvInsightTitle);
        tvInsightBody          = findViewById(R.id.tvInsightBody);

        // Edit Subject button
        findViewById(R.id.btnEditSubject).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditSubjectActivity.class);
            intent.putExtra("subject_id", subjectId);
            startActivity(intent);
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    // ─── Tabs ──────────────────────────────────────────────────────────────────

    private void setupTabs() {
        tabDaily.setOnClickListener(v  -> switchTab("daily"));
        tabWeekly.setOnClickListener(v -> switchTab("weekly"));
        tabMonthly.setOnClickListener(v-> switchTab("monthly"));
    }

    private void switchTab(String tab) {
        currentTab = tab;

        // Selected: bold subject color text
        // Unselected: darker muted brownish version of subject color
        int subjectColor = subject != null ? subject.colorInt : Color.parseColor("#5B8DCC");
        int selectedTextColor   = darkenColor(subjectColor, 0.55f);  // vivid subject color
        int unselectedTextColor = darkenColor(subjectColor, 0.45f);  // muted darker tone

        String[] tabs = {"daily", "weekly", "monthly"};
        TextView[] tvs = {tabDaily, tabWeekly, tabMonthly};

        for (int i = 0; i < tabs.length; i++) {
            boolean isSelected = tabs[i].equals(tab);
            tvs[i].setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
            if (isSelected) {
                // White pill background
                android.graphics.drawable.GradientDrawable sel =
                        new android.graphics.drawable.GradientDrawable();
                sel.setColor(Color.WHITE);
                sel.setCornerRadius(12 * getResources().getDisplayMetrics().density);
                tvs[i].setBackground(sel);
                tvs[i].setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            } else {
                tvs[i].setBackground(null);
                tvs[i].setTypeface(android.graphics.Typeface.DEFAULT);
            }
        }

        // Tint the tab container to light pastel of subject color
        View tabContainer = ((android.view.View) tabDaily.getParent());
        android.graphics.drawable.GradientDrawable containerBg =
                new android.graphics.drawable.GradientDrawable();
        containerBg.setColor(blendWithWhite(subjectColor, 0.55f));
        containerBg.setCornerRadius(14 * getResources().getDisplayMetrics().density);
        tabContainer.setBackground(containerBg);

        renderTabContent();
        updateInsight();
    }

    // ─── Data ──────────────────────────────────────────────────────────────────

    private void loadData() {
        executor.execute(() -> {
            subject = AppDatabase.getInstance(this).subjectDao().getSubjectByIdSync(subjectId);
            if (subject == null) { runOnUiThread(this::finish); return; }
            allLogs = AppDatabase.getInstance(this).classLogDao().getLogsForSubject(subjectId);
            if (allLogs == null) allLogs = new ArrayList<>();

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                populateHeader();
                switchTab("daily");
            });
        });
    }

    // ─── Header ────────────────────────────────────────────────────────────────

    private void populateHeader() {
        // Apply very light pastel background
        int color = subject.colorInt;
        int lightBg = blendWithWhite(color, 0.15f);
        layoutReportRoot.setBackgroundColor(lightBg);

        // Tint the Report Room title accent to match subject color
        tvReportTitle.setTextColor(darkenColor(color, 0.7f));
        tvAnalysisSubject.setTextColor(darkenColor(color, 0.7f));

        // Subject info card color
        findViewById(R.id.cardSubjectInfo).setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color));

        // Icon
        if (subject.iconUri != null && !subject.iconUri.isEmpty()) {
            try {
                ivReportIcon.setImageURI(Uri.parse(subject.iconUri));
            } catch (Exception e) {
                ivReportIcon.setImageResource(R.drawable.ic_subject_default);
            }
        }

        // Text
        tvReportSubjectName.setText(subject.name);
        tvAnalysisSubject.setText(subject.name);
        tvReportSchedule.setText(buildDaysList());
        String instructor = (subject.instructor != null ? subject.instructor : "")
                + (subject.classCode != null ? " · " + subject.classCode : "");
        tvReportInstructor.setText(instructor.trim());

        // Stats
        int total = allLogs.size();
        int present = 0, late = 0;
        int partSum = 0;
        for (ClassLog l : allLogs) {
            if ("Present".equals(l.attendance)) present++;
            else if ("Late".equals(l.attendance)) late++;
            partSum += participationScore(l.participation);
        }
        int attPct = total > 0 ? Math.round((present + late) * 100f / total) : 0;
        String avgPart = total > 0 ? participationLabel(partSum / total) : "—";

        tvReportAttendance.setText(attPct + "%");
        tvReportAvgPart.setText(avgPart);
        tvReportSessions.setText(String.valueOf(total));
    }

    private String buildDaysList() {
        if (subject.classDays == null || subject.classDays.isEmpty()) return "";
        return String.join(" / ", subject.classDays)
                + (subject.timeSlots != null && !subject.timeSlots.isEmpty()
                ? "  " + subject.timeSlots.get(0).formatStart()
                + " – " + subject.timeSlots.get(0).formatEnd()
                : "");
    }

    // ─── Tab content ───────────────────────────────────────────────────────────

    private void renderTabContent() {
        frameTabContent.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);

        switch (currentTab) {
            case "daily":
                View daily = inf.inflate(R.layout.layout_tab_daily, frameTabContent, false);
                frameTabContent.addView(daily);
                setupDailyView(daily);
                break;
            case "weekly":
                View weekly = inf.inflate(R.layout.layout_tab_weekly, frameTabContent, false);
                frameTabContent.addView(weekly);
                setupWeeklyView(weekly);
                break;
            case "monthly":
                View monthly = inf.inflate(R.layout.layout_tab_monthly, frameTabContent, false);
                frameTabContent.addView(monthly);
                setupMonthlyView(monthly);
                break;
        }
    }

    // ─── Daily View ────────────────────────────────────────────────────────────

    private void setupDailyView(View v) {
        android.widget.GridLayout grid = v.findViewById(R.id.gridCalendar);
        TextView tvMonth = v.findViewById(R.id.tvCalMonth);

        renderCalendar(grid, tvMonth, v);

        v.findViewById(R.id.btnCalPrev).setOnClickListener(btn -> {
            displayCalendar.add(Calendar.MONTH, -1);
            renderCalendar(grid, tvMonth, v);
        });
        v.findViewById(R.id.btnCalNext).setOnClickListener(btn -> {
            displayCalendar.add(Calendar.MONTH, 1);
            renderCalendar(grid, tvMonth, v);
        });
    }

    private void renderCalendar(android.widget.GridLayout grid, TextView tvMonth, View parentView) {
        grid.removeAllViews();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonth.setText(fmt.format(displayCalendar.getTime()));

        // Build log map: day-of-month -> log
        Map<Integer, ClassLog> logMap = new HashMap<>();
        Calendar c = Calendar.getInstance();
        for (ClassLog log : allLogs) {
            c.setTimeInMillis(log.logDate);
            if (c.get(Calendar.MONTH) == displayCalendar.get(Calendar.MONTH)
                    && c.get(Calendar.YEAR) == displayCalendar.get(Calendar.YEAR)) {
                logMap.put(c.get(Calendar.DAY_OF_MONTH), log);
            }
        }

        Calendar cal = (Calendar) displayCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int today = -1;
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                && now.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
            today = now.get(Calendar.DAY_OF_MONTH);

        int dp = (int) getResources().getDisplayMetrics().density;

        // Show detail card with prompt text by default
        View cardDetail = parentView.findViewById(R.id.cardDayDetail);
        if (cardDetail != null) {
            cardDetail.setVisibility(View.VISIBLE);
            TextView tvTitle = cardDetail.findViewById(R.id.tvDayDetailTitle);
            if (tvTitle != null) tvTitle.setText("📅 TAP A DATE ABOVE TO SEE DETAILS");
            // Hide rows until date tapped
            cardDetail.findViewById(R.id.tvDayAttendance).setVisibility(View.GONE);
            cardDetail.findViewById(R.id.tvDayParticipation).setVisibility(View.GONE);
            cardDetail.findViewById(R.id.tvDayMood).setVisibility(View.GONE);
        }

        // Blank cells before day 1
        for (int i = 0; i < firstDow; i++) {
            android.widget.GridLayout.LayoutParams lp =
                    new android.widget.GridLayout.LayoutParams();
            lp.width = 0; lp.height = (int)(36 * dp);
            lp.columnSpec = android.widget.GridLayout.spec(i, 1f);
            View blank = new View(this);
            blank.setLayoutParams(lp);
            grid.addView(blank);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            int col = (firstDow + day - 1) % 7;
            int cellSize = (int)(40 * dp);
            android.widget.GridLayout.LayoutParams lp =
                    new android.widget.GridLayout.LayoutParams();
            lp.width = 0; lp.height = cellSize;
            lp.columnSpec = android.widget.GridLayout.spec(col, 1f);
            lp.setMargins(2, 3, 2, 3);

            TextView cell = new TextView(this);
            cell.setText(String.valueOf(day));
            cell.setGravity(android.view.Gravity.CENTER);
            cell.setTextSize(13);
            cell.setLayoutParams(lp);

            ClassLog log = logMap.get(day);
            if (log != null) {
                int bgColor = "Present".equals(log.attendance) ? Color.parseColor("#A8D8A8")
                        : "Late".equals(log.attendance)    ? Color.parseColor("#F0E0A0")
                        :                                     Color.parseColor("#F0A8A8");
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                bg.setColor(bgColor);
                cell.setBackground(bg);
                cell.setTextColor(Color.parseColor("#1C1C1E"));

                final ClassLog finalLog = log;
                final int finalDay = day;
                cell.setOnClickListener(cv -> showDayDetail(parentView, finalLog, finalDay));
                cell.setClickable(true);
                cell.setFocusable(true);

            } else if (day == today) {
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                bg.setStroke((int)(2 * dp), subject.colorInt);
                bg.setColor(Color.TRANSPARENT);
                cell.setBackground(bg);
                cell.setTextColor(subject.colorInt);
                cell.setClickable(true);
                cell.setFocusable(true);
                final int todayDay = day;
                cell.setOnClickListener(cv -> showEmptyDayDetail(parentView, todayDay));
            } else {
                // Normal day — black text, tappable to show "no log" message
                cell.setTextColor(Color.parseColor("#1C1C1E"));
                cell.setClickable(true);
                cell.setFocusable(true);
                android.graphics.drawable.RippleDrawable ripple =
                        new android.graphics.drawable.RippleDrawable(
                                android.content.res.ColorStateList.valueOf(Color.parseColor("#20000000")),
                                null, null);
                cell.setBackground(ripple);
                final int emptyDay = day;
                cell.setOnClickListener(cv -> showEmptyDayDetail(parentView, emptyDay));
            }

            grid.addView(cell);
        }
    }

    private void showEmptyDayDetail(View parentView, int day) {
        View cardDetail = parentView.findViewById(R.id.cardDayDetail);
        if (cardDetail == null) return;
        cardDetail.setVisibility(View.VISIBLE);

        SimpleDateFormat fmt = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.set(displayCalendar.get(Calendar.YEAR), displayCalendar.get(Calendar.MONTH), day);

        ((TextView) cardDetail.findViewById(R.id.tvDayDetailTitle))
                .setText("📅 " + fmt.format(c.getTime()).toUpperCase());

        // Show rows but with "—" placeholders
        TextView tvAtt  = cardDetail.findViewById(R.id.tvDayAttendance);
        TextView tvPart = cardDetail.findViewById(R.id.tvDayParticipation);
        TextView tvMood = cardDetail.findViewById(R.id.tvDayMood);
        if (tvAtt  != null) { tvAtt.setVisibility(View.VISIBLE);  tvAtt.setText("—");  tvAtt.setBackgroundResource(R.drawable.bg_day_badge_low); }
        if (tvPart != null) { tvPart.setVisibility(View.VISIBLE); tvPart.setText("—"); tvPart.setBackgroundResource(R.drawable.bg_day_badge_low); }
        if (tvMood != null) { tvMood.setVisibility(View.VISIBLE); tvMood.setText("—"); tvMood.setBackgroundResource(R.drawable.bg_day_badge_low); }

        TextView tvNote = cardDetail.findViewById(R.id.tvDayNote);
        if (tvNote != null) tvNote.setText("No class log for this day.");
    }

    private void showDayDetail(View parentView, ClassLog log, int day) {
        View cardDetail = parentView.findViewById(R.id.cardDayDetail);
        if (cardDetail == null) return;
        cardDetail.setVisibility(View.VISIBLE);

        SimpleDateFormat fmt = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.set(displayCalendar.get(Calendar.YEAR), displayCalendar.get(Calendar.MONTH), day);

        ((TextView) cardDetail.findViewById(R.id.tvDayDetailTitle))
                .setText("📅 " + fmt.format(c.getTime()).toUpperCase());

        // Make all rows visible
        TextView tvAtt  = cardDetail.findViewById(R.id.tvDayAttendance);
        TextView tvPart = cardDetail.findViewById(R.id.tvDayParticipation);
        TextView tvMood = cardDetail.findViewById(R.id.tvDayMood);
        if (tvAtt  != null) tvAtt.setVisibility(View.VISIBLE);
        if (tvPart != null) tvPart.setVisibility(View.VISIBLE);
        if (tvMood != null) tvMood.setVisibility(View.VISIBLE);

        // Attendance badge
        if (tvAtt != null) {
            String attEmoji = "Present".equals(log.attendance) ? "✅"
                    : "Late".equals(log.attendance)    ? "🕐" : "❌";
            tvAtt.setText(attEmoji + " " + (log.attendance != null ? log.attendance : "—"));
            tvAtt.setBackgroundResource(
                    "Present".equals(log.attendance) ? R.drawable.bg_day_badge_present
                            : "Late".equals(log.attendance)    ? R.drawable.bg_day_badge_late
                            :                                    R.drawable.bg_day_badge_absent);
        }

        // Participation badge
        if (tvPart != null) {
            String partEmoji = "High".equals(log.participation) ? "🙋" : "👋";
            tvPart.setText(partEmoji + " " + (log.participation != null ? log.participation : "—"));
            tvPart.setBackgroundResource(
                    "High".equals(log.participation)   ? R.drawable.bg_day_badge_high
                            : "Medium".equals(log.participation) ? R.drawable.bg_day_badge_medium
                            :                                      R.drawable.bg_day_badge_low);
        }

        // Mood badge
        if (tvMood != null) {
            tvMood.setText(getMoodEmoji(log.mood) + " " + (log.mood != null ? log.mood : "—"));
            tvMood.setBackgroundResource(
                    "Focused".equals(log.mood) || "Happy".equals(log.mood)
                            ? R.drawable.bg_day_badge_happy
                            : "Stressed".equals(log.mood) || "Tired".equals(log.mood)
                            ? R.drawable.bg_day_badge_mood
                            : R.drawable.bg_day_badge_medium);
        }

        // Notes
        ((TextView) cardDetail.findViewById(R.id.tvDayNote))
                .setText(log.notes != null && !log.notes.isEmpty()
                        ? "\"" + log.notes + "\""
                        : "No notes for this session.");
    }

    // ─── Weekly View ───────────────────────────────────────────────────────────

    private Calendar weekStart = null;

    private void setupWeeklyView(View v) {
        if (weekStart == null) {
            weekStart = Calendar.getInstance();
            weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }

        renderWeekly(v);

        v.findViewById(R.id.btnWeekPrev).setOnClickListener(b -> {
            weekStart.add(Calendar.WEEK_OF_YEAR, -1);
            renderWeekly(v);
        });
        v.findViewById(R.id.btnWeekNext).setOnClickListener(b -> {
            weekStart.add(Calendar.WEEK_OF_YEAR, 1);
            renderWeekly(v);
        });

        setupCheckinCard(v, "weekly");
    }

    private void renderBarPillLabels(View v, List<ClassLog> weekLogs) {
        LinearLayout llLabels = v.findViewById(R.id.llBarLabels);
        if (llLabels == null) return;
        llLabels.removeAllViews();

        String[] days = {"Mon","Tue","Wed","Thu","Fri"};
        Map<String, Integer> partMap = new HashMap<>();
        for (ClassLog l : weekLogs)
            partMap.put(getDayAbbr(l), participationScore(l.participation));

        int dp = (int) getResources().getDisplayMetrics().density;

        for (int i = 0; i < days.length; i++) {
            int val = partMap.getOrDefault(days[i], 0);
            String label = val >= 66 ? "High" : val >= 33 ? "Med" : "Low";
            int bgColor  = val >= 66 ? Color.parseColor("#C8F0D8")
                    : val >= 33 ? Color.parseColor("#E3E8FF")
                    :             Color.parseColor("#FFF3CD");
            int txtColor = val >= 66 ? Color.parseColor("#2E7D52")
                    : val >= 33 ? Color.parseColor("#3F51B5")
                    :             Color.parseColor("#B45309");

            TextView pill = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMarginEnd((int)(4 * dp));
            pill.setLayoutParams(lp);
            pill.setGravity(android.view.Gravity.CENTER);
            pill.setText(label);
            pill.setTextSize(9f);
            pill.setTextColor(txtColor);
            pill.setPadding((int)(6*dp), (int)(3*dp), (int)(6*dp), (int)(3*dp));

            android.graphics.drawable.GradientDrawable bg =
                    new android.graphics.drawable.GradientDrawable();
            bg.setColor(bgColor);
            bg.setCornerRadius(20f);
            pill.setBackground(bg);
            llLabels.addView(pill);
        }
    }

    private void renderWeekly(View v) {
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 4);

        SimpleDateFormat fmt = new SimpleDateFormat("MMM d", Locale.getDefault());
        ((TextView) v.findViewById(R.id.tvWeekRange))
                .setText(fmt.format(weekStart.getTime()) + "–" + fmt.format(weekEnd.getTime()));

        // Filter logs for this week
        List<ClassLog> weekLogs = new ArrayList<>();
        for (ClassLog l : allLogs) {
            Calendar lc = Calendar.getInstance();
            lc.setTimeInMillis(l.logDate);
            if (!lc.before(weekStart) && !lc.after(weekEnd)) weekLogs.add(l);
        }

        // Stats
        int present = 0, late = 0, absent = 0;
        int partSum = 0;
        Map<String, Integer> moodCount = new HashMap<>();
        for (ClassLog l : weekLogs) {
            if ("Present".equals(l.attendance)) present++;
            else if ("Late".equals(l.attendance)) late++;
            else absent++;
            partSum += participationScore(l.participation);
            if (l.mood != null) moodCount.put(l.mood, moodCount.getOrDefault(l.mood, 0) + 1);
        }
        int total = weekLogs.size();
        String avgPart = total > 0 ? participationLabel(partSum / total) : "—";
        String topMood = topKey(moodCount);

        ((TextView) v.findViewById(R.id.tvWeekPresent)).setText(present + "/" + (present + late + absent == 0 ? "?" : present + late + absent));
        ((TextView) v.findViewById(R.id.tvWeekAvgPart)).setText(avgPart);
        ((TextView) v.findViewById(R.id.tvWeekTopMood)).setText(getMoodEmoji(topMood));
        ((TextView) v.findViewById(R.id.tvWeekPresentCount)).setText(String.valueOf(present));
        ((TextView) v.findViewById(R.id.tvWeekLateCount)).setText(String.valueOf(late));
        ((TextView) v.findViewById(R.id.tvWeekAbsentCount)).setText(String.valueOf(absent));

        // Set progress bars — proportional to total sessions
        int totalSess = present + late + absent;
        android.widget.ProgressBar pbPresent = v.findViewById(R.id.pbWeekPresent);
        android.widget.ProgressBar pbLate    = v.findViewById(R.id.pbWeekLate);
        android.widget.ProgressBar pbAbsent  = v.findViewById(R.id.pbWeekAbsent);
        if (pbPresent != null) pbPresent.setProgress(totalSess > 0 ? present * 100 / totalSess : 0);
        if (pbLate    != null) pbLate.setProgress(totalSess    > 0 ? late    * 100 / totalSess : 0);
        if (pbAbsent  != null) pbAbsent.setProgress(totalSess  > 0 ? absent  * 100 / totalSess : 0);

        // Bar chart
        setupWeeklyBarChart(v, weekLogs, weekStart);

        // Pie chart
        setupPieChart(v.findViewById(R.id.pieChartWeekly), present, late, absent);

    }

    private void setupWeeklyBarChart(View v, List<ClassLog> weekLogs, Calendar ws) {
        String[] days = {"Mon","Tue","Wed","Thu","Fri"};

        Map<String, Integer> partMap = new HashMap<>();
        Map<String, String> partLabelMap = new HashMap<>();
        for (ClassLog l : weekLogs) {
            String dayKey = getDayAbbr(l);
            partMap.put(dayKey, participationScore(l.participation));
            partLabelMap.put(dayKey, l.participation != null ? l.participation : "Low");
        }

        LinearLayout llBars = v.findViewById(R.id.llCustomBars);
        if (llBars == null) return;
        llBars.removeAllViews();

        float dp = getResources().getDisplayMetrics().density;
        int maxBarH = (int)(110 * dp);

        for (String day : days) {
            int val = partMap.containsKey(day) ? partMap.get(day) : 0;

            int barColor = val >= 66 ? Color.parseColor("#6DC97A")
                    : val >= 33 ? Color.parseColor("#8B8FD4")
                    : val >  0  ? Color.parseColor("#E8A96A")
                    :             Color.parseColor("#E0E0E0");

            int barH = val > 0
                    ? Math.max((int)(val / 100f * maxBarH), (int)(40 * dp))
                    : (int)(20 * dp);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            colLp.setMarginStart((int)(4 * dp));
            colLp.setMarginEnd((int)(4 * dp));
            col.setLayoutParams(colLp);

            android.view.View bar = new android.view.View(this);
            android.graphics.drawable.GradientDrawable barBg =
                    new android.graphics.drawable.GradientDrawable();
            barBg.setColor(barColor);
            barBg.setCornerRadii(new float[]{
                    16*dp, 16*dp, 16*dp, 16*dp, 0, 0, 0, 0
            });
            bar.setBackground(barBg);
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, barH);
            bar.setLayoutParams(barLp);
            col.addView(bar);
            llBars.addView(col);
        }

        renderWeeklyPillsAndDays(v, days, partMap, partLabelMap);
    }

    private void renderWeeklyPillsAndDays(View v, String[] days,
                                          Map<String, Integer> partMap,
                                          Map<String, String> partLabelMap) {
        LinearLayout llLabels = v.findViewById(R.id.llBarLabels);
        if (llLabels == null) return;
        llLabels.removeAllViews();

        float dp = getResources().getDisplayMetrics().density;

        for (String day : days) {
            int val = partMap.containsKey(day) ? partMap.get(day) : 0;
            String label = partLabelMap.containsKey(day) ? partLabelMap.get(day) : "—";

            int pillBg, pillTxt;
            if (val >= 66)     { pillBg = Color.parseColor("#C8F0D8"); pillTxt = Color.parseColor("#2E7D52"); }
            else if (val >= 33){ pillBg = Color.parseColor("#E3E4FF"); pillTxt = Color.parseColor("#4A4FC0"); }
            else if (val > 0)  { pillBg = Color.parseColor("#FFE8D0"); pillTxt = Color.parseColor("#C06020"); }
            else               { pillBg = Color.parseColor("#F0F0F0"); pillTxt = Color.parseColor("#AAAAAA"); label = "—"; }

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            col.setLayoutParams(colLp);

            TextView pill = new TextView(this);
            pill.setText(label);
            pill.setTextSize(11f);
            pill.setTextColor(pillTxt);
            pill.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            pill.setPadding((int)(10*dp), (int)(3*dp), (int)(10*dp), (int)(3*dp));
            android.graphics.drawable.GradientDrawable pillBgD =
                    new android.graphics.drawable.GradientDrawable();
            pillBgD.setColor(pillBg);
            pillBgD.setCornerRadius(20f * dp);
            pill.setBackground(pillBgD);
            LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            pillLp.bottomMargin = (int)(4 * dp);
            pill.setLayoutParams(pillLp);
            col.addView(pill);

            TextView dayTv = new TextView(this);
            dayTv.setText(day);
            dayTv.setTextSize(12f);
            dayTv.setTextColor(Color.parseColor("#555555"));
            dayTv.setGravity(android.view.Gravity.CENTER);
            col.addView(dayTv);

            llLabels.addView(col);
        }
    }

    // ─── Monthly View ──────────────────────────────────────────────────────────

    private Calendar monthCal = Calendar.getInstance();

    private void setupMonthlyView(View v) {
        renderMonthly(v);
        v.findViewById(R.id.btnMonthPrev).setOnClickListener(b -> {
            monthCal.add(Calendar.MONTH, -1);
            renderMonthly(v);
        });
        v.findViewById(R.id.btnMonthNext).setOnClickListener(b -> {
            monthCal.add(Calendar.MONTH, 1);
            renderMonthly(v);
        });
        setupCheckinCard(v, "monthly");
    }

    private void renderMonthly(View v) {
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        ((TextView) v.findViewById(R.id.tvMonthLabel)).setText(fmt.format(monthCal.getTime()));

        List<ClassLog> monthLogs = new ArrayList<>();
        Calendar lc = Calendar.getInstance();
        for (ClassLog l : allLogs) {
            lc.setTimeInMillis(l.logDate);
            if (lc.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH)
                    && lc.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR))
                monthLogs.add(l);
        }

        int present = 0, late = 0, absent = 0;
        int partSum = 0;
        Map<String, Integer> moodCount = new HashMap<>();
        Map<String, Integer> dayPartMap = new HashMap<>();
        int noteCount = 0;
        int streak = 0, maxStreak = 0, curStreak = 0;

        for (ClassLog l : monthLogs) {
            if ("Present".equals(l.attendance)) { present++; curStreak++; }
            else if ("Late".equals(l.attendance)) { late++; curStreak = 0; }
            else { absent++; curStreak = 0; }
            maxStreak = Math.max(maxStreak, curStreak);
            partSum += participationScore(l.participation);
            if (l.mood != null) moodCount.put(l.mood, moodCount.getOrDefault(l.mood, 0) + 1);
            if (getDayAbbr(l) != null) dayPartMap.put(getDayAbbr(l),
                    dayPartMap.getOrDefault(getDayAbbr(l), 0) + participationScore(l.participation));
            if (l.notes != null && !l.notes.isEmpty()) noteCount++;
        }
        int total = monthLogs.size();

        ((TextView) v.findViewById(R.id.tvMonthPresentCount)).setText(String.valueOf(present));
        ((TextView) v.findViewById(R.id.tvMonthLateCount)).setText(String.valueOf(late));
        ((TextView) v.findViewById(R.id.tvMonthAbsentCount)).setText(String.valueOf(absent));

        // Monthly progress bars
        int totalM = present + late + absent;
        android.widget.ProgressBar pbMP = v.findViewById(R.id.pbMonthPresent);
        android.widget.ProgressBar pbML = v.findViewById(R.id.pbMonthLate);
        android.widget.ProgressBar pbMA = v.findViewById(R.id.pbMonthAbsent);
        if (pbMP != null) pbMP.setProgress(totalM > 0 ? present * 100 / totalM : 0);
        if (pbML != null) pbML.setProgress(totalM > 0 ? late    * 100 / totalM : 0);
        if (pbMA != null) pbMA.setProgress(totalM > 0 ? absent  * 100 / totalM : 0);
        ((TextView) v.findViewById(R.id.tvBestStreak)).setText(maxStreak + " days");
        ((TextView) v.findViewById(R.id.tvNotesLogged)).setText(String.valueOf(noteCount));

        // Best/weakest days
        String bestDay = topKey(dayPartMap);
        String worstDay = bottomKey(dayPartMap);
        ((TextView) v.findViewById(R.id.tvBestDays)).setText(bestDay != null ? bestDay : "—");
        ((TextView) v.findViewById(R.id.tvWeakestDay)).setText(worstDay != null ? worstDay : "—");

        // Mood bars
        int happy    = moodCount.getOrDefault("Focused", moodCount.getOrDefault("Happy", 0));
        int neutral  = moodCount.getOrDefault("Neutral", 0);
        int stressed = moodCount.getOrDefault("Stressed", 0) + moodCount.getOrDefault("Tired", 0);

        setMoodBar(v, R.id.pbMoodHappy,    R.id.tvMoodHappyPct,    happy,    total);
        setMoodBar(v, R.id.pbMoodNeutral,  R.id.tvMoodNeutralPct,  neutral,  total);
        setMoodBar(v, R.id.pbMoodStressed, R.id.tvMoodStressedPct, stressed, total);

        setupPieChart(v.findViewById(R.id.pieChartMonthly), present, late, absent);
    }

    private void setMoodBar(View v, int barId, int tvId, int count, int total) {
        int pct = total > 0 ? Math.round(count * 100f / total) : 0;
        ProgressBar pb = v.findViewById(barId);
        TextView tv = v.findViewById(tvId);
        if (pb != null) pb.setProgress(pct);
        if (tv != null) tv.setText(pct + "%");
    }

    // ─── Check-In Card ─────────────────────────────────────────────────────────

    private void setupCheckinCard(View v, String period) {
        List<ClassLog> periodLogs = period.equals("weekly")
                ? getWeekLogs() : getMonthLogs();

        int total = periodLogs.size();
        int present = 0, partSum = 0, stressCount = 0;
        boolean noBackToBack = true;
        String prev = null;
        for (ClassLog l : periodLogs) {
            if ("Present".equals(l.attendance) || "Late".equals(l.attendance)) present++;
            partSum += participationScore(l.participation);
            if ("Stressed".equals(l.mood) || "Tired".equals(l.mood)) stressCount++;
            if ("Absent".equals(l.attendance) && "Absent".equals(prev)) noBackToBack = false;
            prev = l.attendance;
        }
        int avgPart = total > 0 ? partSum / total : 0;

        // Subtitle
        TextView sub = v.findViewById(R.id.tvCheckinSubtitle);
        if (sub != null) sub.setText(subject.name + " · This " + period);

        // Row 1: Showing Up
        setCheckinRow(v,
                R.id.tvCheckinIcon, R.id.tvCheckinTitle, R.id.tvCheckinDesc, R.id.tvCheckinBadge,
                "📅", "Showing Up",
                total > 0 ? "You came to " + present + " out of " + total + " classes! 🎉" : "No sessions logged yet.",
                present >= total * 0.8 || total == 0 ? "Great" : present > 0 ? "On track" : "Check in",
                R.drawable.bg_checkin_badge_green,
                present >= total * 0.8 || total == 0 ? "#2E7D52" : "#1A6B8A");

        // Row 2: Joining In
        setCheckinRow(v,
                R.id.tvCheckinIcon2, R.id.tvCheckinTitle2, R.id.tvCheckinDesc2, R.id.tvCheckinBadge2,
                "✋", "Joining in",
                avgPart < 33 ? "A bit quiet this " + period + ". Raise your hand more!" : "Good participation! 👍",
                avgPart >= 50 ? "Great" : "Try more",
                avgPart >= 50 ? R.drawable.bg_checkin_badge_green : R.drawable.bg_checkin_badge_yellow,
                avgPart >= 50 ? "#2E7D52" : "#B45309");

        // Row 3: Staying Consistent
        setCheckinRow(v,
                R.id.tvCheckinIcon3, R.id.tvCheckinTitle3, R.id.tvCheckinDesc3, R.id.tvCheckinBadge3,
                "🔁", "Staying consistent",
                noBackToBack ? "No back-to-back absences. Keep it up! ✅" : "Watch out for back-to-back absences.",
                noBackToBack ? "On track" : "Check in",
                noBackToBack ? R.drawable.bg_checkin_badge_green : R.drawable.bg_checkin_badge_pink,
                noBackToBack ? "#1A6B3A" : "#B03030");

        // Row 4: Feeling Okay
        setCheckinRow(v,
                R.id.tvCheckinIcon4, R.id.tvCheckinTitle4, R.id.tvCheckinDesc4, R.id.tvCheckinBadge4,
                "😊", "Feeling okay?",
                stressCount > 2 ? "Stressed " + stressCount + " times this " + period + ". Rest up! 🏖" : "You're doing great emotionally! 😊",
                stressCount > 2 ? "Check in" : "Great",
                stressCount > 2 ? R.drawable.bg_checkin_badge_pink : R.drawable.bg_checkin_badge_green,
                stressCount > 2 ? "#B03030" : "#2E7D52");
    }

    private void setCheckinRow(View parent, int iconId, int titleId, int descId, int badgeId,
                               String icon, String title, String desc,
                               String badge, int badgeDrawable, String badgeTextColor) {
        TextView tvIcon  = parent.findViewById(iconId);
        TextView tvTitle = parent.findViewById(titleId);
        TextView tvDesc  = parent.findViewById(descId);
        TextView tvBadge = parent.findViewById(badgeId);
        if (tvIcon  != null) tvIcon.setText(icon);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvDesc  != null) tvDesc.setText(desc);
        if (tvBadge != null) {
            tvBadge.setText(badge);
            tvBadge.setBackgroundResource(badgeDrawable);
            tvBadge.setTextColor(Color.parseColor(badgeTextColor));
        }
    }

    // ─── Smart Insight ─────────────────────────────────────────────────────────

    private void updateInsight() {
        if (subject == null) return;
        String period = currentTab.equals("daily") ? "APR " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                : currentTab.equals("weekly") ? "THIS WEEK" : "THIS MONTH";
        tvInsightTitle.setText("💡 SMART INSIGHT · " + period.toUpperCase());

        List<ClassLog> logs = currentTab.equals("weekly") ? getWeekLogs() : getMonthLogs();
        int total = logs.size();
        if (total == 0) {
            tvInsightBody.setText("Keep logging to unlock your personalized insights!");
            return;
        }
        int partSum = 0; int lateCount = 0;
        Map<String, Integer> dayPart = new HashMap<>();
        for (ClassLog l : logs) {
            partSum += participationScore(l.participation);
            if ("Late".equals(l.attendance)) lateCount++;
            if (getDayAbbr(l) != null) dayPart.put(getDayAbbr(l),
                    dayPart.getOrDefault(getDayAbbr(l), 0) + participationScore(l.participation));
        }
        String weakDay = bottomKey(dayPart);
        int avgPart = partSum / total;

        String insight;
        if (lateCount > 1 && weakDay != null)
            insight = weakDay + " is your weakest day in " + subject.name + ". You were late and had low participation. Watch your " + weakDay + " pattern!";
        else if (avgPart < 33)
            insight = subject.name + " is your most attended subject but participation is low. Try engaging more — raise your hand or ask questions!";
        else
            insight = "You're showing up consistently in " + subject.name + ". Keep it up! 🎉";

        tvInsightBody.setText(insight);
        // Tint insight card to subject color
        findViewById(R.id.frameTabContent).post(() -> {
            // The insight card is outside frameTabContent so find it directly
        });
    }

    // ─── Pie Chart Helper ──────────────────────────────────────────────────────

    private void setupPieChart(com.github.mikephil.charting.charts.PieChart chart,
                               int present, int late, int absent) {
        if (chart == null) return;

        List<com.github.mikephil.charting.data.PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Always show at least a placeholder so ring renders
        if (present + late + absent == 0) {
            entries.add(new com.github.mikephil.charting.data.PieEntry(1f));
            colors.add(Color.parseColor("#E0E0E0"));
        } else {
            if (present > 0) { entries.add(new com.github.mikephil.charting.data.PieEntry(present)); colors.add(Color.parseColor("#81C784")); }
            if (late > 0)    { entries.add(new com.github.mikephil.charting.data.PieEntry(late));    colors.add(Color.parseColor("#FFB74D")); }
            if (absent > 0)  { entries.add(new com.github.mikephil.charting.data.PieEntry(absent));  colors.add(Color.parseColor("#E57373")); }
        }

        com.github.mikephil.charting.data.PieDataSet ds =
                new com.github.mikephil.charting.data.PieDataSet(entries, "");
        ds.setColors(colors);
        ds.setDrawValues(false);
        ds.setSliceSpace(2f);

        com.github.mikephil.charting.data.PieData data =
                new com.github.mikephil.charting.data.PieData(ds);

        chart.setData(data);
        chart.setHoleRadius(52f);
        chart.setTransparentCircleRadius(56f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.setTouchEnabled(false);
        chart.setMinAngleForSlices(5f);
        chart.setExtraOffsets(4f, 4f, 4f, 4f);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private List<ClassLog> getWeekLogs() {
        if (weekStart == null) return new ArrayList<>();
        Calendar end = (Calendar) weekStart.clone();
        end.add(Calendar.DAY_OF_YEAR, 6);
        List<ClassLog> result = new ArrayList<>();
        for (ClassLog l : allLogs) {
            Calendar lc = Calendar.getInstance();
            lc.setTimeInMillis(l.logDate);
            if (!lc.before(weekStart) && !lc.after(end)) result.add(l);
        }
        return result;
    }

    private List<ClassLog> getMonthLogs() {
        List<ClassLog> result = new ArrayList<>();
        Calendar lc = Calendar.getInstance();
        for (ClassLog l : allLogs) {
            lc.setTimeInMillis(l.logDate);
            if (lc.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH)
                    && lc.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR))
                result.add(l);
        }
        return result;
    }

    private int participationScore(String p) {
        if ("High".equals(p))   return 100;
        if ("Medium".equals(p)) return 50;
        return 0;
    }

    private String participationLabel(int avg) {
        if (avg >= 66) return "High";
        if (avg >= 33) return "Med";
        return "Low";
    }

    private String topKey(Map<String, Integer> map) {
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
    }

    private String bottomKey(Map<String, Integer> map) {
        return map.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
    }

    private String getMoodEmoji(String mood) {
        if (mood == null) return "😊";
        switch (mood) {
            case "Happy": case "Focused": return "😊";
            case "Neutral":               return "😐";
            case "Tired":                 return "😴";
            case "Stressed":              return "😰";
            default:                      return "😊";
        }
    }

    private int blendWithWhite(int color, float ratio) {
        int r = (int)(Color.red(color)   * ratio + 255 * (1 - ratio));
        int g = (int)(Color.green(color) * ratio + 255 * (1 - ratio));
        int b = (int)(Color.blue(color)  * ratio + 255 * (1 - ratio));
        return Color.rgb(r, g, b);
    }

    private int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    /** Get day abbreviation (Mon/Tue/etc) from log's timestamp */
    private String getDayAbbr(ClassLog log) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(log.logDate);
        return com.classpulse.utils.AnalyticsEngine.getDayAbbr(c.get(Calendar.DAY_OF_WEEK));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}