package com.classpulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classpulse.R;
import com.classpulse.adapters.SubjectsAdapter;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassSetupActivity extends AppCompatActivity {

    private RecyclerView    rvSubjects;
    private View            llEmpty;
    private SubjectsAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_setup);

        rvSubjects = findViewById(R.id.rv_subjects);
        llEmpty    = findViewById(R.id.ll_empty_subjects);

        adapter = new SubjectsAdapter(new SubjectsAdapter.OnSubjectActionListener() {
            @Override
            public void onEdit(Subject subject) {
                Intent intent = new Intent(ClassSetupActivity.this, AddEditSubjectActivity.class);
                intent.putExtra("subject_id", subject.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(Subject subject) {
                new AlertDialog.Builder(ClassSetupActivity.this)
                        .setTitle("Delete Subject")
                        .setMessage("Delete \"" + subject.name + "\"? This cannot be undone.")
                        .setPositiveButton("Delete", (d, w) ->
                                executor.execute(() ->
                                        AppDatabase.getInstance(ClassSetupActivity.this)
                                                .subjectDao().delete(subject)))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        rvSubjects.setAdapter(adapter);

        // Observe subjects from Room
        AppDatabase.getInstance(this).subjectDao().getAllSubjects()
                .observe(this, subjects -> {
                    adapter.setSubjects(subjects);
                    boolean empty = subjects == null || subjects.isEmpty();
                    rvSubjects.setVisibility(empty ? View.GONE    : View.VISIBLE);
                    llEmpty.setVisibility   (empty ? View.VISIBLE : View.GONE);
                });

        FloatingActionButton fab = findViewById(R.id.fab_add_subject);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditSubjectActivity.class)));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}