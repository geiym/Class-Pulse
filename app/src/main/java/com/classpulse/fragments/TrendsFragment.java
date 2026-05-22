package com.classpulse.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
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

public class TrendsFragment extends Fragment {

    private TextView tvTrendMonth, tvTrajectoryText;
    private TextView tvSlumpTitle, tvSlumpBody;
    private TextView tvCurrentStreak, tvLoggedDays, tvMissedDays;
    private TextView tvMostImproved, tvMostImprovedDelta;
    private TextView tvNeedsAttention, tvNeedsAttentionLabel;
    private TextView tvSmartInsightTitle, tvSmartInsightBody;
    private LinearLayout llMoodParticipation, llSubjectRanking;
    private LinearLayout llTrajectoryBars, llStreakDots;
    private GridLayout heatmapGrid;
    private boolean geminiInsightGenerated = false;

    private ProgressBar pbGeminiLoading;
    private TextView    tvGeminiLabel;

    private List<ClassLog> allLogs     = new ArrayList<>();
    private List<Subject>  allSubjects = new ArrayList<>();
    private Calendar heatmapCal    = Calendar.getInstance();
    private boolean  showBackButton = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static TrendsFragment newInstance(boolean showBackButton) {
        TrendsFragment f = new TrendsFragment();
        Bundle args = new Bundle();
        args.putBoolean("show_back", showBackButton);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        loadData();

        showBackButton = getArguments() != null
                && getArguments().getBoolean("show_back", false);
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setVisibility(showBackButton ? View.VISIBLE : View.GONE);
            btnBack.setOnClickListener(v ->
                    requireActivity().onBackPressed()); // ← FIXED
        }

