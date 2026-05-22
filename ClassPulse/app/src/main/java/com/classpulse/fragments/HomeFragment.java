package com.classpulse.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.classpulse.R;
import com.classpulse.activities.LogReflectionActivity;
import com.classpulse.activities.MainActivity;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;
import com.classpulse.utils.AnalyticsEngine;
import com.classpulse.utils.PrefsManager;
import com.classpulse.views.BarcodeView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    // Header
    private TextView tvGreeting, tvUserName, tvDate;

    // ID card
    private TextView tvIdName, tvIdBirthday, tvIdSchool, tvIdYear;
    private BarcodeView barcodeView;

    // Weekly/Monthly tabs
    private TextView tabWeekly, tabMonthly;
    private boolean isWeekly = true;

    // Stat pills
    private TextView tvAttendanceRate, tvAvgParticipation, tvSessionsStat, tvTopSubject;

    // Bento grid
    private TextView tvPresentCount, tvTopMood, tvTopMoodPct, tvLateCount, tvBestDay;

    // Bar chart
    private BarChart barChartParticipation;
    private TextView tvBestDayChip, tvWorstDayChip;

    // Subject scroll
    private LinearLayout llSubjectsScroll;

    // Mood boxes
    private TextView tvMoodHappyPct, tvMoodNeutralPct, tvMoodStressedPct;

    // Schedule
    private LinearLayout llSchedule;

    private android.widget.RelativeLayout llIdTop;
    private androidx.cardview.widget.CardView cardId;

    private String pendingPickTarget;
    private static final int REQUEST_PICK_IMAGE = 101;

    private TextView tvChartTitle;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Map<String, Integer> monthlyWeekParticipation; // "Wk 1", "Wk 2", etc.
    private TextView tvMoodSectionTitle;
    private TextView tvBestDayLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupTabs();
        setupQuickActions(view);
        populateHeader();
        loadData();
    }

    private void bindViews(View v) {
        tvGreeting   = v.findViewById(R.id.tv_greeting);
        tvUserName   = v.findViewById(R.id.tv_user_name);
        tvDate       = v.findViewById(R.id.tv_date);

        tvIdName     = v.findViewById(R.id.tv_id_name);
        tvIdBirthday = v.findViewById(R.id.tv_id_birthday);
        tvIdSchool   = v.findViewById(R.id.tv_id_school);
        tvIdYear     = v.findViewById(R.id.tv_id_year);
        barcodeView  = v.findViewById(R.id.barcode_view);

        cardId  = v.findViewById(R.id.card_id);
        llIdTop = v.findViewById(R.id.ll_id_top);
        tvChartTitle = v.findViewById(R.id.tv_chart_title);
        tvMoodSectionTitle = v.findViewById(R.id.tv_mood_section_title);
        tvBestDayLabel = v.findViewById(R.id.tv_best_day_label);

        if (cardId != null)
            cardId.setOnClickListener(view ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .addToBackStack(null)
                            .commit()
            );

        // Pill icon — circle clipped, opens pill image picker
        ImageView ivIdIcon = v.findViewById(R.id.iv_id_icon);
        if (ivIdIcon != null) {
            ivIdIcon.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            ivIdIcon.setClipToOutline(true);
            ivIdIcon.setOnClickListener(view -> pickImage("pill"));
        }

        // Avatar — rounded corners, opens avatar image picker
        ImageView ivAvatar = v.findViewById(R.id.iv_profile_avatar);
        if (ivAvatar != null) {
            final float r = getResources().getDisplayMetrics().density * 12f;
            ivAvatar.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), r);
                }
            });
            ivAvatar.setClipToOutline(true);
            ivAvatar.setOnClickListener(view -> pickImage("avatar"));
        }

        tabWeekly  = v.findViewById(R.id.tab_weekly);
        tabMonthly = v.findViewById(R.id.tab_monthly);

        tvAttendanceRate   = v.findViewById(R.id.tv_attendance_rate);
        tvAvgParticipation = v.findViewById(R.id.tv_avg_participation);
        tvSessionsStat     = v.findViewById(R.id.tv_sessions_stat);
        tvTopSubject       = v.findViewById(R.id.tv_top_subject);

        tvPresentCount = v.findViewById(R.id.tv_present_count);
        tvTopMood      = v.findViewById(R.id.tv_top_mood);
        tvTopMoodPct   = v.findViewById(R.id.tv_top_mood_pct);
        tvLateCount    = v.findViewById(R.id.tv_late_count);
        tvBestDay      = v.findViewById(R.id.tv_best_day);

        barChartParticipation = v.findViewById(R.id.bar_chart_participation);
        tvBestDayChip         = v.findViewById(R.id.tv_best_day_chip);
        tvWorstDayChip        = v.findViewById(R.id.tv_worst_day_chip);

        llSubjectsScroll = v.findViewById(R.id.ll_subjects_scroll);

        tvMoodHappyPct    = v.findViewById(R.id.tv_mood_happy_pct);
        tvMoodNeutralPct  = v.findViewById(R.id.tv_mood_neutral_pct);
        tvMoodStressedPct = v.findViewById(R.id.tv_mood_stressed_pct);

        llSchedule = v.findViewById(R.id.ll_schedule_container);
    }

    private void setupTabs() {
        if (tabWeekly == null || tabMonthly == null) return;

        int colorSelectedBlue        = Color.parseColor("#5A8BD0");
        int colorUnselectedLightBlue = Color.parseColor("#C5DBF8");

        tabWeekly.setOnClickListener(v -> {
            isWeekly = true;
            tabWeekly.setBackgroundResource(R.drawable.bg_tab_selected);
            tabWeekly.getBackground().setTint(colorSelectedBlue);
            tabWeekly.setTextColor(Color.WHITE);
            tabMonthly.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabMonthly.getBackground().setTint(colorUnselectedLightBlue);
            tabMonthly.setTextColor(colorSelectedBlue);
            loadData();
        });

        tabMonthly.setOnClickListener(v -> {
            isWeekly = false;
            tabMonthly.setBackgroundResource(R.drawable.bg_tab_selected);
            tabMonthly.getBackground().setTint(colorSelectedBlue);
            tabMonthly.setTextColor(Color.WHITE);
            tabWeekly.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabWeekly.getBackground().setTint(colorUnselectedLightBlue);
            tabWeekly.setTextColor(colorSelectedBlue);
            loadData();
        });
    }

    private void setupQuickActions(View v) {
        View btnLog = v.findViewById(R.id.btn_log_class);
        if (btnLog != null)
            btnLog.setOnClickListener(view ->
                    startActivity(new Intent(requireContext(), LogReflectionActivity.class)));

        View btnTrends = v.findViewById(R.id.btn_view_trends);
        if (btnTrends != null)
            btnTrends.setOnClickListener(view -> {
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).navigateTo(2); // ← changed
            });

        View btnHistory = v.findViewById(R.id.btn_view_history);
        if (btnHistory != null)
            btnHistory.setOnClickListener(view -> {
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).navigateTo(3); // ← changed
            });
    }

    private void pickImage(String target) {
        pendingPickTarget = target;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE
                && resultCode == android.app.Activity.RESULT_OK
                && data != null && data.getData() != null) {

            String uriStr = data.getData().toString();
            PrefsManager prefs = new PrefsManager(requireContext());

            if ("pill".equals(pendingPickTarget)) {
                prefs.setPillIcon(uriStr);
                ImageView iv = requireView().findViewById(R.id.iv_id_icon);
                if (iv != null) iv.setImageURI(data.getData());

            } else if ("avatar".equals(pendingPickTarget)) {
                prefs.setAvatarIcon(uriStr);
                ImageView iv = requireView().findViewById(R.id.iv_profile_avatar);
                if (iv != null) iv.setImageURI(data.getData());
            }
        }
    }
    private void populateHeader() {
        tvGreeting.setText("Your summary,");
        PrefsManager prefs = new PrefsManager(requireContext());
        String name = prefs.getUserName();
        tvUserName.setText(name);
        SimpleDateFormat dateFmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvDate.setText(dateFmt.format(new Date()));

        if (tvIdName     != null) tvIdName.setText(name);
        if (tvIdBirthday != null) tvIdBirthday.setText(prefs.getBirthday());
        if (tvIdSchool   != null) tvIdSchool.setText(prefs.getSchool());
        if (tvIdYear     != null) tvIdYear.setText(prefs.getYearLevel());
        if (barcodeView  != null) barcodeView.setData(name);

        if (llIdTop != null) {
            String savedColor = prefs.getIdCardColor();
            int bgColor = Color.parseColor(savedColor != null ? savedColor : "#FCE4EC");
            llIdTop.setBackgroundColor(bgColor);

            if (cardId != null) {
                int strokeColor = darkenColor(bgColor, 0.82f);
                float strokeWidth = getResources().getDisplayMetrics().density * 0.7f;

                GradientDrawable stroke = new GradientDrawable();
                stroke.setShape(GradientDrawable.RECTANGLE);
                stroke.setCornerRadius(18 * getResources().getDisplayMetrics().density);
                stroke.setStroke((int) strokeWidth, strokeColor);
                stroke.setColor(Color.TRANSPARENT);
                cardId.setForeground(stroke);
            }
        }

        // Both iv_id_icon and iv_profile_avatar use the same avatarUri
        String avatarUri = prefs.getAvatarIcon();
        android.net.Uri parsedUri = avatarUri != null ? android.net.Uri.parse(avatarUri) : null;

        ImageView ivPill = getView() != null ? getView().findViewById(R.id.iv_id_icon) : null;
        if (ivPill != null) {
            if (parsedUri != null) {
                try {
                    ivPill.setImageURI(parsedUri);
                    if (ivPill.getDrawable() == null) {
                        ivPill.setImageResource(R.drawable.ic_default_avatar);
                        prefs.setAvatarIcon(null);
                    }
                } catch (Exception e) {
                    ivPill.setImageResource(R.drawable.ic_default_avatar);
                    prefs.setAvatarIcon(null);
                }
            } else {
                ivPill.setImageResource(R.drawable.ic_default_avatar);
            }
        }

        ImageView ivAvatar = getView() != null ? getView().findViewById(R.id.iv_profile_avatar) : null;
        if (ivAvatar != null) {
            if (parsedUri != null) {
                try {
                    ivAvatar.setImageURI(parsedUri);
                    if (ivAvatar.getDrawable() == null) {
                        ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                        prefs.setAvatarIcon(null);
                    }
                } catch (Exception e) {
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                    prefs.setAvatarIcon(null);
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }

    private int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    private void loadData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ClassLog> allLogs     = db.classLogDao().getAllLogsSync();
            List<Subject>  allSubjects = db.subjectDao().getAllSubjectsSync();

            AnalyticsEngine.DashboardStats stats =
                    AnalyticsEngine.computeDashboardStats(allLogs, allSubjects, isWeekly);

            Calendar cal       = Calendar.getInstance();
            String   todayAbbr = AnalyticsEngine.getDayAbbr(cal.get(Calendar.DAY_OF_WEEK));
            long     dayStart  = getDayStart(cal);
            long     dayEnd    = dayStart + 86400000L;
            List<ClassLog> todayLogs = db.classLogDao().getLogsForDate(dayStart, dayEnd);

// Mood map filtered by period
            long moodPeriodStart = isWeekly ? getWeekStart() : getMonthStart();
            Map<String, Integer> moodMap = new HashMap<>();
            for (ClassLog l : allLogs) {
                if (l.mood != null && l.logDate >= moodPeriodStart)
                    moodMap.put(l.mood, moodMap.getOrDefault(l.mood, 0) + 1);
            }
            final int moodTotal = (int) allLogs.stream()
                    .filter(l -> l.logDate >= moodPeriodStart).count();

            long periodStart = isWeekly ? getWeekStart() : getMonthStart();
            int presentCount = 0, lateCount = 0;
            for (ClassLog l : allLogs) {
                if (l.logDate >= periodStart) {
                    if ("Present".equals(l.attendance))   presentCount++;
                    else if ("Late".equals(l.attendance)) lateCount++;
                }
            }

            final int fp = presentCount, fl = lateCount;
            final Map<String, Integer> fm = moodMap;
            final int total = allLogs.size();
            final int moodPeriodTotal = moodTotal;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                updateStatPills(stats);
                updateBentoGrid(stats, fp, fl, fm, total);
                updateBarChart(stats);
                // Filter logs for subject scroll by period
                long subjectPeriodStart = isWeekly ? getWeekStart() : getMonthStart();
                List<ClassLog> periodLogs = new ArrayList<>();
                for (ClassLog l : allLogs)
                    if (l.logDate >= subjectPeriodStart) periodLogs.add(l);
                final List<ClassLog> fPeriodLogs = periodLogs;
                buildSubjectScroll(allSubjects, fPeriodLogs);
                updateMoodBoxes(fm, moodPeriodTotal);
                buildScheduleList(allSubjects, todayAbbr, todayLogs);
            });
        });
    }

    private long getWeekStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private void updateStatPills(AnalyticsEngine.DashboardStats stats) {
        if (tvAttendanceRate   != null) tvAttendanceRate.setText(stats.attendanceRate + "%");
        if (tvAvgParticipation != null) tvAvgParticipation.setText(stats.avgParticipationLabel);
        if (tvSessionsStat     != null) tvSessionsStat.setText(String.valueOf(stats.notesCount));
        if (tvTopSubject       != null) tvTopSubject.setText(
                stats.topSubject != null ? stats.topSubject : "—");
    }

    private void updateBentoGrid(AnalyticsEngine.DashboardStats stats, int presentCount,
                                 int lateCount, Map<String, Integer> moodMap, int total) {
        if (tvPresentCount != null) tvPresentCount.setText(String.valueOf(presentCount));
        if (tvLateCount    != null) tvLateCount.setText(String.valueOf(lateCount));
        if (tvBestDay != null) {
            if (!isWeekly && stats.monthlyWeekParticipation != null) {
                // Show best week instead of best day
                String bestWk = "—";
                int bestVal = -1;
                for (int w = 1; w <= 4; w++) {
                    int val = stats.monthlyWeekParticipation.getOrDefault("Wk " + w, 0);
                    if (val > bestVal) { bestVal = val; bestWk = "Wk " + w; }
                }
                tvBestDay.setText(bestWk);
            } else {
                tvBestDay.setText(stats.bestDay != null ? stats.bestDay : "—");
            }
        }

        TextView tvPresentLabel = getView() != null ?
                getView().findViewById(R.id.tv_present_period) : null;
        if (tvPresentLabel != null)
            tvPresentLabel.setText(isWeekly ? "This week" : "This month");

        TextView tvLateLabel = getView() != null ?
                getView().findViewById(R.id.tv_late_period) : null;
        if (tvLateLabel != null)
            tvLateLabel.setText(isWeekly ? "This week" : "This month");

        String topMood = "Happy"; int topCount = 0;
        for (Map.Entry<String, Integer> e : moodMap.entrySet())
            if (e.getValue() > topCount) { topCount = e.getValue(); topMood = e.getKey(); }
        int topPct = total > 0 ? (int) Math.round(topCount * 100.0 / total) : 0;

        if (tvTopMood    != null) tvTopMood.setText(getMoodEmoji(topMood) + " " + topMood);
        if (tvTopMoodPct != null) tvTopMoodPct.setText(topPct + "% of sessions");

        if (tvBestDayLabel != null)
            tvBestDayLabel.setText(isWeekly ? "Best day" : "Best week");
    }

    private void updateBarChart(AnalyticsEngine.DashboardStats stats) {
        if (barChartParticipation == null) return;

        if (tvChartTitle != null)
            tvChartTitle.setText(isWeekly ? "PARTICIPATION THIS WEEK" : "PARTICIPATION THIS MONTH");

        if (isWeekly) {
            // Show Mon-Fri days
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < days.length; i++) {
                int val = stats.weeklyParticipation != null
                        ? stats.weeklyParticipation.getOrDefault(days[i], 0) : 0;
                entries.add(new BarEntry(i, val));
            }
            renderBarChart(entries, days);

        } else {
            // Show Week 1-4 for monthly view
            String[] weeks = {"Wk 1", "Wk 2", "Wk 3", "Wk 4"};
            List<BarEntry> entries = new ArrayList<>();
            long monthStart = getMonthStart();
            long weekMs = 7L * 24 * 60 * 60 * 1000;

            // Get all logs for this month
            AppDatabase db = AppDatabase.getInstance(requireContext());
            // We already have data from stats, compute weekly averages from monthStart
            for (int w = 0; w < 4; w++) {
                long wStart = monthStart + w * weekMs;
                long wEnd   = wStart + weekMs;
                // Use stats.weeklyParticipation is weekly only, so we compute month weeks inline
                entries.add(new BarEntry(w, 0)); // placeholder, will fill below
            }

            // Recompute monthly week buckets from allLogs via stats
            // Use monthlyWeekParticipation if available, else fallback
            if (stats.monthlyWeekParticipation != null) {
                entries.clear();
                for (int w = 0; w < 4; w++) {
                    int val = stats.monthlyWeekParticipation.getOrDefault("Wk " + (w+1), 0);
                    entries.add(new BarEntry(w, val));
                }
            }
            renderBarChart(entries, weeks);
        }

        if (!isWeekly && stats.monthlyWeekParticipation != null) {
            // Find best and worst week
            String bestWk = "—", worstWk = "—";
            int bestVal = -1, worstVal = Integer.MAX_VALUE;
            for (int w = 1; w <= 4; w++) {
                int val = stats.monthlyWeekParticipation.getOrDefault("Wk " + w, 0);
                if (val > bestVal)  { bestVal  = val; bestWk  = "Week " + w; }
                if (val < worstVal) { worstVal = val; worstWk = "Week " + w; }
            }
            if (tvBestDayChip  != null) tvBestDayChip.setText(bestWk);
            if (tvWorstDayChip != null) tvWorstDayChip.setText(worstWk);
        } else {
            if (tvBestDayChip  != null) tvBestDayChip.setText(stats.bestDay  != null ? stats.bestDay  : "—");
            if (tvWorstDayChip != null) tvWorstDayChip.setText(stats.worstDay != null ? stats.worstDay : "—");
        }
    }

    private void renderBarChart(List<BarEntry> entries, String[] labels) {
        int[] barColors = new int[entries.size()];
        String[] levelLabels = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            int val = (int) entries.get(i).getY();
            if (val >= 66)      { barColors[i] = Color.parseColor("#81C784"); levelLabels[i] = "High"; }
            else if (val >= 33) { barColors[i] = Color.parseColor("#9FA8DA"); levelLabels[i] = "Med"; }
            else                { barColors[i] = Color.parseColor("#FFCC80"); levelLabels[i] = "Low"; }
        }

        float radius = getResources().getDisplayMetrics().density * 14f;
        barChartParticipation.setRenderer(new com.classpulse.utils.RoundedBarChartRenderer(
                barChartParticipation,
                barChartParticipation.getAnimator(),
                barChartParticipation.getViewPortHandler(),
                radius));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(barColors);
        dataSet.setDrawValues(false);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        barChartParticipation.setData(barData);

        barChartParticipation.getDescription().setEnabled(false);
        barChartParticipation.getLegend().setEnabled(false);
        barChartParticipation.getAxisRight().setEnabled(false);
        barChartParticipation.getAxisLeft().setEnabled(false);
        barChartParticipation.setTouchEnabled(false);
        barChartParticipation.setDrawBarShadow(false);
        barChartParticipation.setDrawGridBackground(false);
        barChartParticipation.setExtraBottomOffset(8f);

        XAxis xAxis = barChartParticipation.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#94A3B8"));
        xAxis.setTextSize(11f);
        xAxis.setYOffset(4f);

        barChartParticipation.animateY(600);

        final String[] finalLabels = levelLabels;
        barChartParticipation.post(() -> drawLevelPills(finalLabels, barColors));
        barChartParticipation.invalidate();
    }

    private void drawLevelPills(String[] labels, int[] barColors) {
        if (barChartParticipation == null || !isAdded()) return;

        // Remove old pill container if present
        ViewGroup parent = (ViewGroup) barChartParticipation.getParent();
        View oldPills = parent.findViewWithTag("pill_row");
        if (oldPills != null) parent.removeView(oldPills);

        // Build a horizontal LinearLayout with 5 pill TextViews
        LinearLayout pillRow = new LinearLayout(requireContext());
        pillRow.setTag("pill_row");
        pillRow.setOrientation(LinearLayout.HORIZONTAL);
        pillRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        int pillColorMap[] = {
                Color.parseColor("#C8F0D8"), // High - green bg
                Color.parseColor("#E3E8FF"), // Med  - blue bg
                Color.parseColor("#FFF3CD"), // Low  - amber bg
        };
        int pillTextColorMap[] = {
                Color.parseColor("#2E7D52"),
                Color.parseColor("#3F51B5"),
                Color.parseColor("#B45309"),
        };

        for (int i = 0; i < labels.length; i++) {
            TextView pill = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            pill.setLayoutParams(lp);
            pill.setGravity(android.view.Gravity.CENTER);
            pill.setText(labels[i]);
            pill.setTextSize(9f);

            // Determine color index
            int ci = labels[i].equals("High") ? 0 : labels[i].equals("Med") ? 1 : 2;
            pill.setTextColor(pillTextColorMap[ci]);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(pillColorMap[ci]);
            bg.setCornerRadius(24f);
            pill.setBackground(bg);
            pill.setPadding(8, 4, 8, 4);

            pillRow.addView(pill);

            // Spacer between pills
            if (i < labels.length - 1) {
                View spacer = new View(requireContext());
                spacer.setLayoutParams(new LinearLayout.LayoutParams(
                        (int)(4 * getResources().getDisplayMetrics().density), 1));
                pillRow.addView(spacer);
            }
        }

        // Insert pill row right after the BarChart inside its parent LinearLayout
        int chartIndex = parent.indexOfChild(barChartParticipation);
        parent.addView(pillRow, chartIndex + 1);
    }

    private void buildSubjectScroll(List<Subject> subjects, List<ClassLog> logs) {
        if (llSubjectsScroll == null) return;
        llSubjectsScroll.removeAllViews();

        Map<Integer, Integer> presentMap = new HashMap<>(), totalMap = new HashMap<>();
        for (ClassLog l : logs) {
            totalMap.put(l.subjectId, totalMap.getOrDefault(l.subjectId, 0) + 1);
            if ("Present".equals(l.attendance) || "Late".equals(l.attendance))
                presentMap.put(l.subjectId, presentMap.getOrDefault(l.subjectId, 0) + 1);
        }

        int[] bgColors   = { Color.parseColor("#C8F0D8"), Color.parseColor("#C8DFF5"),
                Color.parseColor("#FEF5C0"), Color.parseColor("#E8D5F5"),
                Color.parseColor("#FCE4EC") };
        int[] textColors = { Color.parseColor("#2E7D52"), Color.parseColor("#1A5FA8"),
                Color.parseColor("#B45309"), Color.parseColor("#6B21A8"),
                Color.parseColor("#C2607A") };

        int dp8   = (int)(8  * getResources().getDisplayMetrics().density);
        int dp200 = (int)(200 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < subjects.size(); i++) {
            Subject s  = subjects.get(i);
            int ci  = i % bgColors.length;
            int t   = totalMap.getOrDefault(s.id, 0);
            int p   = presentMap.getOrDefault(s.id, 0);
            int pct = t > 0 ? (int) Math.round(p * 100.0 / t) : 0;

            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp8*2, dp8*2, dp8*2, dp8*2);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(36f);
            bg.setColor(bgColors[ci]);
            card.setBackground(bg);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp200,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp8*2);
            card.setLayoutParams(lp);

            TextView emoji = new TextView(requireContext());
            emoji.setText(getSubjectEmoji(s.name));
            emoji.setTextSize(22);
            card.addView(emoji);

            TextView name = new TextView(requireContext());
            name.setText(s.name);
            name.setTextSize(13);
            name.setTextColor(Color.parseColor("#1A1A2E"));
            name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            name.setMaxLines(1);
            card.addView(name);

            ProgressBar pb = new ProgressBar(requireContext(), null,
                    android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp8 + 4);
            pbLp.topMargin = dp8; pbLp.bottomMargin = dp8 / 2;
            pb.setLayoutParams(pbLp);
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setProgressDrawable(requireContext().getDrawable(R.drawable.progress_bar_blue));
            card.addView(pb);

            TextView pctTv = new TextView(requireContext());
            pctTv.setText(pct + "%");
            pctTv.setTextSize(14);
            pctTv.setTextColor(textColors[ci]);
            pctTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            card.addView(pctTv);

            llSubjectsScroll.addView(card);
        }

        if (subjects.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No subjects yet. Add them in Settings!");
            empty.setTextColor(Color.parseColor("#94A3B8"));
            empty.setTextSize(12);
            llSubjectsScroll.addView(empty);
        }
    }

    private void updateMoodBoxes(Map<String, Integer> moodMap, int total) {

        if (tvMoodSectionTitle != null)
            tvMoodSectionTitle.setText(isWeekly ? "MOOD THIS WEEK" : "MOOD THIS MONTH");

        int happy    = moodMap.getOrDefault("Happy", moodMap.getOrDefault("Focused", 0));
        int neutral  = moodMap.getOrDefault("Neutral",  0);
        int stressed = moodMap.getOrDefault("Stressed", 0);
        if (tvMoodHappyPct    != null) tvMoodHappyPct.setText(
                (total > 0 ? (int) Math.round(happy    * 100.0 / total) : 0) + "%");
        if (tvMoodNeutralPct  != null) tvMoodNeutralPct.setText(
                (total > 0 ? (int) Math.round(neutral  * 100.0 / total) : 0) + "%");
        if (tvMoodStressedPct != null) tvMoodStressedPct.setText(
                (total > 0 ? (int) Math.round(stressed * 100.0 / total) : 0) + "%");
    }

    private void buildScheduleList(List<Subject> subjects, String todayAbbr,
                                   List<ClassLog> todayLogs) {
        if (llSchedule == null) return;
        llSchedule.removeAllViews();

        List<Subject> todaySubjects = new ArrayList<>();
        for (Subject s : subjects)
            if (s.isScheduledOnDay(todayAbbr)) todaySubjects.add(s);
        if (todaySubjects.isEmpty()) return;

        Map<Integer, ClassLog> loggedMap = new HashMap<>();
        for (ClassLog l : todayLogs) loggedMap.put(l.subjectId, l);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Subject s : todaySubjects) {
            View item = inflater.inflate(R.layout.item_schedule, llSchedule, false);
            ((TextView) item.findViewById(R.id.tv_subject_name)).setText(s.name);
            ((TextView) item.findViewById(R.id.tv_schedule_time)).setText(s.getFormattedSchedule());
            TextView badge = item.findViewById(R.id.tv_log_status);
            View dot       = item.findViewById(R.id.dot_status);
            if (loggedMap.containsKey(s.id)) {
                badge.setText("Logged ✓");
                badge.setTextColor(Color.parseColor("#4CAF50"));
                badge.setBackgroundResource(R.drawable.bg_badge_logged);
                dot.setBackgroundResource(R.drawable.dot_green);
            } else {
                badge.setText("Log now");
                badge.setTextColor(Color.parseColor("#E65100"));
                badge.setBackgroundResource(R.drawable.bg_badge_log_now);
                dot.setBackgroundResource(R.drawable.dot_yellow);
                int sid = s.id; String sname = s.name;
                badge.setOnClickListener(vv -> {
                    Intent i = new Intent(requireContext(), LogReflectionActivity.class);
                    i.putExtra("subject_id", sid);
                    i.putExtra("subject_name", sname);
                    startActivity(i);
                });
            }
            llSchedule.addView(item);
        }
    }

    private String getMoodEmoji(String mood) {
        switch (mood) {
            case "Happy": case "Focused": return "😊";
            case "Neutral":               return "😐";
            case "Tired":                 return "😴";
            case "Stressed":              return "😰";
            default:                      return "😊";
        }
    }

    private String getSubjectEmoji(String name) {
        if (name == null) return "📚";
        String n = name.toLowerCase();
        if (n.contains("robot"))                    return "🤖";
        if (n.contains("mob") || n.contains("app")) return "📱";
        if (n.contains("math"))                     return "📐";
        if (n.contains("sci"))                      return "🔬";
        if (n.contains("eng"))                      return "📝";
        return "📚";
    }

    private long getDayStart(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);      c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long getMonthStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1); c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);       c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    @Override public void onResume()  { super.onResume();  populateHeader(); loadData(); }
    @Override public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}