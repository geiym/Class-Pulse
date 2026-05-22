package com.classpulse.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classpulse.R;
import com.classpulse.activities.AddEditSubjectActivity;
import com.classpulse.activities.ReportRoomActivity;
import com.classpulse.adapters.SubjectsAdapter;
import com.classpulse.models.Subject;
import com.classpulse.viewmodels.SubjectViewModel;

import java.util.List;
import java.util.Set;

public class SubjectsFragment extends Fragment {

    private SubjectViewModel viewModel;
    private SubjectsAdapter  adapter;
    private LinearLayout     layoutEmptyState;
    private RecyclerView     rvSubjects;
    private ImageButton      btnDeleteMode;
    private TextView         tvDeleteAll;

    private List<Subject> currentSubjects;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subjects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── 1. Bind all views first ───────────────────────────────────────────
        rvSubjects       = view.findViewById(R.id.rvSubjects);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        btnDeleteMode    = view.findViewById(R.id.btnDeleteMode);
        tvDeleteAll      = view.findViewById(R.id.tvDeleteAll);

        // ── 2. ViewModel — requireActivity() so it survives fragment recreate ─
        viewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);

        // ── 3. Setup RecyclerView now that rvSubjects is not null ─────────────
        setupRecyclerView();

        // ── 4. Observe data ───────────────────────────────────────────────────
        observeSubjects();

        // ── 5. Button listeners ───────────────────────────────────────────────
        view.findViewById(R.id.btnAddSubject).setOnClickListener(v -> {
            if (adapter.isDeleteMode()) exitDeleteMode();
            else openAddSubject();
        });

        btnDeleteMode.setOnClickListener(v -> {
            if (adapter.isDeleteMode()) exitDeleteMode();
            else enterDeleteMode();
        });

        tvDeleteAll.setOnClickListener(v -> onDeleteAllClicked());
    }

    // ─── RecyclerView ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new SubjectsAdapter(new SubjectsAdapter.OnSubjectActionListener() {
            @Override
            public void onEdit(Subject subject) {
                openSubjectDetail(subject);
            }

            @Override
            public void onDelete(Subject subject) {
                confirmDeleteSingle(subject);
            }
        }, () -> {
            if (adapter.isDeleteMode()) updateDeleteAllText();
        });
        rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubjects.setAdapter(adapter);
    }

    private void observeSubjects() {
        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            currentSubjects = subjects;
            adapter.setSubjects(subjects);
            boolean empty = subjects == null || subjects.isEmpty();
            rvSubjects.setVisibility(empty ? View.GONE    : View.VISIBLE);
            layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    // ─── Delete mode ──────────────────────────────────────────────────────────

    private void enterDeleteMode() {
        adapter.setDeleteMode(true);
        tvDeleteAll.setVisibility(View.VISIBLE);
        updateDeleteAllText();
    }

    private void exitDeleteMode() {
        adapter.setDeleteMode(false);
        tvDeleteAll.setVisibility(View.GONE);
    }

    private void updateDeleteAllText() {
        int count = adapter.getSelectedIds().size();
        if (count >= 2) tvDeleteAll.setText("Delete all");
        else            tvDeleteAll.setText("Delete");
    }

    private void onDeleteAllClicked() {
        Set<Integer> selected = adapter.getSelectedIds();

        if (selected.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No subjects selected", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = selected.size();
        String title   = count >= 2 ? "Delete Subjects" : "Delete Subject";
        String message = count >= 2
                ? "Delete " + count + " selected subjects? This cannot be undone."
                : "Delete this subject? This cannot be undone.";

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Delete", (d, w) -> {
                    if (currentSubjects != null) {
                        for (Subject s : currentSubjects) {
                            if (selected.contains(s.id)) viewModel.delete(s);
                        }
                    }
                    exitDeleteMode();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private void openAddSubject() {
        startActivity(new Intent(requireContext(), AddEditSubjectActivity.class));
    }

    private void openSubjectDetail(Subject subject) {
        Intent intent = new Intent(requireContext(), ReportRoomActivity.class);
        intent.putExtra(ReportRoomActivity.EXTRA_SUBJECT_ID, subject.id);
        startActivity(intent);
    }

    private void confirmDeleteSingle(Subject subject) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Subject")
                .setMessage("Delete \"" + subject.name + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> viewModel.delete(subject))
                .setNegativeButton("Cancel", null)
                .show();
    }
}