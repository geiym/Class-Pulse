package com.classpulse.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.classpulse.R;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;
import com.classpulse.utils.AnalyticsEngine;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private TextView tabWeekly, tabMonthly;
    private TextView tvChartTitle, tvAttendanceStat, tvAvgPartStat, tvNotesStat;
    private TextView tvBestDay, tvWorstDay;
    private BarChart barChart;
    private PieChart pieChart;
    private LinearLayout llSubjectComparison, llMoodLegend;

    private boolean isWeekly = true;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupTabs();
        loadData();
    }

    private void bindViews(View v) {
        tabWeekly = v.findViewById(R.id.tab_weekly);
        tabMonthly = v.findViewById(R.id.tab_monthly);
        tvChartTitle = v.findViewById(R.id.tv_chart_title);
        tvAttendanceStat = v.findViewById(R.id.tv_attendance_stat);
        tvAvgPartStat = v.findViewById(R.id.tv_avg_part_stat);
        tvNotesStat = v.findViewById(R.id.tv_notes_stat);
        tvBestDay = v.findViewById(R.id.tv_best_day);
        tvWorstDay = v.findViewById(R.id.tv_worst_day);
        barChart = v.findViewById(R.id.bar_chart_participation);
        pieChart = v.findViewById(R.id.pie_chart_mood);
        llSubjectComparison = v.findViewById(R.id.ll_subject_comparison);
        llMoodLegend = v.findViewById(R.id.ll_mood_legend);
    }

    private void setupTabs() {
        tabWeekly.setOnClickListener(v -> {
            isWeekly = true;
            tabWeekly.setBackgroundResource(R.drawable.bg_tab_selected);
            tabWeekly.setTextColor(Color.WHITE);
            tabMonthly.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabMonthly.setTextColor(Color.parseColor("#546E7A"));
            tvChartTitle.setText("PARTICIPATION THIS WEEK");
            loadData();
        });

        tabMonthly.setOnClickListener(v -> {
            isWeekly = false;
            tabMonthly.setBackgroundResource(R.drawable.bg_tab_selected);
            tabMonthly.setTextColor(Color.WHITE);
            tabWeekly.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabWeekly.setTextColor(Color.parseColor("#546E7A"));
            tvChartTitle.setText("PARTICIPATION THIS MONTH");
            loadData();
        });
    }

    private void loadData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ClassLog> logs = db.classLogDao().getAllLogsSync();
            List<Subject> subjects = db.subjectDao().getAllSubjectsSync();

            AnalyticsEngine.DashboardStats stats =
                    AnalyticsEngine.computeDashboardStats(logs, subjects, isWeekly);

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                updateStats(stats);
                updateBarChart(stats);
                updatePieChart(stats);
                updateSubjectComparison(stats);
            });
        });
    }

    private void updateStats(AnalyticsEngine.DashboardStats stats) {
        tvAttendanceStat.setText(stats.attendanceRate + "%");
        tvAvgPartStat.setText(stats.avgParticipation + "%");
        tvNotesStat.setText(String.valueOf(stats.notesCount));
        tvBestDay.setText(stats.bestDay);
        tvWorstDay.setText(stats.worstDay);
    }

    private void updateBarChart(AnalyticsEngine.DashboardStats stats) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < days.length; i++) {
            int val = stats.weeklyParticipation.getOrDefault(days[i], 0);
            entries.add(new BarEntry(i, val));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Participation");
        dataSet.setColors(new int[]{
                Color.parseColor("#1A237E"),
                Color.parseColor("#03A9F4"),
                Color.parseColor("#03A9F4"),
                Color.parseColor("#FFB300"),
                Color.parseColor("#1A237E")
        });
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.parseColor("#546E7A"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(Color.parseColor("#90A4AE"));
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(100f);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#90A4AE"));

        barChart.setTouchEnabled(false);
        barChart.animateY(600);
        barChart.invalidate();
    }

    private void updatePieChart(AnalyticsEngine.DashboardStats stats) {
        Map<String, Integer> mood = stats.moodDistribution;
        if (mood.isEmpty()) { pieChart.setNoDataText("No data yet"); pieChart.invalidate(); return; }

        List<PieEntry> entries = new ArrayList<>();
        int[] colors = {
                Color.parseColor("#03A9F4"),  // Focused
                Color.parseColor("#78909C"),  // Neutral
                Color.parseColor("#FF7043"),  // Tired
                Color.parseColor("#AB47BC")   // Stressed
        };
        String[] moodKeys = {"Focused", "Neutral", "Tired", "Stressed"};
        List<Integer> usedColors = new ArrayList<>();
        int total = 0;
        for (int v : mood.values()) total += v;

        for (int i = 0; i < moodKeys.length; i++) {
            int count = mood.getOrDefault(moodKeys[i], 0);
            if (count > 0) {
                entries.add(new PieEntry(count, ""));
                usedColors.add(colors[i]);
            }
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(usedColors);
        ds.setDrawValues(false);
        ds.setSliceSpace(2f);

        PieData pd = new PieData(ds);
        pieChart.setData(pd);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.animateY(600);
        pieChart.invalidate();

        // Build legend manually
        llMoodLegend.removeAllViews();
        String[] labels = {"Focused", "Neutral", "Tired", "Stressed"};
        for (int i = 0; i < labels.length; i++) {
            int count = mood.getOrDefault(labels[i], 0);
            if (count == 0) continue;
            int pct = total > 0 ? (int) Math.round(count * 100.0 / total) : 0;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, 6);
            row.setLayoutParams(rp);

            View dot = new View(requireContext());
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(10, 10);
            dp.setMarginEnd(8);
            dot.setLayoutParams(dp);
            dot.setBackgroundResource(R.drawable.dot_green);
            dot.getBackground().setTint(colors[i]);

            TextView label = new TextView(requireContext());
            label.setText(labels[i] + " " + pct + "%");
            label.setTextSize(12);
            label.setTextColor(Color.parseColor("#546E7A"));

            row.addView(dot);
            row.addView(label);
            llMoodLegend.addView(row);
        }
    }

    private void updateSubjectComparison(AnalyticsEngine.DashboardStats stats) {
        llSubjectComparison.removeAllViews();
        for (AnalyticsEngine.SubjectEngagement se : stats.subjectRankings) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, 10);
            row.setLayoutParams(rp);

            TextView name = new TextView(requireContext());
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            name.setLayoutParams(np);
            name.setText(se.subjectName);
            name.setTextSize(13);
            name.setTextColor(Color.parseColor("#1A237E"));
            name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

            ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0, 10, 1.5f);
            pp.setMarginStart(12);
            pp.setMarginEnd(8);
            pb.setLayoutParams(pp);
            pb.setProgressDrawable(requireContext().getDrawable(R.drawable.progress_bar_blue));
            pb.setProgress((int) se.engagementScore);

            TextView pct = new TextView(requireContext());
            pct.setText((int) se.engagementScore + "%");
            pct.setTextSize(12);
            pct.setTextColor(Color.parseColor("#546E7A"));

            row.addView(name);
            row.addView(pb);
            row.addView(pct);
            llSubjectComparison.addView(row);
        }

        if (stats.subjectRankings.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No data yet. Start logging your classes!");
            empty.setTextColor(Color.parseColor("#90A4AE"));
            empty.setTextSize(13);
            llSubjectComparison.addView(empty);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
