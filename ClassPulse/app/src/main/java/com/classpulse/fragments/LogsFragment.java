package com.classpulse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classpulse.R;
import com.classpulse.activities.LogReflectionActivity;
import com.classpulse.adapters.LogsAdapter;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogsFragment extends Fragment {

    private RecyclerView rvLogs;
    private LinearLayout llEmpty, llFilterChips;
    private EditText etSearch;
    private LogsAdapter adapter;
    private List<ClassLog> allLogs = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private String activeFilter = "All";
    private boolean showBackButton = false;
    private String searchQuery = "";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLogs = view.findViewById(R.id.rv_logs);
        llEmpty = view.findViewById(R.id.ll_empty_logs);
        llFilterChips = view.findViewById(R.id.ll_filter_chips);
        etSearch = view.findViewById(R.id.etSearchLogs);

        showBackButton = getArguments() != null && getArguments().getBoolean("show_back", false);
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setVisibility(showBackButton ? View.VISIBLE : View.GONE);
            btnBack.setOnClickListener(v ->
                    requireActivity().onBackPressed()); // ← FIXED
        }

        adapter = new LogsAdapter(this::showLogDetail);
        rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLogs.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                searchQuery = s.toString().trim().toLowerCase();
                applyFilter(activeFilter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        Button btnLogFirst = view.findViewById(R.id.btnLogFirstClass);
        if (btnLogFirst != null) {
            btnLogFirst.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), LogReflectionActivity.class)));
        }

        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabLog =
                view.findViewById(R.id.fab_log_class);
        if (fabLog != null) {
            fabLog.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), LogReflectionActivity.class)));
        }

        loadData();
    }

    public static LogsFragment newInstance(boolean showBackButton) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putBoolean("show_back", showBackButton);
        fragment.setArguments(args);
        return fragment;
    }

    private void loadData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            allLogs     = db.classLogDao().getAllLogsSync();
            allSubjects = db.subjectDao().getAllSubjectsSync();

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                buildFilterChips();
                applyFilter(activeFilter);
            });
        });
    }

    private void buildFilterChips() {
        llFilterChips.removeAllViews();

        List<String> filters = new ArrayList<>();
        filters.add("All");
        for (Subject s : allSubjects) filters.add(s.name);
        filters.add("High");
        filters.add("Present");

        int dp = (int) getResources().getDisplayMetrics().density;

        for (String f : filters) {
            android.widget.TextView chip = new android.widget.TextView(requireContext());
            chip.setText(f);
            chip.setTextSize(14);
            chip.setPadding(
                    (int)(20 * dp), (int)(10 * dp),
                    (int)(20 * dp), (int)(10 * dp));

            boolean selected = f.equals(activeFilter);

            if (selected) {
                chip.setTextColor(android.graphics.Color.WHITE);
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            } else {
                chip.setTextColor(android.graphics.Color.parseColor("#555555"));
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTypeface(android.graphics.Typeface.DEFAULT);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd((int)(8 * dp));
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {
                activeFilter = f;
                buildFilterChips();
                applyFilter(f);
            });
            llFilterChips.addView(chip);
        }
    }

    private void applyFilter(String filter) {
        List<ClassLog> filtered = new ArrayList<>();
        for (ClassLog log : allLogs) {
            boolean passesFilter;
            if ("All".equals(filter))                                                passesFilter = true;
            else if ("High".equals(filter) && "High".equals(log.participation))      passesFilter = true;
            else if ("Present".equals(filter) && "Present".equals(log.attendance))   passesFilter = true;
            else passesFilter = log.subjectName != null && log.subjectName.equals(filter);

            boolean passesSearch = searchQuery.isEmpty()
                    || (log.subjectName != null && log.subjectName.toLowerCase().contains(searchQuery))
                    || (log.mood != null && log.mood.toLowerCase().contains(searchQuery))
                    || (log.attendance != null && log.attendance.toLowerCase().contains(searchQuery))
                    || (log.notes != null && log.notes.toLowerCase().contains(searchQuery));

            if (passesFilter && passesSearch) filtered.add(log);
        }

        adapter.setLogs(filtered);
        boolean empty = filtered.isEmpty();
        rvLogs.setVisibility(empty ? View.GONE : View.VISIBLE);
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showLogDetail(ClassLog log) {
        String msg = "Subject: " + log.subjectName
                + "\nAttendance: " + log.attendance
                + "\nParticipation: " + log.participation
                + "\nMood: " + log.mood
                + (log.notes != null && !log.notes.isEmpty() ? "\n\nNotes:\n" + log.notes : "");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Log Details")
                .setMessage(msg)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override public void onResume()  { super.onResume();  loadData(); }
    @Override public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}