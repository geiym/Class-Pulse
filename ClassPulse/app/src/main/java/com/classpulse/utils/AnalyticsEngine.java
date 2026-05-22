package com.classpulse.utils;

import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassPulse Deterministic Analytics Engine
 *
 * Performs all trend analysis locally using rule-based heuristic logic.
 * No external APIs or network calls needed.
 */
public class AnalyticsEngine {

    // ─────────────────────────────────────────────────
    //  DATA CLASSES for returning structured results
    // ─────────────────────────────────────────────────

    public static class HeatmapCell {
        public String day;
        public String timeSlot;
        public String level;  // "High", "Medium", "Low", "None"
        public int count;

        public HeatmapCell(String day, String timeSlot, String level, int count) {
            this.day = day;
            this.timeSlot = timeSlot;
            this.level = level;
            this.count = count;
        }
    }

    public static class MoodCorrelation {
        public String mood;
        public String dominantParticipation;
        public int percentage;

        public MoodCorrelation(String mood, String dominantParticipation, int percentage) {
            this.mood = mood;
            this.dominantParticipation = dominantParticipation;
            this.percentage = percentage;
        }

        public String toInsightString() {
            String emoji = getMoodEmoji(mood);
            return "When " + emoji + " " + mood + " → " + dominantParticipation +
                    " participation " + percentage + "% of the time";
        }

        private String getMoodEmoji(String mood) {
            switch (mood) {
                case "Focused": return "😊";
                case "Neutral": return "😐";
                case "Tired":   return "😴";
                case "Stressed": return "😰";
                default: return "";
            }
        }
    }

    public static class SubjectEngagement {
        public String subjectName;
        public int subjectId;
        public double engagementScore;   // 0–100
        public String level;             // "High", "Medium", "Low"
        public int logCount;

        public SubjectEngagement(String subjectName, int subjectId, double engagementScore, int logCount) {
            this.subjectName = subjectName;
            this.subjectId = subjectId;
            this.engagementScore = engagementScore;
            this.logCount = logCount;
            if (engagementScore >= 70) this.level = "High";
            else if (engagementScore >= 40) this.level = "Medium";
            else this.level = "Low";
        }
    }

    public static class TrajectoryResult {
        public String direction;      // "Improving", "Stable", "Declining"
        public String arrow;          // "↑", "→", "↓"
        public String arrowColor;     // hex color
        public String description;
        public String slumpDay;       // nullable
        public boolean hasSlump;

        public TrajectoryResult(String direction, String arrow, String arrowColor,
                                String description, String slumpDay) {
            this.direction = direction;
            this.arrow = arrow;
            this.arrowColor = arrowColor;
            this.description = description;
            this.slumpDay = slumpDay;
            this.hasSlump = slumpDay != null && !slumpDay.isEmpty();
        }
    }

    public static class DashboardStats {
        public int attendanceRate;
        public int avgParticipation;  // 1=Low,2=Med,3=High scaled to %
        public String avgParticipationLabel;
        public String topSubject;
        public int notesCount;
        public String bestDay;
        public String worstDay;
        public Map<String, Integer> moodDistribution;
        public List<SubjectEngagement> subjectRankings;
        public Map<String, Integer> weeklyParticipation; // day->score
        public Map<String, Integer> monthlyWeekParticipation;

        public DashboardStats() {
            moodDistribution = new HashMap<>();
            subjectRankings = new ArrayList<>();
            weeklyParticipation = new HashMap<>();
            monthlyWeekParticipation = new HashMap<>();
        }
    }

    public static class SmartFeedback {
        public String insight;

        public SmartFeedback(String insight) {
            this.insight = insight;
        }
    }

    // ─────────────────────────────────────────────────
    //  1. ENGAGEMENT HEATMAP
    // ─────────────────────────────────────────────────

    public static Map<String, HeatmapCell> computeHeatmap(List<ClassLog> logs) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
        String[] slots = {"Morning", "Afternoon", "Evening"};