        view.findViewById(R.id.btn_heatmap_prev).setOnClickListener(v -> {
            heatmapCal.add(Calendar.MONTH, -1);
            renderHeatmapGrid();
        });
        view.findViewById(R.id.btn_heatmap_next).setOnClickListener(v -> {
            heatmapCal.add(Calendar.MONTH, 1);
            renderHeatmapGrid();
        });
    }

    private void bindViews(View v) {
        tvTrendMonth          = v.findViewById(R.id.tv_trend_month);
        tvTrajectoryText      = v.findViewById(R.id.tv_trajectory_text);
        tvSlumpTitle          = v.findViewById(R.id.tv_slump_title);
        tvSlumpBody           = v.findViewById(R.id.tv_slump_body);
        tvCurrentStreak       = v.findViewById(R.id.tv_current_streak);
        tvLoggedDays          = v.findViewById(R.id.tv_logged_days);
        tvMissedDays          = v.findViewById(R.id.tv_missed_days);
        tvMostImproved        = v.findViewById(R.id.tv_most_improved);
        tvMostImprovedDelta   = v.findViewById(R.id.tv_most_improved_delta);
        tvNeedsAttention      = v.findViewById(R.id.tv_needs_attention);
        tvNeedsAttentionLabel = v.findViewById(R.id.tv_needs_attention_label);
        tvSmartInsightTitle   = v.findViewById(R.id.tv_smart_insight_title);
        tvSmartInsightBody    = v.findViewById(R.id.tv_smart_insight_body);
        llMoodParticipation   = v.findViewById(R.id.ll_mood_participation);
        llSubjectRanking      = v.findViewById(R.id.ll_subject_ranking);
        llTrajectoryBars      = v.findViewById(R.id.ll_trajectory_bars);
        llStreakDots          = v.findViewById(R.id.ll_streak_dots);
        heatmapGrid           = v.findViewById(R.id.heatmap_grid);
        pbGeminiLoading       = v.findViewById(R.id.pbGeminiLoading);
        tvGeminiLabel         = v.findViewById(R.id.tvGeminiLabel);
    }

    private void loadData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            allLogs     = db.classLogDao().getAllLogsSync();
            allSubjects = db.subjectDao().getAllSubjectsSync();

            List<AnalyticsEngine.MoodCorrelation>   moodCorr   = AnalyticsEngine.computeMoodCorrelations(allLogs);
            List<AnalyticsEngine.SubjectEngagement> ranking    = AnalyticsEngine.computeSubjectRankings(allLogs, allSubjects);
            AnalyticsEngine.TrajectoryResult        trajectory = AnalyticsEngine.computeTrajectory(allLogs);
            AnalyticsEngine.SmartFeedback           feedback   = AnalyticsEngine.generateSmartFeedback(allLogs, allSubjects);

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                updateMonthLabel();
                renderHeatmapGrid();
                renderMoodParticipation(moodCorr);
                renderSubjectRanking(ranking);
                renderTrajectory(trajectory);
                renderSlump(trajectory);
                renderStreak();
                renderSpotlight(ranking);
                renderSmartInsight(feedback);
                if (!geminiInsightGenerated) {
                    geminiInsightGenerated = true;
                    generateGeminiInsight();
                }
            });
        });
    }

    private void generateGeminiInsight() {
        if (allLogs == null || allLogs.isEmpty()) return;

        if (pbGeminiLoading != null) pbGeminiLoading.setVisibility(View.VISIBLE);
        if (tvGeminiLabel   != null) tvGeminiLabel.setVisibility(View.GONE);
        if (tvSmartInsightTitle != null)
            tvSmartInsightTitle.setText("💡 AI INSIGHT · ANALYZING...");
        if (tvSmartInsightBody != null)
            tvSmartInsightBody.setText("Generating your personalized insight...");

        String prompt = buildGeminiPrompt();
        String apiKey = "AIzaSyDjiE4-lDu5NDU__KwvSbuoZoXKnjk93ZQ";

// 2. API CALL: Ginagamit ang OkHttp para ipadala ang prompt sa Gemini API.
        executor.execute(() -> {
            try {
                org.json.JSONObject part = new org.json.JSONObject();
                part.put("text", prompt);

                org.json.JSONArray parts = new org.json.JSONArray();
                parts.put(part);

                org.json.JSONObject content = new org.json.JSONObject();
                content.put("parts", parts);

                org.json.JSONArray contents = new org.json.JSONArray();
                contents.put(content);

                org.json.JSONObject requestBody = new org.json.JSONObject();
                requestBody.put("contents", contents);

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                okhttp3.RequestBody body = okhttp3.RequestBody.create(
                        requestBody.toString(),
                        okhttp3.MediaType.parse("application/json; charset=utf-8"));

                String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                        + "gemini-2.0-flash-lite:generateContent?key=" + apiKey;
                // (Dito binubuo ang JSON request na naglalaman ng ating Prompt)
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                String responseBody = response.body() != null
                        ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    throw new Exception("API error " + response.code() + ": " + responseBody);
                }

                org.json.JSONObject json = new org.json.JSONObject(responseBody);
                // 3. RESPONSE HANDLING: Kinukuha ang sagot mula sa AI.
                String text = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                if (!isAdded()) return;
                // 4. UI RENDERING: Ipinapakita ang text sa screen gamit ang "Typewriter Effect."
                requireActivity().runOnUiThread(() -> {
                    if (pbGeminiLoading != null)
                        pbGeminiLoading.setVisibility(View.GONE);
                    if (tvSmartInsightTitle != null)
                        tvSmartInsightTitle.setText("💡 AI PERSONAL INSIGHT");
                    if (tvSmartInsightBody != null)
                        typewriterEffect(tvSmartInsightBody, text);
                    if (tvGeminiLabel != null)
                        tvGeminiLabel.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                // Fallback: Kapag walang internet, ang Rule-Based Smart Feedback ang ipapakita.
                android.util.Log.e("GEMINI", "Error: " + e.getMessage(), e);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (pbGeminiLoading != null)
                        pbGeminiLoading.setVisibility(View.GONE);
                    if (tvSmartInsightTitle != null)
                        tvSmartInsightTitle.setText("💡 SMART INSIGHT · THIS MONTH");
                    if (tvGeminiLabel != null)
                        tvGeminiLabel.setVisibility(View.GONE);
                });
            }
        });
    }

    private void typewriterEffect(TextView tv, String text) {
        tv.setText("");
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final int[] index = {0};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < text.length()) {
                    tv.append(String.valueOf(text.charAt(index[0])));
                    index[0]++;
                    handler.postDelayed(this, 18);
                }
            }
        };
        handler.post(runnable);
    }

    private String buildGeminiPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an academic coach for a student. ");
        sb.append("Analyze their class attendance and participation data below ");
        sb.append("and give a 2-3 sentence personalized insight. Be brief and direct. ");
        sb.append("Identify patterns, highlight strengths, and give 1-2 specific tips. ");
        sb.append("Be encouraging and direct. Do not use bullet points.\n\n");

        int totalLogs = allLogs.size();
        int presentCount = 0, lateCount = 0, absentCount = 0;
        int highPart = 0, medPart = 0, lowPart = 0;
        Map<String, Integer> moodCount = new HashMap<>();
        Map<String, Integer> dayCount  = new HashMap<>();

        for (ClassLog log : allLogs) {
            if ("Present".equals(log.attendance))      presentCount++;
            else if ("Late".equals(log.attendance))    lateCount++;
            else if ("Absent".equals(log.attendance))  absentCount++;

            if ("High".equals(log.participation))        highPart++;
            else if ("Medium".equals(log.participation)) medPart++;
            else                                         lowPart++;

            if (log.mood != null)
                moodCount.put(log.mood, moodCount.getOrDefault(log.mood, 0) + 1);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(log.logDate);
            String day = getDayName(c.get(Calendar.DAY_OF_WEEK));
            dayCount.put(day, dayCount.getOrDefault(day, 0) + 1);
        }

        int attPct  = totalLogs > 0 ? (presentCount + lateCount) * 100 / totalLogs : 0;
        int highPct = totalLogs > 0 ? highPart * 100 / totalLogs : 0;

        // 2. DATA INJECTION: Kinukuha natin ang mga RESULTA mula sa ating RULES.
        // Hindi natin pinapabasa sa AI ang libo-libong logs; binibigay lang natin ang summary.
        sb.append("STUDENT DATA SUMMARY:\n");
        sb.append("Total sessions logged: ").append(totalLogs).append("\n");
        sb.append("Attendance rate: ").append(attPct).append("%");
        sb.append(" (Present: ").append(presentCount);
        sb.append(", Late: ").append(lateCount);
        sb.append(", Absent: ").append(absentCount).append(")\n");
        sb.append("Participation: High=").append(highPart);
        sb.append(", Medium=").append(medPart);
        sb.append(", Low=").append(lowPart).append("\n");
        sb.append("High participation rate: ").append(highPct).append("%\n");

        String topMood = "Unknown";
        int topMoodCount = 0;
        for (Map.Entry<String, Integer> e : moodCount.entrySet()) {
            if (e.getValue() > topMoodCount) {
                topMoodCount = e.getValue();
                topMood = e.getKey();
            }
        }
        sb.append("Most common mood: ").append(topMood).append("\n");

        String bestDay = "—", worstDay = "—";
        int bestVal = -1, worstVal = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> e : dayCount.entrySet()) {
            if (e.getValue() > bestVal)  { bestVal  = e.getValue(); bestDay  = e.getKey(); }
            if (e.getValue() < worstVal) { worstVal = e.getValue(); worstDay = e.getKey(); }
        }
        sb.append("Most active day: ").append(bestDay).append("\n");
        sb.append("Least active day: ").append(worstDay).append("\n");

        sb.append("\nPER SUBJECT BREAKDOWN:\n");
        Map<String, int[]> subjectStats = new HashMap<>();
        for (ClassLog log : allLogs) {
            if (log.subjectName == null) continue;
            if (!subjectStats.containsKey(log.subjectName))
                subjectStats.put(log.subjectName, new int[]{0, 0, 0});
            int[] s = subjectStats.get(log.subjectName);
            s[1]++;
            if ("Present".equals(log.attendance) || "Late".equals(log.attendance)) s[0]++;
            if ("High".equals(log.participation)) s[2]++;
        }
        for (Map.Entry<String, int[]> e : subjectStats.entrySet()) {
            int[] s   = e.getValue();
            int pct   = s[1] > 0 ? s[0] * 100 / s[1] : 0;
            int hp    = s[1] > 0 ? s[2] * 100 / s[1] : 0;
            sb.append("- ").append(e.getKey())
                    .append(": ").append(pct).append("% attendance, ")
                    .append(hp).append("% high participation\n");
        }

        sb.append("\nCurrent logging streak: ")
                .append(tvCurrentStreak != null ? tvCurrentStreak.getText() : "0")
                .append(" days\n");
        sb.append("\nProvide your coaching insight now:");
        return sb.toString();
    }

    private String getDayName(int dow) {
        switch (dow) {
            case Calendar.MONDAY:    return "Monday";
            case Calendar.TUESDAY:   return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY:  return "Thursday";
            case Calendar.FRIDAY:    return "Friday";
            case Calendar.SATURDAY:  return "Saturday";
            case Calendar.SUNDAY:    return "Sunday";
            default:                 return "Unknown";
        }
    }

    private void updateMonthLabel() {
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvTrendMonth.setText(fmt.format(heatmapCal.getTime()));
    }

    private void renderHeatmapGrid() {
        if (heatmapGrid == null) return;
        heatmapGrid.removeAllViews();
        updateMonthLabel();

        float dp = getResources().getDisplayMetrics().density;
        int cellSize = (int)(36 * dp);
        Map<Integer, String> dayLevel = buildDayLevelMap();

        Calendar cal = (Calendar) heatmapCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow    = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        int todayDay = (heatmapCal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && heatmapCal.get(Calendar.YEAR) == today.get(Calendar.YEAR))
                ? today.get(Calendar.DAY_OF_MONTH) : -1;

        for (int i = 0; i < firstDow; i++) {
            View blank = new View(requireContext());
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = cellSize; lp.height = cellSize;
            lp.columnSpec = GridLayout.spec(i % 7, 1f);
            blank.setLayoutParams(lp);
            heatmapGrid.addView(blank);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            int col = (firstDow + day - 1) % 7;
            TextView cell = new TextView(requireContext());
            cell.setText(String.valueOf(day));
            cell.setGravity(android.view.Gravity.CENTER);
            cell.setTextSize(11);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = cellSize; lp.height = cellSize;
            lp.columnSpec = GridLayout.spec(col, 1f);
            lp.setMargins(2, 2, 2, 2);
            cell.setLayoutParams(lp);

            String level = dayLevel.get(day);
            if (level != null) {
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                switch (level) {
                    case "High":
                        bg.setColor(Color.parseColor("#A8D8A8"));
                        cell.setTextColor(Color.parseColor("#2E7D52"));
                        break;
                    case "Medium":
                        bg.setColor(Color.parseColor("#F5EFC0"));
                        cell.setTextColor(Color.parseColor("#B45309"));
                        break;
                    case "Low":
                        bg.setColor(Color.parseColor("#F5C8C8"));
                        cell.setTextColor(Color.parseColor("#B03030"));
                        break;
                }
                cell.setBackground(bg);
            } else if (day == todayDay) {
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                bg.setStroke((int)(2 * dp), Color.parseColor("#E07060"));
                bg.setColor(Color.TRANSPARENT);
                cell.setBackground(bg);
                cell.setTextColor(Color.parseColor("#E07060"));
            } else {
                cell.setTextColor(Color.parseColor("#1C1C1E"));
            }
            heatmapGrid.addView(cell);
        }
    }

    private Map<Integer, String> buildDayLevelMap() {
        Map<Integer, List<Integer>> dayScores = new HashMap<>();
        for (ClassLog log : allLogs) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(log.logDate);
            if (c.get(Calendar.MONTH) != heatmapCal.get(Calendar.MONTH)
                    || c.get(Calendar.YEAR) != heatmapCal.get(Calendar.YEAR)) continue;
            int d = c.get(Calendar.DAY_OF_MONTH);
            if (!dayScores.containsKey(d)) dayScores.put(d, new ArrayList<>());
            dayScores.get(d).add(log.getParticipationScore());
        }
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> e : dayScores.entrySet()) {
            double avg = 0;
            for (int s : e.getValue()) avg += s;
            avg /= e.getValue().size();
            result.put(e.getKey(), avg >= 2.5 ? "High" : avg >= 1.5 ? "Medium" : "Low");
        }
        return result;
    }

    private void renderMoodParticipation(List<AnalyticsEngine.MoodCorrelation> correlations) {
        llMoodParticipation.removeAllViews();
        if (correlations.isEmpty()) {
            addMoodRow("😊", "Focused", "—", "#8E8E93", "—");
            addMoodRow("😐", "Neutral",  "—", "#8E8E93", "—");
            addMoodRow("😴", "Tired",    "—", "#8E8E93", "—");
            addMoodRow("😰", "Stressed", "—", "#8E8E93", "—");
            return;
        }
        List<AnalyticsEngine.MoodCorrelation> list = new ArrayList<>(correlations);
        for (int i = 0; i < list.size(); i += 2) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, (int)(8 * getResources().getDisplayMetrics().density));
            row.setLayoutParams(rp);
            row.addView(makeMoodCell(list.get(i)));
            if (i + 1 < list.size()) row.addView(makeMoodCell(list.get(i + 1)));
            llMoodParticipation.addView(row);
        }
    }

    private View makeMoodCell(AnalyticsEngine.MoodCorrelation c) {
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginEnd((int)(6 * dp));
        lp.bottomMargin = (int)(8 * dp);
        cell.setLayoutParams(lp);
        cell.setPadding((int)(10*dp),(int)(10*dp),(int)(10*dp),(int)(10*dp));

        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#FFFFFF"));
        bg.setCornerRadius(12 * dp);
        cell.setBackground(bg);

        TextView emoji = new TextView(requireContext());
        emoji.setText(getMoodEmoji(c.mood));
        emoji.setTextSize(20);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ep.setMarginEnd((int)(8*dp));
        emoji.setLayoutParams(ep);

        LinearLayout right = new LinearLayout(requireContext());
        right.setOrientation(LinearLayout.VERTICAL);
        right.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView badge = new TextView(requireContext());
        badge.setText(c.dominantParticipation + " Part");
        badge.setTextSize(12);
        badge.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        badge.setPadding((int)(10*dp),(int)(4*dp),(int)(10*dp),(int)(4*dp));

        int badgeColor, badgeBg;
        if ("High".equals(c.dominantParticipation)) {
            badgeColor = Color.parseColor("#2E7D52"); badgeBg = Color.parseColor("#C8F0D8");
        } else if ("Medium".equals(c.dominantParticipation)) {
            badgeColor = Color.parseColor("#1A5FA8"); badgeBg = Color.parseColor("#C8DCF5");
        } else {
            badgeColor = Color.parseColor("#B03030"); badgeBg = Color.parseColor("#F5C8C8");
        }
        badge.setTextColor(badgeColor);
        android.graphics.drawable.GradientDrawable pillBg =
                new android.graphics.drawable.GradientDrawable();
        pillBg.setColor(badgeBg);
        pillBg.setCornerRadius(20 * dp);
        badge.setBackground(pillBg);

        TextView pct = new TextView(requireContext());
        pct.setText(c.percentage + "%");
        pct.setTextSize(13);
        pct.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        pct.setTextColor(badgeColor);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pp.topMargin = (int)(2*dp);
        pct.setLayoutParams(pp);

        right.addView(badge);
        right.addView(pct);
        cell.addView(emoji);
        cell.addView(right);
        return cell;
    }

    private void addMoodRow(String emoji, String mood, String part, String color, String pct) {}

    private void renderSubjectRanking(List<AnalyticsEngine.SubjectEngagement> ranking) {
        llSubjectRanking.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;
        if (ranking.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("No subjects yet. Add subjects to see ranking.");
            tv.setTextColor(Color.parseColor("#8E8E93"));
            tv.setTextSize(13);
            llSubjectRanking.addView(tv);
            return;
        }
        for (int i = 0; i < ranking.size(); i += 2) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, (int)(10*dp));
            row.setLayoutParams(rp);
            row.addView(makeRankCell(ranking.get(i), i + 1, dp));
            if (i + 1 < ranking.size())
                row.addView(makeRankCell(ranking.get(i + 1), i + 2, dp));
            llSubjectRanking.addView(row);
        }
    }

    private View makeRankCell(AnalyticsEngine.SubjectEngagement se, int rank, float dp) {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginEnd((int)(8*dp));
        cell.setLayoutParams(lp);

        TextView rankTv = new TextView(requireContext());
        rankTv.setText(String.valueOf(rank));
        rankTv.setTextSize(12);
        rankTv.setTextColor(Color.WHITE);
        rankTv.setGravity(android.view.Gravity.CENTER);
        int badgeSize = (int)(22*dp);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(badgeSize, badgeSize);
        bp.setMarginEnd((int)(8*dp));
        rankTv.setLayoutParams(bp);
        android.graphics.drawable.GradientDrawable badgeBg =
                new android.graphics.drawable.GradientDrawable();
        badgeBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        badgeBg.setColor(getRankColor(rank));
        rankTv.setBackground(badgeBg);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(requireContext());
        name.setText(se.subjectName);
        name.setTextSize(13);
        name.setTextColor(Color.parseColor("#1C1C1E"));
        name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        name.setMaxLines(1);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);

        android.widget.ProgressBar pb = new android.widget.ProgressBar(
                requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int)(6*dp));
        pbLp.topMargin = (int)(4*dp);
        pb.setLayoutParams(pbLp);
        pb.setMax(100);
        pb.setProgress((int)se.engagementScore);

        int barColor = se.engagementScore >= 70 ? Color.parseColor("#5CAD72")
                : se.engagementScore >= 40 ? Color.parseColor("#E0A050")
                : Color.parseColor("#D95F6A");
        if (pb.getProgressDrawable() != null) {
            pb.getProgressDrawable().setColorFilter(
                    new android.graphics.PorterDuffColorFilter(barColor,
                            android.graphics.PorterDuff.Mode.SRC_IN));
        }

        TextView score = new TextView(requireContext());
        score.setText((int)se.engagementScore + "%");
        score.setTextSize(12);
        score.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        score.setTextColor(barColor);

        info.addView(name);
        info.addView(pb);

        LinearLayout rowInner = new LinearLayout(requireContext());
        rowInner.setOrientation(LinearLayout.HORIZONTAL);
        rowInner.setGravity(android.view.Gravity.CENTER_VERTICAL);
        rowInner.addView(rankTv);
        rowInner.addView(info);
        rowInner.addView(score);
        cell.addView(rowInner);
        return cell;
    }

    private void renderTrajectory(AnalyticsEngine.TrajectoryResult result) {
        tvTrajectoryText.setText(result.description);
        llTrajectoryBars.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;
        long now  = System.currentTimeMillis();
        long week = 7L * 24 * 60 * 60 * 1000;
        String[] weekLabels = {"Wk 1","Wk 2","Wk 3","Wk 4"};
        int[] weekColors = {
                Color.parseColor("#E0A050"), Color.parseColor("#5CAD72"),
                Color.parseColor("#E07060"), Color.parseColor("#5B8DCC")
        };
        for (int w = 3; w >= 0; w--) {
            long wStart = now - (w + 1) * week;
            long wEnd   = now - w * week;
            List<ClassLog> wLogs = new ArrayList<>();
            for (ClassLog l : allLogs)
                if (l.logDate >= wStart && l.logDate < wEnd) wLogs.add(l);
            double avg = 0;
            for (ClassLog l : wLogs) avg += l.getParticipationScore();
            avg = wLogs.isEmpty() ? 0.5 : avg / wLogs.size();
            int barH = (int)(Math.max(10, avg / 3.0 * 60) * dp);

            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            colLp.setMarginEnd((int)(4*dp));
            col.setLayoutParams(colLp);

            View bar = new View(requireContext());
            android.graphics.drawable.GradientDrawable barBg =
                    new android.graphics.drawable.GradientDrawable();
            barBg.setColor(weekColors[3 - w]);
            barBg.setCornerRadii(new float[]{8*dp,8*dp,8*dp,8*dp,0,0,0,0});
            bar.setBackground(barBg);
            bar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, barH));

            TextView wkLabel = new TextView(requireContext());
            wkLabel.setText(weekLabels[3 - w]);
            wkLabel.setTextSize(10);
            wkLabel.setTextColor(Color.parseColor("#8E8E93"));
            wkLabel.setGravity(android.view.Gravity.CENTER);

            col.addView(bar);
            col.addView(wkLabel);
            llTrajectoryBars.addView(col);
        }
    }

    private void renderSlump(AnalyticsEngine.TrajectoryResult result) {
        View slumpBox = tvSlumpTitle.getParent() instanceof View
                ? (View) tvSlumpTitle.getParent() : null;
        if (result.hasSlump) {
            tvSlumpTitle.setText("⚠️ Slump detected on " + result.slumpDay + "s");
            tvSlumpTitle.setTextColor(Color.parseColor("#D95F6A"));
            tvSlumpBody.setText("Your " + result.slumpDay
                    + " engagement is consistently low. Try a review session or extra prep!");
            if (slumpBox != null) {
                android.graphics.drawable.GradientDrawable slumpBg =
                        new android.graphics.drawable.GradientDrawable();
                slumpBg.setColor(Color.parseColor("#FFEBEE"));
                slumpBg.setCornerRadius(10 * getResources().getDisplayMetrics().density);
                slumpBox.setBackground(slumpBg);
            }
        } else {
            tvSlumpTitle.setText("No slump detected");
            tvSlumpTitle.setTextColor(Color.parseColor("#2E7D52"));
            tvSlumpBody.setText("You haven't had 3 consecutive low-engagement days. Keep the momentum going!");
            if (slumpBox != null) {
                android.graphics.drawable.GradientDrawable slumpBg =
                        new android.graphics.drawable.GradientDrawable();
                slumpBg.setColor(Color.parseColor("#E8F5E9"));
                slumpBg.setCornerRadius(10 * getResources().getDisplayMetrics().density);
                slumpBox.setBackground(slumpBg);
            }
        }
    }

    private void renderStreak() {
        llStreakDots.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, Boolean> loggedDates = new HashMap<>();
        for (ClassLog l : allLogs) {
            String dateKey = fmt.format(new Date(l.logDate));
            loggedDates.put(dateKey, true);
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_MONTH, -13);

        String[] dayAbbrs = {"M","T","W","TH","F","S","S"};
        int streak = 0, logged = 0, missed = 0, tempStreak = 0;
        boolean[] wasLoggedArr = new boolean[14];
        int[] dowIdxArr = new int[14];

        for (int i = 0; i < 14; i++) {
            String dateKey = fmt.format(c.getTime());
            wasLoggedArr[i] = loggedDates.containsKey(dateKey);
            dowIdxArr[i] = (c.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
            if (wasLoggedArr[i]) {
                logged++;
                tempStreak++;
                streak = Math.max(streak, tempStreak);
            } else {
                missed++;
                tempStreak = 0;
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        int pillW = (int)(22*dp), pillH = (int)(46*dp), dotSize = (int)(5*dp);
        llStreakDots.setOrientation(LinearLayout.HORIZONTAL);
        llStreakDots.setGravity(android.view.Gravity.CENTER_VERTICAL);

        for (int i = 0; i < 14; i++) {
            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView pill = new TextView(requireContext());
            pill.setGravity(android.view.Gravity.CENTER);
            pill.setText(wasLoggedArr[i] ? "✓" : "");
            pill.setTextSize(11);
            pill.setTextColor(Color.WHITE);
            pill.setLayoutParams(new LinearLayout.LayoutParams(pillW, pillH));
            android.graphics.drawable.GradientDrawable pillBg =
                    new android.graphics.drawable.GradientDrawable();
            pillBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            pillBg.setCornerRadius(20 * dp);
            pillBg.setColor(wasLoggedArr[i]
                    ? Color.parseColor("#3DBFA0") : Color.parseColor("#F5C8C8"));
            pill.setBackground(pillBg);

            TextView dayLabel = new TextView(requireContext());
            dayLabel.setText(dayAbbrs[dowIdxArr[i]]);
            dayLabel.setTextSize(8);
            dayLabel.setTextColor(Color.parseColor("#8E8E93"));
            dayLabel.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams dlLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dlLp.topMargin = (int)(3*dp);
            dayLabel.setLayoutParams(dlLp);

            col.addView(pill);
            col.addView(dayLabel);
            llStreakDots.addView(col);

            if (i < 13) {
                android.widget.FrameLayout dotHolder =
                        new android.widget.FrameLayout(requireContext());
                dotHolder.setLayoutParams(new LinearLayout.LayoutParams(
                        (int)(8*dp), pillH + (int)(3*dp) + (int)(16*dp)));
                View dot = new View(requireContext());
                android.widget.FrameLayout.LayoutParams dotLp =
                        new android.widget.FrameLayout.LayoutParams(dotSize, dotSize);
                dotLp.gravity = android.view.Gravity.CENTER;
                dotLp.topMargin = -(int)(10*dp);
                dot.setLayoutParams(dotLp);
                android.graphics.drawable.GradientDrawable dotBg =
                        new android.graphics.drawable.GradientDrawable();
                dotBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                dotBg.setColor(Color.parseColor("#C7C7CC"));
                dot.setBackground(dotBg);
                dotHolder.addView(dot);
                llStreakDots.addView(dotHolder);
            }
        }

        tvCurrentStreak.setText(String.valueOf(streak));
        tvLoggedDays.setText(String.valueOf(logged));
        tvMissedDays.setText(String.valueOf(missed));
    }

    private void renderSpotlight(List<AnalyticsEngine.SubjectEngagement> ranking) {
        if (ranking.isEmpty()) {
            tvMostImproved.setText("—");
            tvNeedsAttention.setText("—");
            return;
        }
        AnalyticsEngine.SubjectEngagement best  = ranking.get(0);
        AnalyticsEngine.SubjectEngagement worst = ranking.get(ranking.size() - 1);
        tvMostImproved.setText(best.subjectName.toUpperCase());
        tvMostImprovedDelta.setText((int)best.engagementScore + "% engagement");
        tvNeedsAttention.setText(worst.subjectName.toUpperCase());
        tvNeedsAttentionLabel.setText("Low " + worst.logCount + " session(s)");
    }

    private void renderSmartInsight(AnalyticsEngine.SmartFeedback feedback) {
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM", Locale.getDefault());
        tvSmartInsightTitle.setText("💡 SMART INSIGHT · " + fmt.format(new Date()).toUpperCase());
        tvSmartInsightBody.setText(feedback.insight);
    }

    private String getMoodEmoji(String mood) {
        if (mood == null) return "😊";
        switch (mood) {
            case "Focused":  return "😊";
            case "Neutral":  return "😐";
            case "Tired":    return "😴";
            case "Stressed": return "😰";
            default:         return "😊";
        }
    }

    private int getRankColor(int rank) {
        switch (rank) {
            case 1: return Color.parseColor("#5CAD72");
            case 2: return Color.parseColor("#5B8DCC");
            case 3: return Color.parseColor("#E0A050");
            default: return Color.parseColor("#D95F6A");
        }
    }

    @Override public void onResume()  { super.onResume();  loadData(); }
    @Override public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}