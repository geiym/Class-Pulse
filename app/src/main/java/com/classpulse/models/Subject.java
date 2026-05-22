package com.classpulse.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.classpulse.database.Converters;
import java.util.List;

@Entity(tableName = "subjects")
public class Subject {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String classCode;
    public String instructor;
    public int colorInt;
    public String iconUri; // persisted URI from file picker

    @TypeConverters(Converters.class)
    public List<String> classDays;

    @TypeConverters(Converters.class)
    public List<ClassTimeSlot> timeSlots;

    public Subject() {}

    // ─── Inner model ──────────────────────────────────────────────────────────

    public static class ClassTimeSlot {
        public String day;
        public int startHour;
        public int startMinute;
        public int endHour;
        public int endMinute;

        public ClassTimeSlot() {}

        public ClassTimeSlot(String day,
                             int startHour, int startMinute,
                             int endHour,   int endMinute) {
            this.day         = day;
            this.startHour   = startHour;
            this.startMinute = startMinute;
            this.endHour     = endHour;
            this.endMinute   = endMinute;
        }

        public String formatStart() { return formatTime(startHour, startMinute); }
        public String formatEnd()   { return formatTime(endHour,   endMinute);   }

        private String formatTime(int h, int m) {
            String ampm = h < 12 ? "AM" : "PM";
            int h12 = h % 12;
            if (h12 == 0) h12 = 12;
            return String.format(java.util.Locale.getDefault(), "%d:%02d %s", h12, m, ampm);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns true if this subject is scheduled on the given day.
     * Accepts both full names ("Monday") and abbreviations ("Mon").
     * Used by HomeFragment: s.isScheduledOnDay(todayAbbr)
     */
    public boolean isScheduledOnDay(String dayAbbr) {
        if (classDays == null || dayAbbr == null) return false;
        for (String day : classDays) {
            if (day.equalsIgnoreCase(dayAbbr)
                    || abbreviate(day).equalsIgnoreCase(dayAbbr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a human-readable schedule string e.g. "Mon/Wed/Fri  8:00 AM – 4:00 PM"
     * Used by HomeFragment: s.getFormattedSchedule()
     */
    public String getFormattedSchedule() {
        if (classDays == null || classDays.isEmpty()) return "No schedule";

        StringBuilder days = new StringBuilder();
        for (int i = 0; i < classDays.size(); i++) {
            if (i > 0) days.append("/");
            days.append(abbreviate(classDays.get(i)));
        }

        if (timeSlots == null || timeSlots.isEmpty()) return days.toString();

        // Find the slot matching the first day, fallback to index 0
        ClassTimeSlot slot = timeSlots.get(0);
        for (ClassTimeSlot ts : timeSlots) {
            if (ts.day != null && abbreviate(ts.day)
                    .equalsIgnoreCase(abbreviate(classDays.get(0)))) {
                slot = ts;
                break;
            }
        }

        return days + "  " + slot.formatStart() + " \u2013 " + slot.formatEnd();
    }

    /**
     * Short summary used by the subject list cards e.g. "Mon/Wed 8:00 AM - 4:00 PM"
     */
    public String getScheduleSummary() {
        if (classDays == null || classDays.isEmpty()) return "";

        StringBuilder days = new StringBuilder();
        for (int i = 0; i < classDays.size(); i++) {
            if (i > 0) days.append("/");
            days.append(abbreviate(classDays.get(i)));
        }

        if (timeSlots == null || timeSlots.isEmpty()) return days.toString();

        ClassTimeSlot first = timeSlots.get(0);
        return days + " " + first.formatStart() + " - " + first.formatEnd();
    }

    private String abbreviate(String day) {
        if (day == null || day.length() < 3) return day != null ? day : "";
        switch (day) {
            case "Monday":    return "Mon";
            case "Tuesday":   return "Tue";
            case "Wednesday": return "Wed";
            case "Thursday":  return "Thu";
            case "Friday":    return "Fri";
            case "Saturday":  return "Sat";
            case "Sunday":    return "Sun";
            default:          return day.substring(0, 3);
        }
    }
}