        // bucket[day][slot] -> list of participation scores
        Map<String, List<Integer>> buckets = new HashMap<>();
        for (String d : days) {
            for (String s : slots) {
                buckets.put(d + "_" + s, new ArrayList<>());
            }
        }

        for (ClassLog log : logs) {
            String key = log.dayOfWeek + "_" + log.timeSlot;
            if (buckets.containsKey(key)) {
                buckets.get(key).add(log.getParticipationScore());
            }
        }

        Map<String, HeatmapCell> result = new HashMap<>();
        for (String d : days) {
            for (String s : slots) {
                String key = d + "_" + s;
                List<Integer> scores = buckets.get(key);
                if (scores == null || scores.isEmpty()) {
                    result.put(key, new HeatmapCell(d, s, "None", 0));
                } else {
                    double avg = 0;
                    for (int sc : scores) avg += sc;
                    avg /= scores.size();
                    String level;
                    if (avg >= 2.5) level = "High";
                    else if (avg >= 1.5) level = "Medium";
                    else level = "Low";
                    result.put(key, new HeatmapCell(d, s, level, scores.size()));
                }
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────
    //  2. MOOD × PARTICIPATION CORRELATION
    // ─────────────────────────────────────────────────

    public static List<MoodCorrelation> computeMoodCorrelations(List<ClassLog> logs) {
        String[] moods = {"Focused", "Neutral", "Tired", "Stressed"};
        List<MoodCorrelation> result = new ArrayList<>();

        for (String mood : moods) {
            Map<String, Integer> partCount = new HashMap<>();
            partCount.put("Low", 0);
            partCount.put("Medium", 0);
            partCount.put("High", 0);
            int total = 0;

            for (ClassLog log : logs) {
                if (mood.equals(log.mood) && log.participation != null) {
                    partCount.put(log.participation, partCount.getOrDefault(log.participation, 0) + 1);
                    total++;
                }
            }

            if (total == 0) continue;

            // Find dominant participation
            String dominant = "Medium";
            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : partCount.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    dominant = entry.getKey();
                }
            }

            int percentage = (int) Math.round((maxCount * 100.0) / total);
            result.add(new MoodCorrelation(mood, dominant, percentage));
        }
        return result;
    }

    // ─────────────────────────────────────────────────
    //  3. SUBJECT ENGAGEMENT RANKING
    // ─────────────────────────────────────────────────

    public static List<SubjectEngagement> computeSubjectRankings(List<ClassLog> logs, List<Subject> subjects) {
        // Group logs by subjectId
        Map<Integer, List<ClassLog>> bySubject = new HashMap<>();
        for (ClassLog log : logs) {
            if (!bySubject.containsKey(log.subjectId)) {
                bySubject.put(log.subjectId, new ArrayList<>());
            }
            bySubject.get(log.subjectId).add(log);
        }

        List<SubjectEngagement> result = new ArrayList<>();
        for (Subject subject : subjects) {
            List<ClassLog> subLogs = bySubject.getOrDefault(subject.id, new ArrayList<>());
            if (subLogs.isEmpty()) {
                result.add(new SubjectEngagement(subject.name, subject.id, 0, 0));
                continue;
            }

            double totalScore = 0;
            for (ClassLog log : subLogs) {
                // Attendance (0-2) * 20 + Participation (0-3) * 20 + Notes (0-1) * 10 → max 90
                double score = log.getAttendanceScore() * 20
                        + log.getParticipationScore() * 20
                        + (log.notes != null && !log.notes.isEmpty() ? 10 : 0);
                totalScore += score;
            }
            double avgScore = Math.min(100, (totalScore / subLogs.size()) * (100.0 / 90.0));
            result.add(new SubjectEngagement(subject.name, subject.id, Math.round(avgScore), subLogs.size()));
        }

        // Sort descending
        Collections.sort(result, (a, b) -> Double.compare(b.engagementScore, a.engagementScore));
        return result;
    }

    // ─────────────────────────────────────────────────
    //  4. MID-WEEK SLUMP DETECTION
    // ─────────────────────────────────────────────────

