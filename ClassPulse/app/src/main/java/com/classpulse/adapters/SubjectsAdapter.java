package com.classpulse.adapters;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.classpulse.R;
import com.classpulse.models.Subject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

    public interface OnSubjectActionListener {
        void onEdit(Subject subject);
        void onDelete(Subject subject);
    }

    private List<Subject> subjects = new ArrayList<>();
    private final OnSubjectActionListener listener;
    private final Runnable onSelectionChanged;

    // ─── Delete mode state ────────────────────────────────────────────────────
    private boolean deleteMode = false;
    private final Set<Integer> selectedIds = new HashSet<>();

    /** Use this constructor when you don't need selection-change callbacks */
    public SubjectsAdapter(OnSubjectActionListener listener) {
        this(listener, null);
    }

    public SubjectsAdapter(OnSubjectActionListener listener, Runnable onSelectionChanged) {
        this.listener = listener;
        this.onSelectionChanged = onSelectionChanged;
    }

    public void setSubjects(List<Subject> list) {
        this.subjects = list != null ? list : new ArrayList<>();
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public void setDeleteMode(boolean enabled) {
        deleteMode = enabled;
        if (!enabled) selectedIds.clear();
        notifyDataSetChanged();
    }

    public boolean isDeleteMode() { return deleteMode; }

    public Set<Integer> getSelectedIds() { return new HashSet<>(selectedIds); }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        // Pass onSelectionChanged into bind() so the static ViewHolder can use it
        holder.bind(subjects.get(position), listener, deleteMode, selectedIds, onSelectionChanged);
    }

    @Override
    public int getItemCount() { return subjects.size(); }

    // ─── ViewHolder (static — no reference to outer class) ───────────────────

    static class SubjectViewHolder extends RecyclerView.ViewHolder {

        private final CardView  cardSubject;
        private final ImageView ivSubjectIcon;
        private final TextView  tvName;
        private final TextView  tvSchedule;
        private final CheckBox  cbDeleteSelect;
        private final ImageView ivChevron;

        SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSubject    = itemView.findViewById(R.id.cardSubject);
            ivSubjectIcon  = itemView.findViewById(R.id.ivSubjectIcon);
            tvName         = itemView.findViewById(R.id.tvSubjectName);
            tvSchedule     = itemView.findViewById(R.id.tvSubjectSchedule);
            cbDeleteSelect = itemView.findViewById(R.id.cbDeleteSelect);
            ivChevron      = itemView.findViewById(R.id.ivChevron);
        }

        void bind(Subject subject,
                  OnSubjectActionListener listener,
                  boolean deleteMode,
                  Set<Integer> selectedIds,
                  Runnable onSelectionChanged) {

            tvName.setText(subject.name);
            tvSchedule.setText(subject.getScheduleSummary());

            // Load icon
            if (ivSubjectIcon != null) {
                if (subject.iconUri != null && !subject.iconUri.isEmpty()) {
                    try {
                        ivSubjectIcon.setImageURI(Uri.parse(subject.iconUri));
                        if (ivSubjectIcon.getDrawable() == null)
                            ivSubjectIcon.setImageResource(R.drawable.ic_subject_default);
                    } catch (Exception e) {
                        ivSubjectIcon.setImageResource(R.drawable.ic_subject_default);
                    }
                } else {
                    ivSubjectIcon.setImageResource(R.drawable.ic_subject_default);
                }
            }

            // Card + text colors
            cardSubject.setCardBackgroundColor(subject.colorInt);
            int textColor = isColorLight(subject.colorInt)
                    ? Color.parseColor("#1C1C1E") : Color.WHITE;
            tvName.setTextColor(textColor);
            tvSchedule.setTextColor(adjustAlpha(textColor, 0.65f));

            // ─── Delete mode ──────────────────────────────────────────────────
            if (deleteMode) {
                cbDeleteSelect.setVisibility(View.VISIBLE);
                ivChevron.setVisibility(View.GONE);

                cbDeleteSelect.setOnCheckedChangeListener(null);
                cbDeleteSelect.setChecked(selectedIds.contains(subject.id));

                cbDeleteSelect.setOnCheckedChangeListener((btn, checked) -> {
                    if (checked) selectedIds.add(subject.id);
                    else         selectedIds.remove(subject.id);
                    if (onSelectionChanged != null) onSelectionChanged.run();
                });

                // Tapping the card toggles checkbox
                cardSubject.setOnClickListener(v ->
                        cbDeleteSelect.setChecked(!cbDeleteSelect.isChecked()));
                cardSubject.setOnLongClickListener(null);

            } else {
                cbDeleteSelect.setVisibility(View.GONE);
                ivChevron.setVisibility(View.VISIBLE);
                cbDeleteSelect.setOnCheckedChangeListener(null);

                cardSubject.setOnClickListener(v -> listener.onEdit(subject));
                cardSubject.setOnLongClickListener(v -> {
                    listener.onDelete(subject);
                    return true;
                });
            }
        }

        private static boolean isColorLight(int color) {
            double r = Color.red(color) / 255.0;
            double g = Color.green(color) / 255.0;
            double b = Color.blue(color) / 255.0;
            return (0.2126 * r + 0.7152 * g + 0.0722 * b) > 0.5;
        }

        private static int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        }
    }
}