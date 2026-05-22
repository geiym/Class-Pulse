package com.classpulse.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.classpulse.R;
import com.classpulse.models.ClassLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LogsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LOG    = 1;

    public interface OnLogClickListener { void onLogClick(ClassLog log); }

    private final OnLogClickListener listener;
    private final List<Object> items = new ArrayList<>(); // String headers + ClassLog entries

    public LogsAdapter(OnLogClickListener listener) {
        this.listener = listener;
    }

    public void setLogs(List<ClassLog> logs) {
        items.clear();
        if (logs == null || logs.isEmpty()) { notifyDataSetChanged(); return; }

        // Sort by date descending
        logs.sort((a, b) -> Long.compare(b.logDate, a.logDate));

        // Group into TODAY / THIS WEEK / previous weeks
        Calendar now = Calendar.getInstance();
        long todayStart = getDayStart(now);
        long weekStart  = getWeekStart(now);

        String lastHeader = null;
        for (ClassLog log : logs) {
            String header;
            if (log.logDate >= todayStart)    header = "TODAY";
            else if (log.logDate >= weekStart) header = "THIS WEEK";
            else {
                // Format as "WEEK OF MMM d"
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(log.logDate);
                c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
                SimpleDateFormat fmt = new SimpleDateFormat("MMM d", Locale.getDefault());
                header = "WEEK OF " + fmt.format(c.getTime()).toUpperCase();
            }

            if (!header.equals(lastHeader)) {
                items.add(header);
                lastHeader = header;
            }
            items.add(log);
        }
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int pos) {
        return items.get(pos) instanceof String ? TYPE_HEADER : TYPE_LOG;
    }

    @Override public int getItemCount() { return items.size(); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_log_section_header, parent, false);
            return new HeaderVH(v);
        }
        View v = inf.inflate(R.layout.item_log, parent, false); // uses your existing item_log.xml
        return new LogVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tvHeader.setText((String) items.get(pos));
        } else {
            ((LogVH) holder).bind((ClassLog) items.get(pos), listener);
        }
    }

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(View v) { super(v); tvHeader = v.findViewById(R.id.tvSectionHeader); }
    }

    static class LogVH extends RecyclerView.ViewHolder {
        // Uses existing item_log.xml IDs
        View dateBadge;
        TextView tvMonth, tvDay, tvSubject, tvAttChip, tvPartChip, tvMoodChip, tvNotes;

        LogVH(View v) {
            super(v);
            tvMonth    = v.findViewById(R.id.tv_log_month);
            tvDay      = v.findViewById(R.id.tv_log_day);
            // Get date badge container via month TextView's parent
            dateBadge  = tvMonth != null ? (View) tvMonth.getParent() : null;
            tvSubject  = v.findViewById(R.id.tv_log_subject);
            tvAttChip  = v.findViewById(R.id.tv_attendance_chip);
            tvPartChip = v.findViewById(R.id.tv_participation_chip);
            tvMoodChip = v.findViewById(R.id.tv_mood_chip);
            tvNotes    = v.findViewById(R.id.tv_log_notes);
        }

        void bind(ClassLog log, OnLogClickListener listener) {
            // Date
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(log.logDate);
            if (tvMonth != null) tvMonth.setText(
                    new SimpleDateFormat("MMM", Locale.getDefault()).format(c.getTime()).toUpperCase());
            if (tvDay != null) tvDay.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));

            // Tint the date badge by attendance
            if (dateBadge != null) {
                int boxColor = "Present".equals(log.attendance) ? Color.parseColor("#5CAD72")
                        : "Late".equals(log.attendance)    ? Color.parseColor("#E0A050")
                        :                                    Color.parseColor("#D95F6A");
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setColor(boxColor);
                bg.setCornerRadius(12 * itemView.getResources().getDisplayMetrics().density);
                dateBadge.setBackground(bg);
            }

            // Subject
            if (tvSubject != null)
                tvSubject.setText(log.subjectName != null ? log.subjectName : "Unknown");

            // Attendance chip
            if (tvAttChip != null) {
                String attEmoji = "Present".equals(log.attendance) ? "✅ "
                        : "Late".equals(log.attendance)    ? "🕐 " : "❌ ";
                tvAttChip.setText(attEmoji + (log.attendance != null ? log.attendance : "—"));
                tvAttChip.setBackgroundResource(
                        "Present".equals(log.attendance) ? R.drawable.bg_badge_log_present
                                : "Late".equals(log.attendance)    ? R.drawable.bg_badge_log_late
                                :                                    R.drawable.bg_badge_log_absent);
                tvAttChip.setTextColor(
                        "Present".equals(log.attendance) ? Color.parseColor("#2E7D52")
                                : "Late".equals(log.attendance)    ? Color.parseColor("#B45309")
                                :                                    Color.parseColor("#B03030"));
            }

            // Participation chip
            if (tvPartChip != null) {
                tvPartChip.setText(log.participation != null ? log.participation : "—");
                tvPartChip.setTextColor(Color.parseColor("#3F51B5"));
            }

            // Mood chip
            if (tvMoodChip != null) {
                String emoji = getMoodEmoji(log.mood);
                tvMoodChip.setText(emoji + " " + (log.mood != null ? log.mood : "—"));
                tvMoodChip.setTextColor(Color.parseColor("#0288D1"));
            }

            // Notes
            if (tvNotes != null) {
                if (log.notes != null && !log.notes.isEmpty()) {
                    tvNotes.setText(log.notes);
                    tvNotes.setVisibility(View.VISIBLE);
                } else {
                    tvNotes.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> listener.onLogClick(log));
        }

        private static String getMoodEmoji(String mood) {
            if (mood == null) return "😊";
            switch (mood) {
                case "Happy": case "Focused": return "😊";
                case "Neutral":               return "😐";
                case "Tired":                 return "😴";
                case "Stressed":              return "😰";
                default:                      return "😊";
            }
        }
    }

    // ─── Date helpers ─────────────────────────────────────────────────────────

    private long getDayStart(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);      c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long getWeekStart(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);      c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}