    public static String detectMidWeekSlump(List<ClassLog> logs) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
        Map<String, List<Integer>> dayScores = new HashMap<>();
        for (String d : days) dayScores.put(d, new ArrayList<>());

        for (ClassLog log : logs) {
            if (log.dayOfWeek != null && dayScores.containsKey(log.dayOfWeek)) {
                dayScores.get(log.dayOfWeek).add(log.getParticipationScore());
            }
        }

        Map<String, Double> dayAvg = new HashMap<>();
        for (String d : days) {
            List<Integer> scores = dayScores.get(d);
            if (scores == null || scores.isEmpty()) continue;
            double avg = 0;
            for (int s : scores) avg += s;
            dayAvg.put(d, avg / scores.size());
        }

        // Find lowest day
        String lowestDay = null;
        double lowestScore = Double.MAX_VALUE;
        for (Map.Entry<String, Double> e : dayAvg.entrySet()) {
            if (e.getValue() < lowestScore) {
                lowestScore = e.getValue();
                lowestDay = e.getKey();
            }
        }

        if (lowestDay != null && lowestScore < 1.5) {
            return lowestDay;
        }
        return null;
    }

    // ─────────────────────────────────────────────────
    //  5. TRAJECTORY (14-DAY COMPARISON)
    // ─────────────────────────────────────────────────

    public static TrajectoryResult computeTrajectory(List<ClassLog> allLogs) {
        long now = System.currentTimeMillis();
        long day14 = 14L * 24 * 60 * 60 * 1000;

        List<ClassLog> recent = new ArrayList<>();
        List<ClassLog> previous = new ArrayList<>();

        for (ClassLog log : allLogs) {
            long age = now - log.logDate;
            if (age <= day14) recent.add(log);
            else if (age <= day14 * 2) previous.add(log);
        }

        if (previous.isEmpty() && recent.isEmpty()) {
            return new TrajectoryResult("Stable", "→", "#78909C",
                    "Not enough data yet. Keep logging your classes!", null);
        }
        if (previous.isEmpty()) {
            return new TrajectoryResult("Stable", "→", "#78909C",
                    "Logging consistently. Check back in 2 weeks for a comparison.", null);
        }

        double recentAvg = participationAvg(recent);
        double prevAvg = participationAvg(previous);
        double delta = recentAvg - prevAvg;

        String slump = detectMidWeekSlump(allLogs);
        String slumpMsg = slump != null
                ? "Mid-week slump detected — " + slump + " engagement consistently low."
                : "No significant slump patterns detected.";

        if (delta > 0.25) {
            return new TrajectoryResult("Improving", "↑", "#4CAF50",
                    "Participation improving vs last period", slump);
        } else if (delta < -0.25) {
            return new TrajectoryResult("Declining", "↓", "#F44336",
                    "Participation has dipped compared to last period", slump);
        } else {
            return new TrajectoryResult("Stable", "→", "#78909C",
                    "Consistency maintained over the last 14 days", slump);
        }
    }

    private static double participationAvg(List<ClassLog> logs) {
        if (logs.isEmpty()) return 0;
        double sum = 0;
        for (ClassLog l : logs) sum += l.getParticipationScore();
        return sum / logs.size();
    }

    // ─────────────────────────────────────────────────
    //  6. DASHBOARD STATISTICS
    // ─────────────────────────────────────────────────

    public static DashboardStats computeDashboardStats(List<ClassLog> logs, List<Subject> subjects, boolean weekly) {
        DashboardStats stats = new DashboardStats();
        if (logs.isEmpty()) return stats;

        // Filter by period
        long now = System.currentTimeMillis();
        long period = weekly ? 7L * 24 * 60 * 60 * 1000 : 30L * 24 * 60 * 60 * 1000;
        List<ClassLog> filtered = new ArrayList<>();
        for (ClassLog l : logs) {
            if (now - l.logDate <= period) filtered.add(l);
        }
        if (filtered.isEmpty()) filtered = logs;

        // Attendance rate
        int present = 0, nonAbsent = 0;
        for (ClassLog l : filtered) {
            if (!"Absent".equals(l.attendance)) nonAbsent++;
            if ("Present".equals(l.attendance)) present++;
        }
        stats.attendanceRate = filtered.isEmpty() ? 0 : (int) Math.round((nonAbsent * 100.0) / filtered.size());

        // Avg participation
        double partSum = 0;
        for (ClassLog l : filtered) partSum += l.getParticipationScore();
        double partAvg = filtered.isEmpty() ? 0 : partSum / filtered.size();
        stats.avgParticipation = (int) Math.round((partAvg / 3.0) * 100);
        if (partAvg >= 2.5) stats.avgParticipationLabel = "High";
        else if (partAvg >= 1.5) stats.avgParticipationLabel = "Med";
        else stats.avgParticipationLabel = "Low";

        // Sessions count — total logs in period (fixed)
        stats.notesCount = filtered.size();

        // Mood distribution
        for (ClassLog l : filtered) {
            if (l.mood != null) {
                stats.moodDistribution.put(l.mood, stats.moodDistribution.getOrDefault(l.mood, 0) + 1);
            }
        }

        // Subject rankings
        stats.subjectRankings = computeSubjectRankings(filtered, subjects);

        // Top subject
        if (!stats.subjectRankings.isEmpty()) {
            stats.topSubject = abbreviate(stats.subjectRankings.get(0).subjectName);
        } else {
            stats.topSubject = "—";
        }

        // Best/worst day
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        Map<String, Double> dayAvgs = new HashMap<>();
        for (String d : days) {
            List<Integer> scores = new ArrayList<>();
            for (ClassLog l : filtered) {
                if (d.equals(l.dayOfWeek)) scores.add(l.getParticipationScore());
            }
            if (!scores.isEmpty()) {
                double avg = 0;
                for (int s : scores) avg += s;
                dayAvgs.put(d, avg / scores.size());
            }
        }

        String bestDay = "—", worstDay = "—";
        double bestScore = -1, worstScore = Double.MAX_VALUE;
        for (Map.Entry<String, Double> e : dayAvgs.entrySet()) {
            if (e.getValue() > bestScore) { bestScore = e.getValue(); bestDay = expandDay(e.getKey()); }
            if (e.getValue() < worstScore) { worstScore = e.getValue(); worstDay = expandDay(e.getKey()); }
        }
        stats.bestDay = bestDay;
        stats.worstDay = worstDay;

        // Weekly participation per day
        for (String d : days) {
            List<Integer> scores = new ArrayList<>();
            for (ClassLog l : filtered) {
                if (d.equals(l.dayOfWeek)) scores.add(l.getParticipationScore());
            }
            if (!scores.isEmpty()) {
                double avg = 0;
                for (int s : scores) avg += s;
                stats.weeklyParticipation.put(d, (int) Math.round((avg / scores.size() / 3.0) * 100));
            }
        }

        // Monthly week buckets
        stats.monthlyWeekParticipation = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long mStart = cal.getTimeInMillis();
        long weekMs = 7L * 24 * 60 * 60 * 1000;
        for (int w = 0; w < 4; w++) {
            long wStart = mStart + w * weekMs;
            long wEnd   = wStart + weekMs;
            int sum = 0, count = 0;
            for (ClassLog l : logs) {
                if (l.logDate >= wStart && l.logDate < wEnd) {
                    sum += l.getParticipationScore();
                    count++;
                }
            }
            stats.monthlyWeekParticipation.put("Wk " + (w + 1), count > 0 ? (int)Math.round((sum / (double)count / 3.0) * 100) : 0);
        }

        return stats;
    }

    // ─────────────────────────────────────────────────
    //  7. SMART FEEDBACK (Rule-based heuristic)
    // ─────────────────────────────────────────────────

    public static SmartFeedback generateSmartFeedback(List<ClassLog> logs, List<Subject> subjects) {
        if (logs.size() < 3) {
            return new SmartFeedback("Keep logging your classes to unlock personalized insights!");
        }

        // Rule 1: Morning vs Afternoon performance
        double morningAvg = slotAvg(logs, "Morning");
        double afternoonAvg = slotAvg(logs, "Afternoon");
        if (morningAvg > 0 && afternoonAvg > 0) {
            if (morningAvg - afternoonAvg > 0.5) {
                return new SmartFeedback(
                    "You participate more in morning classes — try reviewing notes before afternoon sessions.");
            }
            if (afternoonAvg - morningAvg > 0.5) {
                return new SmartFeedback(
                    "You perform better in afternoon classes! Consider scheduling study time in the morning.");
            }
        }

        // Rule 2: Mid-week slump
        String slumpDay = detectMidWeekSlump(logs);
        if (slumpDay != null) {
            return new SmartFeedback(
                "Your engagement drops on " + expandDay(slumpDay) +
                "s — try a mid-week reset or review session.");
        }

        // Rule 3: Low engagement subject
        List<SubjectEngagement> rankings = computeSubjectRankings(logs, subjects);
        if (!rankings.isEmpty()) {
            SubjectEngagement lowest = rankings.get(rankings.size() - 1);
            if (lowest.logCount >= 2 && lowest.engagementScore < 45) {
                return new SmartFeedback(
                    "Your engagement in " + lowest.subjectName +
                    " is low. Consider allocating extra prep time for this class.");
            }
        }

        // Rule 4: Tired mood triggers low participation (≥3 consecutive)
        int tiredLow = 0;
        for (ClassLog l : logs) {
            if ("Tired".equals(l.mood) && "Low".equals(l.participation)) tiredLow++;
        }
        if (tiredLow >= 3) {
            return new SmartFeedback(
                "Being tired often leads to low participation. Try improving your sleep schedule!");
        }

        // Rule 5: Improving trajectory
        TrajectoryResult traj = computeTrajectory(logs);
        if ("Improving".equals(traj.direction)) {
            return new SmartFeedback("Great progress! Your participation has been improving over the last 2 weeks. Keep it up! 🎉");
        }
        if ("Declining".equals(traj.direction)) {
            return new SmartFeedback("Your participation has dipped recently. Try re-engaging with your study routine.");
        }

        return new SmartFeedback("You're maintaining consistent class attendance. Great habit! Keep reflecting after every session.");
    }

    // ─────────────────────────────────────────────────
    //  HELPER UTILITIES
    // ─────────────────────────────────────────────────

    private static double slotAvg(List<ClassLog> logs, String slot) {
        double sum = 0; int count = 0;
        for (ClassLog l : logs) {
            if (slot.equals(l.timeSlot)) { sum += l.getParticipationScore(); count++; }
        }
        return count == 0 ? 0 : sum / count;
    }

    private static String expandDay(String abbr) {
        if (abbr == null) return "";
        switch (abbr) {
            case "Mon": return "Monday";
            case "Tue": return "Tuesday";
            case "Wed": return "Wednesday";
            case "Thu": return "Thursday";
            case "Fri": return "Friday";
            case "Sat": return "Saturday";
            case "Sun": return "Sunday";
            default:    return abbr;
        }
    }

    private static String abbreviate(String name) {
        if (name == null) return "";
        if (name.length() <= 6) return name;
        String[] words = name.split("\\s+");
        if (words.length == 1) return name.substring(0, Math.min(6, name.length())) + ".";
        // Use first meaningful word
        return words[0].length() > 6 ? words[0].substring(0, 5) + "." : words[0];
    }

    public static String getTimeSlotForHour(int hour) {
        if (hour < 12) return "Morning";
        if (hour < 17) return "Afternoon";
        return "Evening";
    }

    public static String getDayAbbr(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY:    return "Mon";
            case Calendar.TUESDAY:   return "Tue";
            case Calendar.WEDNESDAY: return "Wed";
            case Calendar.THURSDAY:  return "Thu";
            case Calendar.FRIDAY:    return "Fri";
            case Calendar.SATURDAY:  return "Sat";
            case Calendar.SUNDAY:    return "Sun";
            default:                 return "Mon";
        }
    }
}
